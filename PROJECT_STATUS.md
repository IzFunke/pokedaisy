# PokeDaisy 3DS Project Status

Development is on `feature/azahar-3ds-single-screen` in the personal fork `IzFunke/pokedaisy` (`origin`). The official repository is `upstream`; push defaults target `origin`.

## Verified

- Fork, remotes, branch and submodules are configured. Azahar is pinned at `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2).
- The checkout already contains feature-flagged Azahar JNI integration and Pokémon X/Y single-screen support. This work extends and validates existing integration rather than recreating it.
- Baseline `assembleDebug` succeeded. `assembleDebug -Ppokedaisy.n3ds=true` and `assembleDev -Ppokedaisy.n3ds=true` succeeded on Windows x64 with NDK 28.2.13676358.
- Production/device Dev APKs contain `lib/arm64-v8a/libpk_n3ds.so`. An opt-in x86_64 build (`-Ppokedaisy.n3dsX86_64=true`) now also packages Azahar for emulator runtime checks; the Galaxy S25 Ultra uses ARM64. The package remains isolated as `com.pokedaisy.app.dev`.
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


The user later reported that the 3DS view still appeared dual-screen and that 3DS ROMs no longer started on the S25 Ultra. The DS menu tracker override was restricted to DS games, and 3DS keeps its selected layout. Emulator runtime work then exposed two more causes: a hidden touch-screen `SurfaceView` could block Azahar from starting in ONE SCREEN, and the single-surface core layout was Azahar's `Default` (both screens), not `SingleScreen`. The boot now waits only for a touch surface that is visible at launch, and the one-surface mode selects Azahar's `SingleScreen` layout.

An opt-in x86_64 Azahar build now runs on the API 36 emulator. Ultra Moon boots to its first language-selection screen; logs report title ID `00040000001b5100` and steady 29.8–30.0 game FPS / 59.8 system FPS after shader warm-up. In ONE SCREEN, the capture shows only the 5:3 top screen. L2 opens the touch screen as a floating overlay and closes it again; creating its Android `SurfaceView` after boot now adds Azahar's second window on the emulation thread and recreates the renderer. Performance returned to 29.5–30.0 game FPS / 58.7–59.8 system FPS after the renderer switch. The emulator remains at the initial language-selection prompt; no personal game setup choice was made. Each emulator update used `adb install -r`, preserving app data. The S25 Ultra was not touched during this follow-up.

## Runtime validation and remaining gaps

The initial inventory preceded completion of the user's dump. A user-owned European Pokémon Y `.3ds` image is now present in the designated directory. Its NCSD header and title ID (`0x0004000000055E00`) were checked read-only; the original was left untouched. A temporary device copy in a dedicated Download subfolder was linked in Dev. On the Galaxy S25 Ultra, the actual title booted into its first language-selection screen and rendered at about 59.8 FPS / 100% speed. The user selected and confirmed German; the game moved into its logo/intro sequence. After a process restart before reaching gameplay/save setup, the language prompt appeared again, so persistence of the language choice is not verified. German has been confirmed again in the current active session, which remains at the logo/intro; no later character/setup choice was made. Graphics/title boot and D-pad input are verified; audio, broader gameplay controls, battle/field behavior, save persistence, pause/resume, and long-session stability remain unverified.

The user's European Pokémon Ultra Moon image was inspected read-only (valid NCSD, decrypted, title ID `0x00040000001B5100`), copied to the S25 validation directory, and launched on the Galaxy S25 Ultra. The user chose German and advanced through the opening dialogue to Kukui's first player-profile photo question; no personal profile choice was made. The title-ID save path contains a 445,440-byte `main` file and 16-byte `00000001.metadata` sibling; its bytes confirm `(0x40000, 1 directory, 1 file, duplicate=true)`. A separate emulator runtime check now also boots the same title to language selection and verifies the actual single-screen frame. Ultra Sun shares retail routing and identity mapping but has not been launched; its save format is not guessed without live evidence.

A read-only filename check of the user's designated host ROM folder and Dev ROM folder found Pokémon Y and Ultra Moon, but no Ultra Sun image. Ultra Sun's hardware boot check therefore remains pending on the user's own dump becoming available.

Copying settings from the installed official package requires an official settings-export path or access Android does not grant on this device.

## Git state

Checkpoints `64e20368` (setup and project docs), `34eec0a6` (ARM64 JNI smoke test), `89c80ab2` (USUM routing and shared handheld save settings), and `5d7d4069` (Pokémon Y device findings) are pushed to `origin/feature/azahar-3ds-single-screen`. The new x86_64 runtime/layout fix is being validated before its next checkpoint. No upstream PR has been opened. Keep the goal active until game/runtime behavior, compatibility, and release evidence are established.
