# PokeDaisy 3DS Project Status

Development is on `feature/azahar-3ds-single-screen` in the personal fork `IzFunke/pokedaisy` (`origin`). The official repository is `upstream`; push defaults target `origin`.

## Verified

- Fork, remotes, branch and submodules are configured. Azahar is pinned at `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2).
- The checkout already contains feature-flagged Azahar JNI integration and Pokémon X/Y single-screen support. This work extends and validates existing integration rather than recreating it.
- Baseline `assembleDebug` succeeded. `assembleDebug -Ppokedaisy.n3ds=true` and `assembleDev -Ppokedaisy.n3ds=true` succeeded on Windows x64 with NDK 28.2.13676358.
- Dev APK contains `lib/arm64-v8a/libpk_n3ds.so`; its package is `com.pokedaisy.app.dev`. It is installed separately on the Galaxy S25 Ultra (ARM64, Android 17) and the x86_64 translation AVD.
- On the Galaxy S25 Ultra, `LibraryActivity` resumed and no fatal exception or native load error appeared in the captured startup log.
- The x86_64 translation AVD aborts with SIGILL during startup; its ARM64 translation is not a viable Azahar runtime target. It must not be counted as a successful runtime check.
- Official v1.6.2-beta.1 APK used for reference was checksum-verified against its published GitHub release digest; the original file was preserved.
- `git diff --check` passes.

## Current change

`make submodules` and the developer setup instructions now initialize DSperate, which the Android app build and license asset require but the prior setup command omitted.

## Not verified yet

No Pokémon Y ROM was found in the designated directory during the initial inventory. No game content was downloaded or modified. Azahar core initialization with a game, Pokémon Y gameplay, X/Y single-screen behavior on hardware, graphics/audio/input/saves, performance, and long-session stability remain unverified. The 3DS JNI integration compiles but the physical-device check so far covers app startup only.

## Tests

The feature-enabled debug and dev APK builds pass. A broad `:app:testDebugUnitTest` run reported unrelated existing failures (including BpsPatchTest, FfMusicBudgetTest, FlacWriterTest, PerformanceGuardsTest, RomFolderTest, RzipSaveTest, SaveFileEditsTest and SiteDataExportTest) and was interrupted before a final Gradle summary because execution stalled. A focused 3DS test run is in progress; see `TEST_RESULTS.md` for updates.

## Git state

Only `Makefile` and `docs/DEVELOPMENT.md` are currently modified. No commit or push has been made yet. Do not open an upstream PR until the remaining behavior, compatibility and test evidence is available and the user approves submission.
- Focused N3DS tests were attempted but could not start because a prior stalled Gradle Test Executor held a file lock in the generated test-results directory. Re-run after clearing generated test output.
