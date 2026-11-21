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
- Synthetic N3DS reader/routing tests and X/Y single-screen Paparazzi screenshot tests pass. On the latest focused rerun, 6 ROM-dependent ORAS/Sun/Moon cart tests were skipped because those images are absent from this checkout; the synthetic Sun/Moon reader tests passed. Ultra Moon has separate S25 Ultra launch evidence below. A new `N3dsNativeInitTest` instrumentation smoke check also passes on physical ARM64 hardware and is repeatable with `make n3ds-smoke`.
- Full app unit suite completed with 4,194 tests, 17 failures and 59 skipped; failure names are recorded in `TEST_RESULTS.md`.
- `git diff --check` passes for the current changes.

## Current change

The pushed checkpoint fixes `make submodules` and the developer setup instructions to initialize DSperate, which the Android app build and license asset require but the old setup command omitted. It also adds project status, architecture, research, decision, issue, test and next-step documentation.

Commit `89c80ab2` adds USUM retail title recognition and Sun/Moon-style presentation without claiming USUM companion memory support, plus Settings > FOLDERS options for one shared DS save folder and an Azahar 3DS user-data root. DS saves are copied (not moved) when switching to a shared folder. Selecting an empty 3DS root copies the old Azahar tree first. Focused tests and a Dev build pass. The Dev APK was updated on the Galaxy S25 Ultra with `adb install -r`; the regular app was not modified. Documentation updates, including the later Pokémon Y and Ultra Moon device findings, are pushed through commit `5d7d4069`.

The official app's private settings have not been copied into Dev. The installed official package is non-debuggable and declares `allowBackup=false`, so Android blocks `run-as` and backup access to its preferences; its Settings screen and source expose no preferences export action. Dev's existing data remains preserved by in-place installation.

The 3DS single-screen stage is now enabled on portrait phones as well as landscape for 3DS games. On a single-screen phone with the default `ONE SCREEN` layout, the top screen is arranged alone and the touch surface stays available for the existing floating/manual touch control; portrait DS behavior is unchanged. `DsStagePolicyTest` and `:app:assembleDev -Ppokedaisy.n3ds=true` pass. The updated Dev APK was installed on 2026-11-22 using `adb install -r`; Android reported success, and the package still has the same application data, ROM index and preferences. The installed app opens to its existing library without running setup again. A follow-up Ultra Moon start reached `PokeDaisyActivity` and loaded `libpk_n3ds.so`, but did not render a game frame before Android recorded `USER REQUESTED / REMOVE TASK` at 00:28:18. The portrait layout therefore remains unverified on-device; this was not reported as an emulator crash.

## Runtime validation and remaining gaps

The initial inventory preceded completion of the user's dump. A user-owned European Pokémon Y `.3ds` image is now present in the designated directory. Its NCSD header and title ID (`0x0004000000055E00`) were checked read-only; the original was left untouched. A temporary device copy in a dedicated Download subfolder was linked in Dev. On the Galaxy S25 Ultra, the actual title booted into its first language-selection screen and rendered at about 59.8 FPS / 100% speed. The user selected and confirmed German; the game moved into its logo/intro sequence. After a process restart before reaching gameplay/save setup, the language prompt appeared again, so persistence of the language choice is not verified. German has been confirmed again in the current active session, which remains at the logo/intro; no later character/setup choice was made. Graphics/title boot and D-pad input are verified; audio, broader gameplay controls, battle/field behavior, save persistence, pause/resume, and long-session stability remain unverified.

The user's European Pokémon Ultra Moon image was inspected read-only (valid NCSD, decrypted, title ID `0x00040000001B5100`), copied to the same temporary device validation directory, and launched on the Galaxy S25 Ultra. The user chose German and advanced through the opening dialogue to Kukui's first player-profile photo question; no personal profile choice was made. Captured logs identify the running title as `00040000001b5100`; after loading, performance settled near 29.8–30.0 game FPS / 59.8 system FPS at 100% speed. A 445,440-byte `main` file is present in the title's data directory, but it is the only file there (no metadata file), and this does not establish an in-game save or reload. German persistence is unverified because the game has not been restarted since selection. Graphics, title classification, German-language input, and opening dialogue rendering are verified; audio, companion polling, gameplay after setup, save persistence, and longer stability remain unverified. Ultra Sun shares the routing implementation and unit-tested identity mapping but has not been launched on hardware.

Copying settings from the installed official package requires an official settings-export path or access Android does not grant on this device.

## Git state

Checkpoints `64e20368` (setup and project docs), `34eec0a6` (ARM64 JNI smoke test), `89c80ab2` (USUM routing and shared handheld save settings), and `5d7d4069` (Pokémon Y device findings) are pushed to `origin/feature/azahar-3ds-single-screen`. No upstream PR has been opened. Keep the goal active until game/runtime behavior, compatibility, and release evidence are established.
