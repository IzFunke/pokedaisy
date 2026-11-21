# PokeDaisy 3DS Project Status

Development is on `feature/azahar-3ds-single-screen` in the personal fork `IzFunke/pokedaisy` (`origin`). The official repository is `upstream`; push defaults target `origin`.

## Verified

- Fork, remotes, branch and submodules are configured. Azahar is pinned at `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2).
- The checkout already contains feature-flagged Azahar JNI integration and Pokémon X/Y single-screen support. This work extends and validates existing integration rather than recreating it.
- Baseline `assembleDebug` succeeded. `assembleDebug -Ppokedaisy.n3ds=true` and `assembleDev -Ppokedaisy.n3ds=true` succeeded on Windows x64 with NDK 28.2.13676358.
- Dev APK contains `lib/arm64-v8a/libpk_n3ds.so`; its package is `com.pokedaisy.app.dev`. It is installed separately on the Galaxy S25 Ultra (ARM64, Android 17) and the x86_64 translation AVD.
- On the Galaxy S25 Ultra, `LibraryActivity` resumed and no fatal exception or native load error appeared in the captured startup log.
- The x86_64 translation AVD aborts with SIGILL during startup; it is not a viable Azahar runtime target.
- Official v1.6.2-beta.1 APK used for reference was checksum-verified against its published GitHub release digest; the original file was preserved.
- Focused N3DS logic tests and X/Y single-screen Paparazzi screenshot tests pass. A new `N3dsNativeInitTest` instrumentation smoke check also passes on physical ARM64 hardware and is repeatable with `make n3ds-smoke`.
- Full app unit suite completed with 4,194 tests, 17 failures and 59 skipped; failure names are recorded in `TEST_RESULTS.md`.
- `git diff --check` passes for the current changes.

## Current change

The pushed checkpoint fixes `make submodules` and the developer setup instructions to initialize DSperate, which the Android app build and license asset require but the old setup command omitted. It also adds project status, architecture, research, decision, issue, test and next-step documentation.

## Not verified yet

No Pokémon Y ROM was found in the designated directory during the initial inventory. No game content was downloaded or modified. Azahar initialization with a game, Pokémon Y gameplay, X/Y single-screen behavior on hardware, graphics/audio/input/saves, performance, and long-session stability remain unverified. Physical-device checks cover app startup and Android/JNI setup only.

## Git state

Checkpoints `64e20368` (setup and project docs) and `34eec0a6` (ARM64 JNI smoke test) are pushed to `origin/feature/azahar-3ds-single-screen`. No upstream PR has been opened. Keep the goal active until game/runtime behavior, compatibility, and release evidence are established.
