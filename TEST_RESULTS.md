# Test Results

Date: 2026-11-21

## Build checks

- `:app:assembleDebug`: PASS (baseline, feature disabled).
- `:app:assembleDebug -Ppokedaisy.n3ds=true`: PASS; Azahar ARM64 CMake target compiled and APK packaged.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS; isolated dev APK packaged.
- APK inspection: PASS; package `com.pokedaisy.app.dev`, native ABI `arm64-v8a`, includes `libpk_n3ds.so`.
- `:app:connectedDevAndroidTest -Ppokedaisy.n3ds=true -Pandroid.testInstrumentationRunnerArguments.class=com.pokedaisy.app.n3ds.N3dsNativeInitTest`: PASS on Galaxy S25 Ultra, Android 17. The instrumentation test loads `libpk_n3ds.so` and successfully calls `N3dsNative.init` using a private app directory. This is Android/JNI setup validation only; it does not initialize a game or render a frame.
- `git diff --check`: PASS before the checkpoint commit; re-run before the next commit.

## Device startup

- Galaxy S25 Ultra, ARM64 Android 17: dev package installed without replacing `com.pokedaisy.app`; `LibraryActivity` resumed; captured startup logs showed no fatal exception, SIGSEGV, or `UnsatisfiedLinkError`.
- x86_64/API 30 translation AVD: package installation succeeded but startup aborted with SIGILL. FAIL / incompatible runtime. This does not validate Azahar on x86.

## Focused 3DS tests

- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests "com.pokedaisy.app.n3ds.N3dsCartTest"`: PASS.
- Same task filtered to `Gen6Test`, `N3dsBattleTapsTest`, `OrasSmCartTest`, and `SmBattleDumpTest`: PASS.
- Same task filtered to `N3dsBattleOneScreenScreenshotTest`, `XyCompanionScreenshotTest`, and `XyFieldHudScreenshotTest`: PASS. These are Paparazzi screenshot tests of the existing single-screen UI, not hardware gameplay checks.

## Full app test suite

Command: `:app:testDebugUnitTest -Ppokedaisy.n3ds=true`.

Gradle completed after 11m42s: 4,194 tests completed, 17 failed, 59 skipped. Failures were in `BpsPatchTest`, `FfMusicBudgetTest`, `FlacWriterTest` (7 cases), `PerformanceGuardsTest`, `RomFolderTest` (4 cases), `RzipSaveTest`, `SaveFileEditsTest`, and `SiteDataExportTest`. Several failures report Windows path/rename behavior, and the compatibility data fixture is stale. The focused N3DS and single-screen suites passed. These broader failures remain in the baseline tree and should be triaged separately; this run does not establish a clean full-suite baseline.

## Game checks

The initial inventory occurred before the user's Pokémon Y dump was available. Afterward, the user's European Pokémon Y image was inspected read-only and a temporary copy was linked from a dedicated Download subfolder in Dev. On the Galaxy S25 Ultra, the actual game booted to its language-selection screen and rendered at about 59.8 FPS / 100% speed in the observed session. The user chose and confirmed German; the game advanced into its logo/intro sequence. After a process restart before reaching gameplay/save setup, the game returned to language selection; German has been confirmed again in the current active session. The original ROM was not modified. This verifies title boot, graphics, D-pad navigation, and accepted language input, but not persistence of that language choice, later gameplay, audio, save persistence, broad controls, or stability.

The user's European Ultra Moon image was checked read-only (decrypted NCSD, title ID `0x00040000001B5100`), copied to a dedicated device test folder, and launched on the same phone. The user chose German, and the game advanced through Kukui's opening dialogue to the first player-profile photo question (“Welches dieser Fotos stammt gleich noch mal von dir?”); no profile or character choice was made. Azahar logs reported the expected title ID and, after loading, about 29.8–30.0 game FPS / 59.8 system FPS at 100% speed. A 445,440-byte `main` file is present in the title-ID save path (mtime 23:12 local); it is the only file in that title's data directory and has no companion metadata file. This does not prove an in-game save, persistence, or reload. German-choice persistence is also unverified because the game has not been restarted since the choice. Audio, gameplay after setup, companion polling, and longer stability remain unverified. Ultra Sun was not available for the hardware boot check.

## Current save-folder and title-routing changes

- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests "com.pokedaisy.app.SavesLocationTest" --tests "com.pokedaisy.app.n3ds.N3dsCartTest" --tests "com.pokedaisy.app.n3ds.OrasSmCartTest"`: PASS (2026-11-21). Includes DS shared-folder precedence and USUM recognized-but-not-companion-supported checks.
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest --rerun-tasks`: PASS (2026-11-21). It rebuilt selected JVM dependencies; `N3dsCartTest` had 4 pass / 1 ROM-dependent skip, and `OrasSmCartTest` had 2 pass / 6 ROM-dependent skips. The passing cases verify synthetic NCSD/NoCrypto routing for both Ultra IDs, Sun/Moon family classification/layout, and deliberate absence of USUM companion configs. The skipped cases require retail X/ORAS/Sun/Moon ROM files that are not present in this checkout's test directory.
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.n3ds.SmReaderTest`: PASS (2026-11-21), all 6 synthetic-memory tests passed for Sun/Moon scene, bag, battle, camera and pointer-chain reads. These exercise reader logic with constructed bytes, not live Sun/Moon RAM.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS (2026-11-21).
- Galaxy S25 Ultra Dev package updated with `adb install -r` and launched. No uninstall or app-data clearing was performed.
- Official-to-Dev preference transfer was not possible: official package is not debuggable (`run-as` denied) and app manifest disables Android backup (`allowBackup=false`).

