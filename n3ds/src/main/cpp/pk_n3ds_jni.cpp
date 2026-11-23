// The app's way into Azahar (3DS games): boot a cart on the caller's thread, draw with Vulkan to one or two Android
// surfaces, and take buttons, the circle pad and touches. The JNI functions belong to com.pokedaisy.app.n3ds.N3dsNative.
//
// Screens: with two surfaces, the main one shows the top screen alone and the second one the touch screen alone (the
// Thor's two displays, or ONE SCREEN's floating touch window); with one, Azahar uses the selected app layout, which is
// SingleScreen for ONE SCREEN. Its second Android surface is attached when TOUCH SCREEN is opened.
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <dlfcn.h>
#include <jni.h>

#include <algorithm>
#include <atomic>
#include <chrono>
#include <condition_variable>
#include <cstdio>
#include <cstdint>
#include <exception>
#include <limits>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#include "audio_core/sink_details.h"
#include "common/android_utils.h"
#include "common/dynamic_library/dynamic_library.h"
#include "common/file_util.h"
#include "common/logging/backend.h"
#include "common/logging/filter.h"
#include "common/logging/log.h"
#include "common/settings.h"
#include "core/core.h"
#include "core/frontend/applets/default_applets.h"
#include "core/frontend/emu_window.h"
#include "core/frontend/framebuffer_layout.h"
#include "core/hle/kernel/process.h"
#include "core/memory.h"
#include "core/frontend/input.h"
#include "core/hle/service/service.h"
#include "core/loader/loader.h"
#include "core/perf_stats.h"
#include "video_core/gpu.h"
#include "video_core/renderer_base.h"

#define PKLOG(...) __android_log_print(ANDROID_LOG_INFO, "pokedaisy/n3ds", __VA_ARGS__)

