# Test Results

Date: 2026-11-21

## Build checks

- `:app:assembleDebug`: PASS (baseline, feature disabled).
- `:app:assembleDebug -Ppokedaisy.n3ds=true`: PASS; Azahar ARM64 CMake target compiled and APK packaged.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS; isolated dev APK packaged.
- APK inspection: PASS; package `com.pokedaisy.app.dev`, native ABI `arm64-v8a`, includes `libpk_n3ds.so`.
- `git diff --check`: PASS.

## Device startup

- Galaxy S25 Ultra, ARM64 Android 17: dev package installed without replacing `com.pokedaisy.app`; `LibraryActivity` resumed; captured startup logs showed no fatal exception, SIGSEGV, or `UnsatisfiedLinkError`.
- x86_64/API 30 translation AVD: package installation succeeded but startup aborted with SIGILL. FAIL / incompatible runtime. This does not validate Azahar on x86.

## Unit tests

- Full `:app:testDebugUnitTest -Ppokedaisy.n3ds=true`: started but did not complete. Observed failures: BpsPatchTest, FfMusicBudgetTest, several FlacWriterTest cases, PerformanceGuardsTest, RomFolderTest, RzipSaveTest, SaveFileEditsTest, and SiteDataExportTest. Gradle was interrupted after the test executor stalled; no final test count is available. These results are not attributable to the 3DS changes without a clean baseline comparison.
- Focused N3DS tests: pending result.

## Game checks

No 3DS game was launched. Pokémon Y was not found in the expected directory during inventory. No ROM was downloaded or touched.
- Focused `N3dsCartTest`, `Gen6Test`, and `N3dsBattleTapsTest`: NOT RUN to completion. Attempts failed because the prior stalled Gradle Test Executor retained a lock on `app/build/test-results/testDebugUnitTest/binary/output.bin`; Gradle could not clean the test-results directory. The stale executor was terminated. Rerun after a clean checkout/build directory or reboot if needed.