## 3DS single-screen portrait follow-up

- 2026-11-22 focused routing rerun: `N3dsCartTest` (4 pass / 1 ROM-dependent skip), `OrasSmCartTest` (2 pass / 6 ROM-dependent skips), `N3dsBattleTapsTest` (3 pass), and `DsStagePolicyTest` (3 pass). No failures. USUM checks verify launch classification and shared Sun/Moon battle taps while explicitly asserting that Ultra Sun / Ultra Moon do not yet have companion memory configs.
- `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon`: PASS (APK 84,293,811 bytes, built 2026-11-22). `adb install -r app/build/outputs/apk/dev/app-dev.apk` then returned `Success`; installed package `com.pokedaisy.app.dev` remains versionCode 23 / versionName 1.6.2-beta.1 (lastUpdateTime 2026-11-22 00:17:07).
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.DsStagePolicyTest`: PASS (3 tests). 3DS stages are enabled in portrait and landscape; portrait DS remains unchanged; second-screen, debug-mirror, companion-over-game and fold cases remain excluded.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS (2026-11-22), APK packaged at `app/build/outputs/apk/dev/app-dev.apk` (84,293,811 bytes).
- On the Galaxy S25 Ultra, the Dev package data directory, ROM index, selected ROM folders, and `pokedaisy.xml` remained present after the in-place update; the app returned to its library without setup. No app data was cleared and the package was not uninstalled. A follow-up Ultra Moon start reached `PokeDaisyActivity` and loaded `libpk_n3ds.so`, but did not render a game frame before Android recorded process exit reason `USER REQUESTED` / subreason `REMOVE TASK` at 2026-11-22 00:28:18. This does not establish an emulator crash or a successful game boot. No personal profile option was selected; visual verification remains pending.
- Source review found why `ONE SCREEN` could still look like dual-screen: a detected 3DS `SCREEN` scene changed its arrangement to side-by-side. Removed that implicit override so `ONE SCREEN` consistently displays only the top game screen; manual touch-screen access and explicit side-by-side layouts remain.
- 2026-11-22 regression check: `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.N3dsBattleTapsTest --offline --no-daemon --console=plain`: PASS. `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain`: PASS. Installed the resulting APK with `adb install -r`; Android returned `Success`, package update time is 00:39:48, and `shared_prefs/pokedaisy.xml` remains present. No game relaunch was done after the update because the user had requested to stop at the profile-photo choice; in-game visual confirmation is pending.