namespace {

std::string fromJava(JNIEnv* env, jstring s) {
    if (!s) return {};
    const char* chars = env->GetStringUTFChars(s, nullptr);
    std::string out = chars ? chars : "";
    if (chars) env->ReleaseStringUTFChars(s, chars);
    return out;
}

// On Android Azahar reads every path relative to its user folder (AndroidUtils::TranslateFilePath, the folder from
// N3dsAndroidUtils.getUserDirectory); "!" in front marks a real path, which a cart's is.
std::string nativePath(const std::string& path) {
    return path.empty() || path[0] == '!' ? path : "!" + path;
}

// ---- Vulkan and the windows --------------------------------------------------------------------------------------

std::shared_ptr<Common::DynamicLibrary> vulkanLibrary;

class VulkanContext final : public Frontend::GraphicsContext {
public:
    std::shared_ptr<Common::DynamicLibrary> GetDriverLibrary() override {
        return vulkanLibrary;
    }
};

// One Android surface. Vulkan presents from the game thread itself, so there's no context to make current.
class PkWindow final : public Frontend::EmuWindow {
public:
    PkWindow(ANativeWindow* surface, bool secondary) : EmuWindow(secondary) {
        SetSurface(surface);
    }
    // True when it changed (the renderer then has to rebuild its swapchain).
    bool SetSurface(ANativeWindow* surface) {
        const int w = surface ? ANativeWindow_getWidth(surface) : 0;
        const int h = surface ? ANativeWindow_getHeight(surface) : 0;
        if (surface == current && w == width && h == height) return false;
        current = surface, width = w, height = h;
        window_info.type = Frontend::WindowSystemType::Android;
        window_info.render_surface = surface;
        UpdateCurrentFramebufferLayout(std::max(w, 1), std::max(h, 1), false);
        return true;
    }
    void PollEvents() override {
        ++frames;
    }
    std::unique_ptr<Frontend::GraphicsContext> CreateSharedContext() const override {
        return std::make_unique<VulkanContext>();
    }
    std::shared_ptr<Common::DynamicLibrary> GetDriverLibrary() override {
        return vulkanLibrary;
    }
    std::atomic<u64> frames{0};

private:
    ANativeWindow* current = nullptr;
    int width = 0, height = 0;
};

std::mutex surfaceMutex;
std::condition_variable surfaceCv;         // a surface came (setSurface)
ANativeWindow* surfaces[2] = {};           // [0] main, [1] second
std::unique_ptr<PkWindow> windows[2];     // live while a game runs
std::atomic_bool makeSecondaryWindow{false};

// ---- Input: our own devices, read by the HID service every frame -------------------------------------------------

std::atomic<u32> buttonBits{0};
std::atomic<float> stickX{0}, stickY{0};

class PkButton final : public Input::ButtonDevice {
public:
    explicit PkButton(int bit) : bit(bit) {}
    bool GetStatus() const override {
        return (buttonBits.load(std::memory_order_relaxed) >> bit) & 1;
    }

private:
    int bit;
};

class PkButtonFactory final : public Input::Factory<Input::ButtonDevice> {
public:
    std::unique_ptr<Input::ButtonDevice> Create(const Common::ParamPackage& params) override {
        return std::make_unique<PkButton>(params.Get("code", 0));
    }
};

class PkStick final : public Input::AnalogDevice {
public:
    std::tuple<float, float> GetStatus() const override {
        return {stickX.load(std::memory_order_relaxed), stickY.load(std::memory_order_relaxed)};
    }
};

class PkStickFactory final : public Input::Factory<Input::AnalogDevice> {
public:
    std::unique_ptr<Input::AnalogDevice> Create(const Common::ParamPackage&) override {
        return std::make_unique<PkStick>();
    }
};

// ---- The run loop's state ----------------------------------------------------------------------------------------

std::mutex runMutex;                       // one game at a time
std::atomic<bool> stopRun{false};
std::atomic<bool> paused{false};
std::mutex pauseMutex;
std::condition_variable pauseCv;

// States are taken / loaded between two RunLoop calls on the game thread: requests wait here.
std::mutex requestMutex;
std::string saveStateRequest, loadStateRequest;
std::condition_variable requestDone;
bool requestOk = false;
bool requestPending = false;

bool initialized = false;

// Reads of the game's memory, also between two RunLoop calls (the companion's sample: a consistent copy).
struct ReadJob {
    std::vector<std::pair<u32, u32>> ranges; // address, length
    std::vector<u8> out;
    // Non-zero: names of the modules (CROs) loaded after this static module (CRS), its header's "next" chain, joined
    // by '\n' into [modules]. Can be combined with [ranges] for one consistent frame-boundary observation.
    u32 moduleRoot = 0;
    std::string modules;
    // Written before anything is read (debug builds' pokes: N3dsNative.writeMemory).
    std::vector<std::pair<u32, std::vector<u8>>> writes;
    bool pending = false, ok = false;
};

// A module chain's names (see ReadJob::moduleRoot): each CRO header holds its name's address at +0x84 and the next
// module's at +0x88 once loaded; the CRS's own "next" starts the list. At most 32, every read checked.
std::string moduleNames(Core::System& system, Kernel::Process& process, u32 root) {
    auto& mem = system.Memory();
    const auto valid = [&](u32 a) { return mem.IsValidVirtualAddress(process, a); };
    std::string out;
    if (!valid(root + 0x8C) || mem.Read32(root + 0x80) != 0x304F5243) return out; // "CRO0"
    u32 a = mem.Read32(root + 0x88);
    for (int n = 0; n < 32 && a != 0 && valid(a + 0x8C); n++) {
        const u32 name = mem.Read32(a + 0x84);
        std::string nm;
        for (u32 i = 0; i < 48 && valid(name + i); i++) {
            const char c = static_cast<char>(mem.Read8(name + i));
            if (c < 0x20 || c > 0x7E) break; // the end, or not a name

            nm += c;
        }
        if (!out.empty()) out += '\n';
        out += nm;
        a = mem.Read32(a + 0x88);
    }
    return out;
}
ReadJob readJob;
// One read job at a time: callers (the companion's sampler, the scene probe) queue here, not on readJob itself.
std::mutex readCallers;

void runReads(Core::System& system) {
    std::unique_lock lock(requestMutex);
    if (!readJob.pending) return;
    auto process = system.Kernel().GetCurrentProcess();
    readJob.ok = process != nullptr;
    readJob.out.clear();
    readJob.modules.clear();
    if (process) {
        for (const auto& [addr, data] : readJob.writes)
            if (system.Memory().IsValidVirtualAddress(*process, addr) &&
                system.Memory().IsValidVirtualAddress(*process, addr + static_cast<u32>(data.size()) - 1))
                system.Memory().WriteBlock(*process, addr, data.data(), data.size());
    }
    readJob.writes.clear();
    if (process && readJob.moduleRoot != 0) readJob.modules = moduleNames(system, *process, readJob.moduleRoot);
    if (!process) {
        readJob.pending = false;
        requestDone.notify_all();
        return;
    }
    // Validate the complete virtual range before asking Azahar to copy it. A check of
    // only the first and last byte would miss an unmapped page in the middle.
    for (const auto& [addr, len] : readJob.ranges) {
        const u64 end = static_cast<u64>(addr) + len;
        if (len == 0 || end > static_cast<u64>(std::numeric_limits<u32>::max()) + 1) {
            readJob.ok = false;
            readJob.pending = false;
            requestDone.notify_all();
            return;
        }
        for (u64 page = addr; page < end; page = (page & ~u64{0xFFF}) + 0x1000) {
            if (!system.Memory().IsValidVirtualAddress(*process, static_cast<u32>(page))) {
                readJob.ok = false;
                readJob.pending = false;
                requestDone.notify_all();
                return;
            }
        }
        if (!system.Memory().IsValidVirtualAddress(*process, static_cast<u32>(end - 1))) {
            readJob.ok = false;
            readJob.pending = false;
            requestDone.notify_all();
            return;
        }
    }
    for (const auto& [addr, len] : readJob.ranges) {
        const size_t at = readJob.out.size();
        readJob.out.resize(at + len);
        if (process) system.Memory().ReadBlock(*process, addr, readJob.out.data() + at, len);
    }
    readJob.pending = false;
    requestDone.notify_all();
}

void runRequests(Core::System& system) {
    std::unique_lock lock(requestMutex);
    if (!requestPending) return;
    bool ok = false;
    if (!saveStateRequest.empty()) {
        const std::vector<u8> buffer = system.SaveStateBuffer();
        if (FILE* f = std::fopen((saveStateRequest + ".tmp").c_str(), "wb")) {
            ok = std::fwrite(buffer.data(), 1, buffer.size(), f) == buffer.size();
            ok = (std::fclose(f) == 0) && ok;
            ok = ok && std::rename((saveStateRequest + ".tmp").c_str(), saveStateRequest.c_str()) == 0;
        }
    } else if (!loadStateRequest.empty()) {
        std::vector<u8> buffer;
        if (FILE* f = std::fopen(loadStateRequest.c_str(), "rb")) {
            std::fseek(f, 0, SEEK_END);
            buffer.resize(std::ftell(f));
            std::fseek(f, 0, SEEK_SET);
            ok = std::fread(buffer.data(), 1, buffer.size(), f) == buffer.size();
            std::fclose(f);
        }
        ok = ok && system.LoadStateBuffer(std::move(buffer));
    }
    saveStateRequest.clear(), loadStateRequest.clear();
    requestOk = ok, requestPending = false;
    requestDone.notify_all();
}

bool request(bool save, const std::string& path) {
    std::unique_lock lock(requestMutex);
    if (!Core::System::GetInstance().IsPoweredOn()) return false;
    (save ? saveStateRequest : loadStateRequest) = path;
    requestPending = true;
    pauseCv.notify_all(); // a paused game still serves requests
    requestDone.wait(lock, [] { return !requestPending || stopRun; });
    return !requestPending && requestOk;
}

} // namespace

