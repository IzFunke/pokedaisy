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

No 3DS game was launched. Pokémon Y was not found in the expected directory during inventory. No ROM was downloaded or touched. Azahar core initialization with a game, graphics/audio/input/save behavior, performance, and long-session stability remain unverified.

## Current save-folder and title-routing changes

- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests "com.pokedaisy.app.SavesLocationTest" --tests "com.pokedaisy.app.n3ds.N3dsCartTest" --tests "com.pokedaisy.app.n3ds.OrasSmCartTest"`: PASS (2026-11-21). Includes DS shared-folder precedence and USUM recognized-but-not-companion-supported checks.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS (2026-11-21).
- Galaxy S25 Ultra Dev package updated with `adb install -r` and launched. No uninstall or app-data clearing was performed.
- Official-to-Dev preference transfer was not possible: official package is not debuggable (`run-as` denied) and app manifest disables Android backup (`allowBackup=false`).
