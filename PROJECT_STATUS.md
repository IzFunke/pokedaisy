# PokeDaisy 3DS Project Status

Development is on `feature/azahar-3ds-single-screen` in the personal fork `IzFunke/pokedaisy` (`origin`). The official repository is `upstream`; push defaults target `origin`.

## Verified

- Fork, remotes, branch and submodules are configured. Azahar is pinned at `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2).
- The checkout already contains feature-flagged Azahar JNI integration and Pokémon X/Y single-screen support. This work extends and validates existing integration rather than recreating it.
- Baseline `assembleDebug` succeeded. `assembleDebug -Ppokedaisy.n3ds=true` and `assembleDev -Ppokedaisy.n3ds=true` succeeded on Windows x64 with NDK 28.2.13676358.
- Production/device Dev APKs contain `lib/arm64-v8a/libpk_n3ds.so`. An opt-in x86_64 build (`-Ppokedaisy.n3dsX86_64=true`) now also packages Azahar for emulator runtime checks; the Galaxy S25 Ultra uses ARM64. The package remains isolated as `com.pokedaisy.app.dev`.
- On the Galaxy S25 Ultra, `LibraryActivity` resumed and no fatal exception or native load error appeared in the captured startup log.
- The latest ARM64 Dev APK was installed on the S25 Ultra with `adb install -r`; the app data directory and Dev preferences remained intact (identical preference-file SHA-256 before/after, `setup_done` and `last_rom` present). The game was not relaunched during that update.
- The x86_64 translation AVD aborts with SIGILL during startup; it is not a viable Azahar runtime target.
- The opt-in API 36 x86_64 AVD boots the user's Pokémon Y test copy. The closed `ONE SCREEN` view shows the top surface; opening the touch window creates the lower Azahar surface and displays the actual in-game language help; closing it returns to top-only output. The user-authorized German choice was confirmed and the game was left at `Begin game` / `Select language`, before personal setup. Cross-process language persistence is unverified.
- An API 36 follow-up disproved the earlier composition-order fix: `SurfaceView` alpha still leaked the second display. The current change uses a `TextureView` for Azahar's touch surface, leaving the top `SurfaceView` unchanged. Ultra Moon now boots with one native SurfaceView visible while closed; Azahar continues rendering at about 30 game FPS / 60 system FPS. The TOUCH SCREEN toggle opened and closed without a crash, but its content appeared black on the post-language settings message, so normal in-game touch-screen output still needs verification. In landscape, the top 5:3 game display filled the available center region. German was selected as authorized; the emulator was left before character/profile selection. The focused Dev build and stage/cart/save tests pass.
- The S25 Dev package remains installed at versionCode 23; it was not updated in this follow-up, and no app data was changed.
- Read-only S25 properties report model `SM-S938B`, Android 17 / API 37. It includes the API 36 `SurfaceView` composition-order API used by the fix, but the new APK has not been installed or visually checked on the phone in this follow-up.
- The API 36 ARM64 AVD image exists, but Android Emulator 37.2.13 refuses to launch it on this Windows x86_64 host (`arm64` guest architecture unsupported by QEMU2 here). ARM64 runtime evidence must come from compatible hardware; the S25 was not used or updated in this follow-up.
- Official v1.6.2-beta.1 APK used for reference was checksum-verified against its published GitHub release digest; the original file was preserved.
- Synthetic N3DS reader/routing tests and X/Y single-screen Paparazzi screenshot tests pass. On the latest focused rerun, 6 ROM-dependent ORAS/Sun/Moon cart tests were skipped because those images are absent from this checkout; the synthetic Sun/Moon reader tests passed. Ultra Moon has separate S25 Ultra launch evidence below. A new `N3dsNativeInitTest` instrumentation smoke check also passes on physical ARM64 hardware and is repeatable with `make n3ds-smoke`.
- The user's European Pokémon Y and Ultra Moon images have the same title IDs used by routing. `N3dsCart` constants keep their historical `_USA` suffix; European Sun/Moon and Ultra Sun dumps are not available here for equivalent ROM-header verification.
- Full `:app:testDebugUnitTest -Ppokedaisy.n3ds=true` suite passes: 4,207 tests, 0 failures, 60 skipped across 191 classes (2026-11-22). The website export test normalizes Windows CRLF before comparison. See `TEST_RESULTS.md`.
- `git diff --check` passes for the current changes.

## Current change

The pushed checkpoint fixes `make submodules` and the developer setup instructions to initialize DSperate, which the Android app build and license asset require but the old setup command omitted. It also adds project status, architecture, research, decision, issue, test and next-step documentation.

Commit `89c80ab2` adds USUM retail title recognition and Sun/Moon-style presentation without claiming USUM companion memory support, plus Settings > FOLDERS options for one shared DS save folder and an Azahar 3DS user-data root. DS saves are copied (not moved) when switching to a shared folder. Selecting an empty 3DS root copies the old Azahar tree first. Focused tests and a Dev build pass. The Dev APK was updated on the Galaxy S25 Ultra with `adb install -r`; the regular app was not modified. Documentation updates, including the later Pokémon Y and Ultra Moon device findings, are pushed through commit `5d7d4069`.

The official app's private settings have not been copied into Dev. The installed official package is non-debuggable and declares `allowBackup=false`, so Android blocks `run-as` and backup access to its preferences; its Settings screen and source expose no preferences export action. Dev's existing data remains preserved by in-place installation.

## 2026-11-22 follow-up

- Added NCSD title-ID detection to linked-folder scanning so Pokémon Sun, Moon, Ultra Sun and Ultra Moon appear in the library even without a companion RAM map. The ROM cache stamp changes to rescan entries written by earlier builds. Ultra Moon appeared with `NOT SUPPORTED` on the API 36 x86_64 emulator, and direct `LaunchActivity` launch booted title ID `00040000001b5100`.
- The API 36 emulator ran Ultra Moon at about 29.9 game FPS / 59.8 system FPS after warm-up. No language was selected; this setup session is still at the first language prompt.
- The same screenshot showed the 4:3 touch surface leaking into the supposedly closed ONE SCREEN layout despite composition ordering. Touching the on-screen D-pad did not move the language cursor. Single-screen hiding and input are therefore not verified as working in this build and remain open defects. The S25 APK was not updated during this follow-up.
- Focused unit tests for companion library detection, linked-folder caching, 3DS cart routing and saves pass: 19 tests, 0 failures, 2 expected skips. Details in `TEST_RESULTS.md`.


The user later reported that the 3DS view still appeared dual-screen and that 3DS ROMs no longer started on the S25 Ultra. The DS menu tracker override was restricted to DS games, and 3DS keeps its selected layout. Emulator runtime work then exposed two more causes: a hidden touch-screen `SurfaceView` could block Azahar from starting in ONE SCREEN, and the single-surface core layout was Azahar's `Default` (both screens), not `SingleScreen`. The boot now waits only for a touch surface that is visible at launch, and the one-surface mode selects Azahar's `SingleScreen` layout.

The follow-up surface leak was caused by Android compositing the `SurfaceView` independently of its parent alpha. On API 36, the touch surface now has an explicit non-negative composition order before attachment; visual closed/open/closed checks show top-only output while closed and the real 4:3 touch screen when requested. Older Android versions still need an equivalent verified hide/show path.

An opt-in x86_64 Azahar build now runs on the API 36 emulator. Ultra Moon boots to its first language-selection screen; logs report title ID `00040000001b5100` and steady 29.8–30.0 game FPS / 59.8 system FPS after shader warm-up. In ONE SCREEN, the capture shows only the 5:3 top screen. L2 opens the touch screen as a floating overlay and closes it again; creating its Android `SurfaceView` after boot now adds Azahar's second window on the emulation thread and recreates the renderer. Performance returned to 29.5–30.0 game FPS / 58.7–59.8 system FPS after the renderer switch. The emulator remains at the initial language-selection prompt; no personal game setup choice was made. Each emulator update used `adb install -r`, preserving app data. The S25 Ultra was not touched during this follow-up.

## Runtime validation and remaining gaps

The initial inventory preceded completion of the user's dump. A user-owned European Pokémon Y `.3ds` image is now present in the designated directory. Its NCSD header and title ID (`0x0004000000055E00`) were checked read-only; the original was left untouched. A temporary device copy in a dedicated Download subfolder was linked in Dev. On the Galaxy S25 Ultra, the actual title booted into its first language-selection screen and rendered at about 59.8 FPS / 100% speed. The user selected and confirmed German; the game moved into its logo/intro sequence. After a process restart before reaching gameplay/save setup, the language prompt appeared again, so persistence of the language choice is not verified. German has been confirmed again in the current active session, which remains at the logo/intro; no later character/setup choice was made. Graphics/title boot and D-pad input are verified; audio, broader gameplay controls, battle/field behavior, save persistence, pause/resume, and long-session stability remain unverified.

The user's European Pokémon Ultra Moon image was inspected read-only (valid NCSD, decrypted, title ID `0x00040000001B5100`), copied to the S25 validation directory, and launched on the Galaxy S25 Ultra. The user chose German and advanced through the opening dialogue to Kukui's first player-profile photo question; no personal profile choice was made. The title-ID save path contains a 445,440-byte `main` file and 16-byte `00000001.metadata` sibling; its bytes confirm `(0x40000, 1 directory, 1 file, duplicate=true)`. A separate emulator runtime check now also boots the same title to language selection and verifies the actual single-screen frame. Ultra Sun shares retail routing and identity mapping but has not been launched; its save format is not guessed without live evidence.

A read-only filename check of the user's designated host ROM folder and Dev ROM folder found Pokémon Y and Ultra Moon, but no Ultra Sun image. Ultra Sun's hardware boot check therefore remains pending on the user's own dump becoming available.

Copying settings from the installed official package requires an official settings-export path or access Android does not grant on this device.

## Git state

Checkpoints `64e20368` (setup and project docs), `34eec0a6` (ARM64 JNI smoke test), `89c80ab2` (USUM routing and shared handheld save settings), `5d7d4069` (Pokémon Y device findings), and `7be978ba` (x86_64 runtime, true single-screen boot, and dynamic touch-screen window) are pushed to `origin/feature/azahar-3ds-single-screen`. No upstream PR has been opened. Keep the goal active until game/runtime behavior, compatibility, and release evidence are established.

On 2026-11-22, activity UI commit `0a2fcc62` built and passed focused `DsStagePolicyTest`, `N3dsCartTest`, `N3dsSavesTest`, and `OrasSmCartTest`; the opt-in Dev APK was installed on the x86_64 AVD with `adb install -r`. Setup preferences and the last ROM remained present. The commit is pushed to `origin`; the untracked `work/` directory contains local test artifacts and was not staged.

Commit `e05727a2` fixes the API 36 `SurfaceView` composition alpha leak that made ONE SCREEN appear dual-screen, and records the closed/open/closed emulator check plus the below-API-36 and ARM64 device validation gaps. It is pushed to `origin/feature/azahar-3ds-single-screen`; no upstream PR was opened. The latest x86_64 Dev installation used `adb install -r` and retained `setup_done`, `last_rom`, and the existing layout preference.

Commit `e75b457a` records the follow-up Ultra Moon boot on the updated API 36 emulator, stopped at the unconfirmed language selector. A read-only package query confirmed `com.pokedaisy.app.dev` remains installed on the S25 Ultra at versionCode 23; this follow-up did not update the phone.

## 2026-11-22 DS-style 3DS screen follow-up

- The user requested 3DS touch-screen presentation to behave like PokeDaisy's DS presentation. The 3DS uses the saved DS layout for touch-screen placement, opens its touch display automatically on mapped field menus and battle bag/party states, shows both screens side by side for mapped scenes that need both displays, and supports manual framed opening/dismissal in ONE SCREEN and PIP. On an attached external display, the touch render is kept visible and stays routed to the DS-style bottom-screen host instead of being hidden by ONE SCREEN or pulled back into the main stage during scene updates. This routing change compiles but still needs hardware validation.
- SETTINGS exposes the shared DS TOUCH SCREEN / DS LAYOUT controls for 3DS builds, and a 3DS CORE chooser. Azahar is the only bundled 3DS backend: AUTO resolves to Azahar, while AZAHAR explicitly chooses it.
- The API 36 x86_64 emulator was updated only with `adb install -r`. Ultra Moon's setup and intro were advanced on German to the `Wie heißt du?` player-name entry. No name or profile choice was made. Lower-screen keyboard input and the new battle auto-window states still need live verification.
- `:app:assembleDev` with `-Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true` passed, and the resulting APK was installed with `adb install -r` on emulator-5558. No unit-test task was run for this follow-up. The S25 Ultra app/data were untouched.
- At the seven-hour deadline the restore task ran: S25 `lock_screen_lock_after_timeout=5000` and `screen_off_timeout=300000`; the scheduled task and its local PowerShell/log files are absent.

### 2026-11-22 latest layout follow-up

- Adjusted the 3DS touch window frame so ONE SCREEN shows a frame only while the window is open; PIP retains its always-visible inset frame. Added regression coverage for both states.
- Focused policy, Sun/Moon reader, and cart routing tests passed. The x86_64 Dev APK built successfully and was installed on emulator-5558 with adb install -r.
- Initial diagnostics showed an invisible touch `TextureView` was incorrectly required, leaving Ultra Moon at the loading presentation. The startup policy now waits only for a visible or second-display touch surface; a subsequent run booted title `00040000001b5100` and passed the closed/open/closed visual check on emulator-5558.
- The S25 Ultra was updated with `adb install -r` only. `setup_done=true` and the pre-existing `last_rom` remained in its private preferences; no uninstall or data clear was performed.
- Focused tests and the x86_64 Dev build passed after this fix. The S25 display was not visually inspected. No upstream PR was opened.

## 2026-11-22 Black 2 / Ultra Moon comparison and follow-up

The active branch is still `feature/azahar-3ds-single-screen`, at `61fdd8ea`; `origin` is the personal fork and `upstream` is the official repository. Local status before this work had only the pre-existing untracked `work/` artifacts. The S25 Ultra (`SM-S938B`, Android 17/API 37) still has the Dev package installed; this work has not uninstalled the app or cleared its data.

| Capability | Pokémon Black 2 reference | Ultra Moon on S25 | Current implementation / gap | Evidence status |
|---|---|---|---|---|
| Core and screen | melonDS/DS path, top screen full-size in ONE SCREEN | Azahar boots and renders the 5:3 top display in ONE SCREEN | Both paths exist; 3DS renderer boot is verified | Device captures, Black 2 and Ultra Moon screenshots |
| Companion initialization | Gen 5 telemetry publishes the common `SnapshotView` | Companion remained at `LÄDT … startet…` | `N3dsSession.sampler` publishes only inside `config != null`; USUM has no `Gen6Reader.Config` | Source path and S25 screenshot confirm the cause |
| Runtime state and party | `Poller.readGen5Telemetry` reads identified game state and party data | No party, map, item, battle or scene sample | Need title-specific USUM memory map, then reuse common Companion presentation | Not implemented for USUM |
| Automatic lower screen | DS core frame flags drive `dsTouchWanted`; menus open the lower-screen window and it hides afterward | USUM scene remains default `FIELD`; the scene loop does not run with null config | Existing 3DS scene policy is ready, but it requires verified memory addresses; currently only manual TOUCH SCREEN works | The exact gate is confirmed in `N3dsSession` / `Gen6Reader` |
| Touch mapping | Existing DS touch renderer and core coordinate path | Azahar lower-screen view and touch path exist; S25 menu/tap correctness has not been checked in a live touch-dependent screen | Verify on S25 in a known touch screen after a title memory map exists | Not yet verified on S25 |
| Battle menus | Gen 5 battle telemetry and DS menu state drive context UI | No USUM battle state; no auto bag/party opening | USUM battle readers and real battle verification are missing | Not implemented for USUM |
| Reddit X field HUD / map / camera | Not applicable to DS | Shared 3DS HUD paths exist, but USUM's missing telemetry leaves them without live data | Verify with Pokémon Y/X and extend only after title-specific data exists | Feature parity not claimed |
| Dismiss auto-open battle page | DS has core-dependent dismiss behavior | A 3DS auto-open bag/party page's dim backdrop did not send B | Fixed to send B only for mapped battle bag/party states; covered by focused policy test | Code change; device verification awaits mapped battle state |

The root cause is not a generic Azahar rendering failure: the USUM title boots, but `Gen6Reader.configFor()` returns null by design, which prevents both snapshot publication and scene polling. The game-specific map must be recovered and validated before automatic menus or party/battle data can work. No Sun/Moon address was copied into USUM.

An additional truthful-status change now publishes a dedicated Companion message for an unmapped 3DS title instead of leaving the generic startup text indefinitely. The message explains that game data is not mapped and points to the manual TOUCH SCREEN fallback. A Linux `n3ds_dump` build using the existing Azahar submodule is running in a local Docker container to inspect the user's own Ultra Moon image; it uses a scratch user directory and does not touch the original ROM or live save.
# 2026-11-22: Ultra Moon EU headless startup verification

The native Azahar capture target now builds successfully in the existing Linux Docker builder. It boots the user's EU
Ultra Moon cart from a read-only ROM mount in an isolated scratch user directory. The title ID is `00040000001b5100`;
the language chooser uses a static CRO root at `0x008b2000` with module `LangSelect`. This proves boot and the early
module root only. Ultra Moon field/battle/party offsets, automatic 3DS lower-screen behavior, and full Companion parity
remain incomplete. The production S25 app and its data were not touched during this headless capture.

The full Dev UnitTest suite subsequently passed: 4,213 tests, 0 failures, 0 errors, 60 skipped across 191 suites. The
ARM64 Dev native library and APK also built successfully. The S25 Dev app was updated with `adb install -r` while the
device was at the library; `setup_done=true` and the existing `last_rom` preference remained. No uninstall, data clear,
or game-input action was used, and the app was not forced to the foreground.

## 2026-11-22: Ultra Moon language-selector screen detection

The headless Ultra Moon savestate was reloaded in a fresh diagnostic process; a module query at `0x008b2000` returned
`LangSelect`. `N3dsSession` now polls that observed root for Ultra Moon and automatically selects the paired screen layout
only while `LangSelect` is present. Other modules are `UNMAPPED`, preserving the selected layout and manual touch-screen
fallback. No USUM field, battle, party, or Companion mapping is inferred from Sun/Moon. The focused `OrasSmCartTest`
passed and `:app:assembleDev -Ppokedaisy.n3ds=true` passed. The resulting Dev APK was installed on the S25 with
`adb install -r`; `setup_done`, the existing `last_rom` entry, and the shared save-folder preference were retained.
The phone was locked during install, so this automatic transition has not yet been visually verified on hardware.

## 2026-11-22: Ultra Moon intro on x86_64 emulator

After the user-authorized German language choice, the API 36 x86_64 emulator was advanced through the Ultra Moon intro. At the Kukui photo/profile setup view, input stopped before selecting or confirming a photo or profile. The live CRO query reports `AppDemo`, `SequencePlayer`, and `Object`; `N3dsSession` still labels this as `UNMAPPED`, as intended. The Companion therefore remains on the explicit unmapped-data message, and no new USUM memory mapping can be claimed.

The emulator log during the intro reported roughly 17–18 game FPS and 33–36 system FPS (about 55–62% speed), falling lower around the photo view. This is x86_64 emulator evidence, not an S25 Ultra performance measurement. The RAM capture is preserved under the ignored/untracked `work/` directory; no ROM or save data was changed.

The debug capture path now lives in app-private `files/n3ds-debug`, which makes its marker and output accessible to `adb run-as`; debug snapshots for Gen 7 retail titles capture the first 16 MiB at `0x30000000` even when the title has no `Gen6Reader.Config`. The capture scheduler no longer depends on a non-null game config. This improves diagnostics only and does not enable a guessed USUM Companion map.

## 2026-11-22 Ultra Moon static data follow-up

Using the user's local Ultra Moon EU image read-only, corrected USUM's German text-table indices and its island-map archive location (`a/2/3/8`). The layout-aware static readers now pass against the ROM for labels, moves, species types, zones, wild encounters, icons, and island map extraction. Focused USUM tests and the ARM64+x86_64 feature-enabled Dev build pass. The photo/profile prompt remains untouched. Live USUM party/battle/scene mapping and DS-level automatic second-screen/Companion parity are still open.

## Ultra Moon intro screen routing (2026-11-22)

A live x86_64 Azahar module snapshot at Kukui's photo/profile setup contained exactly `AppDemo`, `SequencePlayer`, and `Object` (also seen during the intro). These modules were previously treated as unknown, leaving the lower display manual in ONE SCREEN. `Gen6Reader.startupSceneOf` now recognizes that complete observed group as a screen scene alongside `LangSelect`; partial/unrelated groups remain `UNMAPPED`. Existing stage policy then temporarily arranges the top and lower displays side by side for these setup screens, following the DS menu behavior. The setup choice itself was not confirmed or changed.

The focused startup-scene and stage-policy tests pass, and the feature-enabled ARM64+x86_64 Dev APK builds successfully with the Ultra Moon observed setup-module mapping. The running emulator was not updated or sent input, preserving the photo/profile checkpoint.