extern "C" {

// "<status>\t<program id hex>\t<title>": status is Loader::ResultStatus (0 = Success, 8 = ErrorEncrypted, ...); the id
// and title are empty when the cart couldn't be read that far. null when no loader knows the file at all.
JNIEXPORT jstring JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_probe(JNIEnv* env, jclass, jstring path) {
    std::unique_ptr<Loader::AppLoader> loader = Loader::GetLoader(nativePath(fromJava(env, path)));
    if (!loader) return nullptr;
    u64 programId = 0;
    const Loader::ResultStatus status = loader->ReadProgramId(programId);
    std::string title;
    if (status == Loader::ResultStatus::Success) loader->ReadTitle(title);
    char id[17] = "";
    if (status == Loader::ResultStatus::Success) snprintf(id, sizeof id, "%016llx", (unsigned long long)programId);
    const std::string out = std::to_string(static_cast<int>(status)) + "\t" + id + "\t" + title;
    return env->NewStringUTF(out.c_str());
}

// Once per process, before anything else: Azahar's folders (its emulated SD card and NAND, config, shader caches) under
// userDir, its log to logcat, the input devices, every system module emulated (no console files), Vulkan.
// utils: com.pokedaisy.app.n3ds.N3dsAndroidUtils, whose getUserDirectory() is userDir (set by the caller first).
JNIEXPORT jboolean JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_init(JNIEnv* env, jclass, jstring userDir,
                                                                      jclass utils) {
    if (initialized) return JNI_TRUE;
    const std::string dir = fromJava(env, userDir);
    AndroidUtils::InitJNI(env, static_cast<jclass>(env->NewGlobalRef(utils)));
    FileUtil::SetUserPath(); // first: Azahar sets its paths once; on Android they're "/..." under the user folder
    Common::Log::Initialize();
    Common::Log::Start();
    Common::Log::Filter filter;
    filter.ParseFilterString("*:Warning");
    Common::Log::SetGlobalFilter(filter);

    Settings::values.graphics_api = Settings::GraphicsAPI::Vulkan;
    Settings::values.use_cpu_jit = true;
    Settings::values.is_new_3ds = true;   // more CPU for the game: Pokemon X / Y run on both
    Settings::values.use_disk_shader_cache = true;
    Settings::values.async_shader_compilation = true;
    Settings::values.frame_limit = 100;
    Settings::values.output_type = AudioCore::SinkType::Auto;
    for (int i = 0; i < Settings::NativeButton::NumButtons; i++)
        Settings::values.current_input_profile.buttons[i] = "engine:pk,code:" + std::to_string(i);
    Settings::values.current_input_profile.analogs[Settings::NativeAnalog::CirclePad] = "engine:pk";
    Settings::values.current_input_profile.touch_device = "engine:emu_window";
    for (const auto& module : Service::service_module_map) Settings::values.lle_modules[module.name] = false;
    Input::RegisterFactory<Input::ButtonDevice>("pk", std::make_shared<PkButtonFactory>());
    Input::RegisterFactory<Input::AnalogDevice>("pk", std::make_shared<PkStickFactory>());

    vulkanLibrary = std::make_shared<Common::DynamicLibrary>(dlopen("libvulkan.so", RTLD_NOW));
    initialized = true;
    PKLOG("init: user dir %s", dir.c_str());
    return JNI_TRUE;
}

// The main (0) or second (1) surface, null when it went away.
JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_setSurface(JNIEnv* env, jclass, jint which,
                                                                        jobject surface) {
    if (which < 0 || which > 1) return;
    std::scoped_lock lock(surfaceMutex);
    // Acquire the replacement before releasing the old window. PkWindow uses pointer identity to decide whether its
    // render surface changed; releasing first can let Android reuse the old ANativeWindow address for a new surface,
    // making a detach/reattach look unchanged and leaving Vulkan attached to the old swapchain.
    ANativeWindow* const previous = surfaces[which];
    surfaces[which] = surface ? ANativeWindow_fromSurface(env, surface) : nullptr;
    surfaceCv.notify_all();
    // TOUCH SCREEN attaches this surface on demand in ONE SCREEN. Create its renderer on the game thread at a frame
    // boundary (not here, on UI).
    if (which == 1 && surfaces[which] && !windows[which] && Core::System::GetInstance().IsPoweredOn()) {
        makeSecondaryWindow = true;
    } else if (windows[which] && windows[which]->SetSurface(surfaces[which])) {
        auto& system = Core::System::GetInstance();
        if (system.IsPoweredOn() && surfaces[which]) system.GPU().Renderer().NotifySurfaceChanged(which == 1);
    }
    if (previous) ANativeWindow_release(previous);
}

// Boots path and runs it on this thread until stop(); returns Core::System::ResultStatus (0 = stopped normally).
JNIEXPORT jint JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_run(JNIEnv* env, jclass, jstring jpath,
                                                                 jint oneSurfaceLayout) {
    std::scoped_lock runLock(runMutex);
    const std::string path = nativePath(fromJava(env, jpath));
    auto& system = Core::System::GetInstance();
    stopRun = false;
    paused = false;
    // The surface lock is held through the boot (as Azahar's own front end does): a surface released meanwhile (the
    // screen waking, a lock screen closing) would leave Vulkan making its swapchain from a window that's gone. Vulkan
    // can't make one from no window either: wait for the main one, up to a few seconds.
    std::unique_lock surfaceLock(surfaceMutex);
    makeSecondaryWindow = false;
    if (!surfaceCv.wait_for(surfaceLock, std::chrono::seconds(5), [] { return surfaces[0] != nullptr || stopRun; }) ||
        stopRun) {
        PKLOG("no surface to draw on: not started");
        return static_cast<jint>(Core::System::ResultStatus::ErrorNotInitialized);
    }
    const bool two = surfaces[1] != nullptr;
    // Two surfaces: the top screen alone on the main one, the touch screen alone on the second. With one surface use
    // the requested Azahar layout; ONE SCREEN passes SingleScreen. If TOUCH SCREEN is opened later, setSurface creates
    // Azahar's second window on the emulation thread.
    Settings::values.layout_option = two ? Settings::LayoutOption::SingleScreen
                                         : static_cast<Settings::LayoutOption>(std::clamp(oneSurfaceLayout, 0, 3));
    Settings::values.secondary_display_layout = Settings::SecondaryDisplayLayout::BottomScreenOnly;
    Settings::values.swap_screen = false;
    windows[0] = std::make_unique<PkWindow>(surfaces[0], false);
    windows[1] = two ? std::make_unique<PkWindow>(surfaces[1], true) : nullptr;
    FileUtil::SetCurrentRomPath(path);
    Frontend::RegisterDefaultApplets(system);
    system.ApplySettings();
    const auto status = system.Load(*windows[0], path, windows[1].get());
    if (status != Core::System::ResultStatus::Success) {
        PKLOG("load failed: %d %s", static_cast<int>(status), system.GetStatusDetails().c_str());
        windows[0].reset(), windows[1].reset();
        return static_cast<jint>(status);
    }
    surfaceLock.unlock();
    u64 programId = 0;
    system.GetAppLoader().ReadProgramId(programId);
    system.GPU().ApplyPerProgramSettings(programId);
    std::atomic_bool stopLoading{false};
    system.GPU().Renderer().Rasterizer()->LoadDefaultDiskResources(stopLoading, nullptr);
    system.RegisterCoreLoopThreadId();
    PKLOG("running %016llx", static_cast<unsigned long long>(programId));

    jint result = 0;
    auto lastPerf = std::chrono::steady_clock::now();
    while (!stopRun) {
        if (makeSecondaryWindow.exchange(false)) {
            std::scoped_lock lock(surfaceMutex);
            if (surfaces[1] && !windows[1]) {
                windows[1] = std::make_unique<PkWindow>(surfaces[1], true);
                system.GPU().RecreateRenderer(*windows[0], windows[1].get());
            }
        }
        runRequests(system);
        runReads(system);
        if (paused) {
            std::unique_lock lock(pauseMutex);
            // Polled as well: a request's notify can come between this check and the wait.
            pauseCv.wait_for(lock, std::chrono::milliseconds(50), [] {
                std::scoped_lock r(requestMutex);
                return !paused || stopRun || requestPending || readJob.pending;
            });
            continue;
        }
        const auto r = system.RunLoop();
        // The speed line every 5 s (logcat pokedaisy/n3ds): what to read first when a game runs slow.
        if (const auto now = std::chrono::steady_clock::now(); now - lastPerf > std::chrono::seconds(5)) {
            lastPerf = now;
            const auto stats = system.GetAndResetPerfStats();
            PKLOG("perf: game %.1f fps, system %.1f fps, speed %.0f%%, frame: gpu %.1f ms swap %.1f ms svc %.1f ms "
                  "ipc %.1f ms rest %.1f ms",
                  stats.game_fps, stats.system_fps, stats.emulation_speed * 100, stats.time_gpu * 1000,
                  stats.time_swap * 1000, stats.time_hle_svc * 1000, stats.time_hle_ipc * 1000,
                  stats.time_remaining * 1000);
        }
        if (r == Core::System::ResultStatus::Success) continue;
        if (r == Core::System::ResultStatus::ShutdownRequested) break;
        PKLOG("run loop: %d %s", static_cast<int>(r), system.GetStatusDetails().c_str());
        result = static_cast<jint>(r);
        break;
    }
    {
        std::scoped_lock lock(requestMutex);
        requestPending = false;
        readJob.pending = false;
        requestDone.notify_all();
    }
    system.Shutdown();
    std::scoped_lock lock(surfaceMutex);
    windows[0].reset(), windows[1].reset();
    makeSecondaryWindow = false;
    PKLOG("stopped");
    return result;
}

JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_stop(JNIEnv*, jclass) {
    stopRun = true;
    surfaceCv.notify_all();
    pauseCv.notify_all();
    requestDone.notify_all();
}

JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_setPaused(JNIEnv*, jclass, jboolean p) {
    paused = p;
    pauseCv.notify_all();
}

// The 3DS buttons as Settings::NativeButton bits (A 0, B 1, X 2, Y 3, UP 4, DOWN 5, LEFT 6, RIGHT 7, L 8, R 9,
// START 10, SELECT 11, ZL 14, ZR 15, HOME 16).
JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_setButtons(JNIEnv*, jclass, jint bits) {
    buttonBits = static_cast<u32>(bits);
}

// The circle pad, -1..1 each way, up positive.
JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_setStick(JNIEnv*, jclass, jfloat x, jfloat y) {
    stickX = std::clamp(x, -1.f, 1.f);
    stickY = std::clamp(y, -1.f, 1.f);
}

// A touch on the main (0) or second (1) surface, in that surface's pixels; Azahar maps it to the touch screen where it
// drew it (a touch outside the touch screen is ignored).
JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_touch(JNIEnv*, jclass, jint which, jint x, jint y,
                                                                   jint action) {
    std::scoped_lock lock(surfaceMutex);
    if (which < 0 || which > 1 || !windows[which]) return;
    auto& w = *windows[which];
    const unsigned ux = static_cast<unsigned>(std::max(x, 0)), uy = static_cast<unsigned>(std::max(y, 0));
    if (action == 0) w.TouchPressed(ux, uy);
    else if (action == 1) w.TouchMoved(ux, uy);
    else w.TouchReleased();
}

JNIEXPORT jboolean JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_saveState(JNIEnv* env, jclass, jstring path) {
    return request(true, fromJava(env, path)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_loadState(JNIEnv* env, jclass, jstring path) {
    return request(false, fromJava(env, path)) ? JNI_TRUE : JNI_FALSE;
}

// The 3D picture's resolution, a multiple of the 3DS's own (1 = 400 x 240 on top); 0 = the window's size. Takes effect
// on the next boot.
JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_setResolution(JNIEnv*, jclass, jint factor) {
    Settings::values.resolution_factor = static_cast<u32>(std::clamp(factor, 0, 10));
}

// The game's memory: ranges as (address, length) pairs, copied one after another between two frames; null without a game.
// Blocks until the game thread has done it (a frame at most), so never from the UI thread.
JNIEXPORT jbyteArray JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_readMemory(JNIEnv* env, jclass, jintArray jranges) {
    const jsize n = env->GetArrayLength(jranges);
    // Bound JNI allocations and frame-boundary read time even if a caller passes a
    // malformed range list. Normal telemetry requests use only a handful of ranges.
    constexpr jsize maxRanges = 256;
    constexpr u64 maxBytes = 32ull * 1024 * 1024;
    if (n <= 0 || (n & 1) != 0 || n / 2 > maxRanges) return nullptr;
    std::vector<jint> r(n);
    env->GetIntArrayRegion(jranges, 0, n, r.data());
    u64 totalBytes = 0;
    for (jsize i = 0; i < n; i += 2) {
        const u64 len = static_cast<u32>(r[i + 1]);
        const u64 end = static_cast<u64>(static_cast<u32>(r[i])) + len;
        if (r[i + 1] <= 0 || len > (1u << 20) || end > static_cast<u64>(std::numeric_limits<u32>::max()) + 1)
            return nullptr;
        totalBytes += len;
        if (totalBytes > maxBytes) return nullptr;
    }
    std::vector<u8> out;
    {
        std::scoped_lock caller(readCallers);
        std::unique_lock lock(requestMutex);
        if (!Core::System::GetInstance().IsPoweredOn() || stopRun) return nullptr;
        readJob.ranges.clear();
        readJob.moduleRoot = 0;
        for (jsize i = 0; i + 1 < n; i += 2)
            readJob.ranges.emplace_back(static_cast<u32>(r[i]), static_cast<u32>(std::clamp(r[i + 1], 0, 1 << 20)));
        readJob.pending = true;
        pauseCv.notify_all();
        requestDone.wait(lock, [] { return !readJob.pending || stopRun; });
        if (readJob.pending || !readJob.ok) return nullptr;
        out.swap(readJob.out);
    }
    jbyteArray result = env->NewByteArray(static_cast<jsize>(out.size()));
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(out.size()), reinterpret_cast<const jbyte*>(out.data()));
    return result;
}

// A game's loaded modules and a small set of related memory ranges from the same frame boundary. This keeps screen
// detection and battle-controller reads coherent and avoids queuing a second synchronous JNI request during battle.
JNIEXPORT jobjectArray JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_readObservationNative(JNIEnv* env, jclass, jint root,
                                                                                       jintArray jranges) {
    const jsize n = jranges ? env->GetArrayLength(jranges) : 0;
    constexpr jsize maxRanges = 256;
    constexpr u64 maxBytes = 32ull * 1024 * 1024;
    if (n < 0 || (n & 1) != 0 || n / 2 > maxRanges || root <= 0) return nullptr;
    std::vector<jint> r(static_cast<size_t>(n));
    if (n != 0) env->GetIntArrayRegion(jranges, 0, n, r.data());
    u64 totalBytes = 0;
    for (jsize i = 0; i < n; i += 2) {
        const u64 len = static_cast<u32>(r[i + 1]);
        const u64 end = static_cast<u64>(static_cast<u32>(r[i])) + len;
        if (r[i + 1] <= 0 || len > (1u << 20) || end > static_cast<u64>(std::numeric_limits<u32>::max()) + 1)
            return nullptr;
        totalBytes += len;
        if (totalBytes > maxBytes) return nullptr;
    }
    std::string modules;
    std::vector<u8> out;
    {
        std::scoped_lock caller(readCallers);
        std::unique_lock lock(requestMutex);
        if (!Core::System::GetInstance().IsPoweredOn() || stopRun) return nullptr;
        readJob.ranges.clear();
        readJob.moduleRoot = static_cast<u32>(root);
        for (jsize i = 0; i + 1 < n; i += 2)
            readJob.ranges.emplace_back(static_cast<u32>(r[i]), static_cast<u32>(r[i + 1]));
        readJob.pending = true;
        pauseCv.notify_all();
        requestDone.wait(lock, [] { return !readJob.pending || stopRun; });
        readJob.moduleRoot = 0;
        if (readJob.pending || !readJob.ok) return nullptr;
        modules.swap(readJob.modules);
        out.swap(readJob.out);
    }
    jclass byteArrayClass = env->FindClass("[B");
    if (byteArrayClass == nullptr) return nullptr;
    jobjectArray result = env->NewObjectArray(2, byteArrayClass, nullptr);
    if (result == nullptr) return nullptr;
    const auto toJava = [env](const u8* bytes, size_t size) -> jbyteArray {
        if (size > static_cast<size_t>(std::numeric_limits<jsize>::max())) return nullptr;
        jbyteArray array = env->NewByteArray(static_cast<jsize>(size));
        if (array != nullptr && size != 0)
            env->SetByteArrayRegion(array, 0, static_cast<jsize>(size), reinterpret_cast<const jbyte*>(bytes));
        return array;
    };
    jbyteArray moduleBytes = toJava(reinterpret_cast<const u8*>(modules.data()), modules.size());
    jbyteArray memoryBytes = toJava(out.data(), out.size());
    if (moduleBytes == nullptr || memoryBytes == nullptr) return nullptr;
    env->SetObjectArrayElement(result, 0, moduleBytes);
    env->SetObjectArrayElement(result, 1, memoryBytes);
    env->DeleteLocalRef(moduleBytes);
    env->DeleteLocalRef(memoryBytes);
    env->DeleteLocalRef(byteArrayClass);
    return result;
}

// Speed in percent of real time; 0 = as fast as it goes.
JNIEXPORT void JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_setSpeed(JNIEnv*, jclass, jint percent) {
    Settings::values.frame_limit = static_cast<double>(std::max(percent, 0));
}

// Frames presented on the main surface so far, and the game's frames a second (Azahar's own measure).
JNIEXPORT jlong JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_frames(JNIEnv*, jclass) {
    std::scoped_lock lock(surfaceMutex);
    return windows[0] ? static_cast<jlong>(windows[0]->frames.load()) : 0;
}

JNIEXPORT jdouble JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_gameFps(JNIEnv*, jclass) {
    auto& system = Core::System::GetInstance();
    if (!system.IsPoweredOn() || !system.perf_stats) return 0;
    return system.perf_stats->GetLastStats().game_fps;
}

} // extern "C"

// ---- Screenshots (a state's thumbnail) ----------------------------------------------------------------------------

// Azahar writes a screenshot into its buffer at the next frame it presents, on the game thread; the buffer stays ours
// (global) so one that lands after the caller gave up writes into live memory. One at a time, as Azahar allows.
std::mutex shotMutex;
std::condition_variable shotCv;
std::vector<u32> shotBuf;
bool shotDone = false;
bool shotInvert = false;

// The top screen of the next frame at [w] x [h] (ARGB, top row first); null without a running game, while another
// screenshot is still pending, or when no frame came within a second (a paused game). Blocks: never the UI thread.
extern "C" JNIEXPORT jintArray JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_screenshot(JNIEnv* env, jclass, jint w, jint h,
                                                                             jboolean bottom) try {
    if (w <= 0 || h <= 0 || w > 4096 || h > 4096) return nullptr;
    auto& system = Core::System::GetInstance();
    std::unique_lock lock(shotMutex);
    {
        std::scoped_lock r(requestMutex);
        if (!system.IsPoweredOn() || stopRun || paused) return nullptr;
        auto& renderer = system.GPU().Renderer();
        if (renderer.IsScreenshotPending()) return nullptr;
        shotBuf.assign(static_cast<size_t>(w) * h, 0);
        shotDone = false;
        renderer.RequestScreenshot(
            shotBuf.data(),
            [](bool invertY) {
                std::scoped_lock l(shotMutex);
                shotInvert = invertY;
                shotDone = true;
                shotCv.notify_all();
            },
            Layout::SingleFrameLayout(static_cast<u32>(w), static_cast<u32>(h), bottom == JNI_TRUE, false));
    }
    if (!shotCv.wait_for(lock, std::chrono::seconds(1), [] { return shotDone; })) return nullptr;
    std::vector<jint> out(shotBuf.size());
    for (jint y = 0; y < h; y++) {
        const size_t from = static_cast<size_t>(shotInvert ? h - 1 - y : y) * w;
        for (jint x = 0; x < w; x++)
            out[static_cast<size_t>(y) * w + x] = static_cast<jint>(shotBuf[from + x] | 0xFF000000u);
    }
    jintArray result = env->NewIntArray(static_cast<jsize>(out.size()));
    if (!result) return nullptr;
    env->SetIntArrayRegion(result, 0, static_cast<jsize>(out.size()), out.data());
    return result;
} catch (const std::exception& error) {
    PKLOG("screenshot failed: %s", error.what());
    return nullptr;
} catch (...) {
    PKLOG("screenshot failed with an unknown native exception");
    return nullptr;
}

// One atomic render-target readback of both 3DS displays, laid out for a transition overlay. The 400x240 top
// screen is left of the 320x240 touch screen; Vulkan draws both rectangles from the same emulated frame.
extern "C" JNIEXPORT jintArray JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_screenshotPairNative(JNIEnv* env, jclass) try {
    constexpr jint width = 720;
    constexpr jint height = 240;
    auto& system = Core::System::GetInstance();
    std::unique_lock lock(shotMutex);
    {
        std::scoped_lock r(requestMutex);
        if (!system.IsPoweredOn() || stopRun || paused) return nullptr;
        auto& renderer = system.GPU().Renderer();
        if (renderer.IsScreenshotPending()) return nullptr;
        shotBuf.assign(static_cast<size_t>(width) * height, 0);
        shotDone = false;
        Layout::FramebufferLayout layout{};
        layout.width = width;
        layout.height = height;
        layout.top_screen_enabled = true;
        layout.bottom_screen_enabled = true;
        layout.top_screen = {0, 0, 400, 240};
        layout.bottom_screen = {400, 0, width, height};
        layout.is_rotated = true;
        renderer.RequestScreenshot(
            shotBuf.data(),
            [](bool invertY) {
                std::scoped_lock l(shotMutex);
                shotInvert = invertY;
                shotDone = true;
                shotCv.notify_all();
            },
            layout);
    }
    if (!shotCv.wait_for(lock, std::chrono::seconds(1), [] { return shotDone; })) return nullptr;
    std::vector<jint> out(shotBuf.size());
    for (jint y = 0; y < height; y++) {
        const size_t from = static_cast<size_t>(shotInvert ? height - 1 - y : y) * width;
        for (jint x = 0; x < width; x++)
            out[static_cast<size_t>(y) * width + x] = static_cast<jint>(shotBuf[from + x] | 0xFF000000u);
    }
    jintArray result = env->NewIntArray(static_cast<jsize>(out.size()));
    if (!result) return nullptr;
    env->SetIntArrayRegion(result, 0, static_cast<jsize>(out.size()), out.data());
    return result;
} catch (const std::exception& error) {
    PKLOG("paired screenshot failed: %s", error.what());
    return nullptr;
} catch (...) {
    PKLOG("paired screenshot failed with an unknown native exception");
    return nullptr;
}

// The names of the modules loaded after the static module whose header is at [root] (ReadJob::moduleRoot), one per
// line; null without a game. Blocks for a frame at most: never the UI thread.
extern "C" JNIEXPORT jstring JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_modules(JNIEnv* env, jclass, jint root) {
    std::string out;
    {
        std::scoped_lock caller(readCallers);
        std::unique_lock lock(requestMutex);
        if (!Core::System::GetInstance().IsPoweredOn() || stopRun) return nullptr;
        readJob.ranges.clear();
        readJob.moduleRoot = static_cast<u32>(root);
        readJob.pending = true;
        pauseCv.notify_all();
        requestDone.wait(lock, [] { return !readJob.pending || stopRun; });
        readJob.moduleRoot = 0;
        if (readJob.pending || !readJob.ok) return nullptr;
        out.swap(readJob.modules);
    }
    return env->NewStringUTF(out.c_str());
}

// Debug builds: [data] written at [addr] in the game's memory between two frames (finding things on a device). Blocks
// for a frame at most; false without a game.
extern "C" JNIEXPORT jboolean JNICALL Java_com_pokedaisy_app_n3ds_N3dsNative_writeMemory(JNIEnv* env, jclass, jint addr,
                                                                                    jbyteArray jdata) {
    const jsize n = env->GetArrayLength(jdata);
    std::vector<u8> data(static_cast<size_t>(n));
    env->GetByteArrayRegion(jdata, 0, n, reinterpret_cast<jbyte*>(data.data()));
    std::scoped_lock caller(readCallers);
    std::unique_lock lock(requestMutex);
    if (!Core::System::GetInstance().IsPoweredOn() || stopRun || n == 0) return JNI_FALSE;
    readJob.ranges.clear();
    readJob.moduleRoot = 0;
    readJob.writes.clear();
    readJob.writes.emplace_back(static_cast<u32>(addr), std::move(data));
    readJob.pending = true;
    pauseCv.notify_all();
    requestDone.wait(lock, [] { return !readJob.pending || stopRun; });
    return readJob.pending ? JNI_FALSE : JNI_TRUE;
}
