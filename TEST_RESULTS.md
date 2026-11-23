# Test Results

## 2026-11-22 S25 runtime follow-up

- Rebuilt `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain` successfully and updated the S25 Ultra's existing `com.pokedaisy.app.dev` package using `adb install -r` (`Success`; versionCode 23 / versionName 1.6.2-beta.1). No uninstall, app-data clear, or settings reset was used. This was the current source build; no source code changed in this follow-up.
- Pokémon Y Europe booted through Azahar at about 29.8–30 FPS. Runtime module logs reached the title/start-menu path only. A title-state memory capture read the configured MyStatus language byte as `5` (German), but no party or bag entries decoded at that state, so this does not validate the X/Y gameplay map or Companion telemetry.
- Ultra Moon Europe rendered the German intro at about 30 FPS. Runtime modules included `AppDemo`, `SequencePlayer`, `Object`, then `StrInput` / `HeroSelect`; `N3dsSession` still reported `UNMAPPED`. No profile photo was selected; testing stopped at setup in accordance with the player's earlier instruction.
- Before device testing, both 3DS save files and metadata were copied under `work/s25-validation-saves-20261122/`. A second pull afterward had matching SHA-256 hashes for all four files. No save changed. The Y memory captures remain under `work/s25-y-live-dump-20261122/`.
- Result: both retail carts render on the S25. This does not pass the goal's field/menu/battle state detection, auto touch-screen, Companion, transition, or DS-parity criteria. See `docs/3DS_DS_PARITY.md` for the exact observed limits.

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
- 2026-11-22: rebuilt `:app:assembleDev -Ppokedaisy.n3ds=true` and updated the existing S25 Ultra Dev package with `adb install -r`. Android retained `/data/user/0/com.pokedaisy.app.dev`; the SHA-256 of `shared_prefs/pokedaisy.xml` was identical before and after, and `setup_done` / `last_rom` remained present. No uninstall, clear-data, or app relaunch/game progression was performed during this update.
- 2026-11-22 read-only S25 follow-up: `dumpsys activity exit-info com.pokedaisy.app.dev` reports the three latest process exits (01:06:54, 01:07:11, 01:08:02 local) as `USER REQUESTED` / `REMOVE TASK`, status 0. Those records show no crash or ANR for those exits, but do not prove a ROM boot; the current focus is the notification shade and no game was launched during this check.
- 2026-11-22 API 36 AVD state recheck: `com.pokedaisy.app.dev` remains installed and its existing preferences are present, but `last_rom` points to a missing app-private ROM copy. The AVD has 2.5 GB free; the host Ultra Moon image is 4 GiB and its last NCSD partition ends at 3,759,026,176 bytes, so a fresh copy/boot check does not fit without adding emulator storage. No emulator data was deleted to make room.
- x86_64/API 30 translation AVD: package installation succeeded but startup aborted with SIGILL. FAIL / incompatible runtime. This does not validate Azahar on x86.

## Focused 3DS tests

- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests "com.pokedaisy.app.n3ds.N3dsCartTest"`: PASS.
- Same task filtered to `Gen6Test`, `N3dsBattleTapsTest`, `OrasSmCartTest`, and `SmBattleDumpTest`: PASS.
- Same task filtered to `N3dsBattleOneScreenScreenshotTest`, `XyCompanionScreenshotTest`, and `XyFieldHudScreenshotTest`: PASS. These are Paparazzi screenshot tests of the existing single-screen UI, not hardware gameplay checks.

## Full app test suite

Command: `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain`.

2026-11-22: Gradle completed after 13m03s: 4,207 tests, 0 failures, 60 skipped across 191 test classes. This rerun includes the earlier Windows path/rename, atomic replacement, save, and frame-publication fixes. `SiteDataExportTest` now normalizes CRLF to LF before comparing generated JSON, so the fixture check works with Windows checkout line endings. The focused N3DS and single-screen results above remain separate from this full JVM suite; ROM-dependent skips remain expected.

## Recheck of the formerly failing baseline classes

- 2026-11-22: re-ran the eight classes named by the 17-failure run (46 tests, 1 skipped): PASS after fixing atomic replacement of existing FLAC/WAV/cache/save files, making backup names collision-safe, normalizing path comparisons across host separators, and updating the frame-publication source guard to assert ordering rather than exact formatting. The symlink alias test was skipped when Windows denied symlink creation. The full suite subsequently passed after normalizing generated website JSON line endings; `website/src/data/compat.json` required no content update.
- Full-suite verification now covers 4,207 tests across 191 classes: 0 failures, 60 skips.
- After the file/path fixes, the focused 3DS regression set (`DsStagePolicyTest`, `N3dsCartTest`, `N3dsSavesTest`, `OrasSmCartTest`, `N3dsBattleTapsTest`) passed: 26 tests, 0 failures, 7 ROM-dependent skips. `:app:assembleDev` with ARM64 and x86_64 Azahar flags passed. The API 36 x86_64 emulator was updated with `adb install -r`; `setup_done=true`, `last_rom=UltraMoon.3ds`, and `ds_layout=0` remained. The S25 Ultra was not touched.

## Game checks

The initial inventory occurred before the user's Pokémon Y dump was available. Afterward, the user's European Pokémon Y image was inspected read-only and a temporary copy was linked from a dedicated Download subfolder in Dev. On the Galaxy S25 Ultra, the actual game booted to its language-selection screen and rendered at about 59.8 FPS / 100% speed in the observed session. The user chose and confirmed German; the game advanced into its logo/intro sequence. After a process restart before reaching gameplay/save setup, the game returned to language selection; German has been confirmed again in the current active session. The original ROM was not modified. This verifies title boot, graphics, D-pad navigation, and accepted language input, but not persistence of that language choice, later gameplay, audio, save persistence, broad controls, or stability.

The user's European Ultra Moon image was checked read-only (decrypted NCSD, title ID `0x00040000001B5100`), copied to a dedicated device test folder, and launched on the same phone. The user chose German, and the game advanced through Kukui's opening dialogue to the first player-profile photo question (“Welches dieser Fotos stammt gleich noch mal von dir?”); no profile or character choice was made. Azahar logs reported the expected title ID and, after loading, about 29.8–30.0 game FPS / 59.8 system FPS at 100% speed. A read-only follow-up found the 445,440-byte `main` file and its sibling 16-byte `00000001.metadata` in the title's parent `data/` directory (both mtime 23:12 local). The metadata bytes `(0x40000, 1, 1, 1)` are now used as Ultra Moon's known fallback format. This confirms archive format metadata, but not in-game progress save, persistence, or reload. German-choice persistence remains unverified because the game has not been restarted since the latest selection. Audio, gameplay after setup, companion polling, and longer stability remain unverified. Ultra Sun was not available for the hardware boot check.

## Current save-folder and title-routing changes

- 2026-11-22 focused Sun/Moon and Ultra Sun/Ultra Moon rerun: `N3dsCartTest` (6 tests, 1 ROM-dependent skip), `OrasSmCartTest` (8 tests, 6 ROM-dependent skips), and `N3dsSavesTest` (2 tests): 16 total, 0 failures, 7 skips. Confirms all four title IDs are launchable when decrypted, family/layout identity is mapped, USUM remain excluded from companion RAM support, Ultra Moon's known save metadata is used, and Ultra Sun's unverified save metadata is left unset.
- 2026-11-22: focused persistence regression run after adding coverage for the shared DS and 3DS save-folder preferences: `PrefsTest` (5 tests) and `SavesLocationTest` (7 tests), 12 passed / 0 failed / 0 skipped. Confirms both global paths survive preference-store recreation and can be reset, alongside shared DS folder precedence.
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests "com.pokedaisy.app.SavesLocationTest" --tests "com.pokedaisy.app.n3ds.N3dsCartTest" --tests "com.pokedaisy.app.n3ds.OrasSmCartTest"`: PASS (2026-11-21). Includes DS shared-folder precedence and USUM recognized-but-not-companion-supported checks.
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest --rerun-tasks`: PASS (2026-11-21). It rebuilt selected JVM dependencies; `N3dsCartTest` had 4 pass / 1 ROM-dependent skip, and `OrasSmCartTest` had 2 pass / 6 ROM-dependent skips. The passing cases verify synthetic NCSD/NoCrypto routing for both Ultra IDs, Sun/Moon family classification/layout, and deliberate absence of USUM companion configs. The skipped cases require retail X/ORAS/Sun/Moon ROM files that are not present in this checkout's test directory.
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.n3ds.SmReaderTest`: PASS (2026-11-21), all 6 synthetic-memory tests passed for Sun/Moon scene, bag, battle, camera and pointer-chain reads. These exercise reader logic with constructed bytes, not live Sun/Moon RAM.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS (2026-11-21).
- Galaxy S25 Ultra Dev package updated with `adb install -r` and launched. No uninstall or app-data clearing was performed.
- Official-to-Dev preference transfer was not possible: official package is not debuggable (`run-as` denied) and app manifest disables Android backup (`allowBackup=false`).

## 3DS single-screen portrait follow-up

- 2026-11-22 focused routing rerun: `N3dsCartTest` (4 pass / 1 ROM-dependent skip), `OrasSmCartTest` (2 pass / 6 ROM-dependent skips), `N3dsBattleTapsTest` (3 pass), and `DsStagePolicyTest` (3 pass). No failures. USUM checks verify launch classification and shared Sun/Moon battle taps while explicitly asserting that Ultra Sun / Ultra Moon do not yet have companion memory configs.
- `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon`: PASS (APK 84,293,811 bytes, built 2026-11-22). `adb install -r app/build/outputs/apk/dev/app-dev.apk` then returned `Success`; installed package `com.pokedaisy.app.dev` remains versionCode 23 / versionName 1.6.2-beta.1 (lastUpdateTime 2026-11-22 00:17:07).
- `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.DsStagePolicyTest`: PASS (3 tests). 3DS stages are enabled in portrait and landscape; portrait DS remains unchanged; second-screen, debug-mirror and fold cases remain excluded. 3DS now keeps its stage while the companion is over the game.
- `:app:assembleDev -Ppokedaisy.n3ds=true`: PASS (2026-11-22), APK packaged at `app/build/outputs/apk/dev/app-dev.apk` (84,293,811 bytes).
- On the Galaxy S25 Ultra, the Dev package data directory, ROM index, selected ROM folders, and `pokedaisy.xml` remained present after the in-place update; the app returned to its library without setup. No app data was cleared and the package was not uninstalled. A follow-up Ultra Moon start reached `PokeDaisyActivity` and loaded `libpk_n3ds.so`, but did not render a game frame before Android recorded process exit reason `USER REQUESTED` / subreason `REMOVE TASK` at 2026-11-22 00:28:18. This does not establish an emulator crash or a successful game boot. No personal profile option was selected; visual verification remains pending.
- Source review found why `ONE SCREEN` could still look like dual-screen: a detected 3DS `SCREEN` scene changed its arrangement to side-by-side. Removed that implicit override so `ONE SCREEN` consistently displays only the top game screen; manual touch-screen access and explicit side-by-side layouts remain.
- 2026-11-22 regression check: `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.N3dsBattleTapsTest --offline --no-daemon --console=plain`: PASS. `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain`: PASS. Installed the resulting APK with `adb install -r`; Android returned `Success`, package update time is 00:39:48, and `shared_prefs/pokedaisy.xml` remains present. No game relaunch was done after the update because the user had requested to stop at the profile-photo choice; in-game visual confirmation is pending.
- 2026-11-22 follow-up regression run: `N3dsCartTest` 4 passed / 1 ROM-dependent skip; `OrasSmCartTest` 2 passed / 6 ROM-dependent skips; `N3dsBattleTapsTest` 3 passed; `DsStagePolicyTest` 3 passed; 0 failures. A read-only check found no Ultra Sun ROM in the user's designated host ROM folder or Dev ROM folder, so its physical-device launch cannot be checked yet.
- Read-only Ultra Moon save-path recheck: Azahar's 16-byte `00000001.metadata` is alongside `00000001/` in the parent `data/` directory, not inside it. No save file was changed. The earlier statement that this save had no metadata was based on checking the child directory rather than its parent and has been corrected.
- `N3dsSavesTest`: Ultra Moon's live metadata values are covered as `(0x40000, 1, 1, duplicate=true)`; Ultra Sun intentionally remains unmapped pending live evidence.
- After adding the verified Ultra Moon metadata fallback, `N3dsSavesTest` passed 2/2 and `N3dsCartTest` passed 4 with 1 ROM-dependent skip; no failures. `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain` passed. The APK was updated in place with `adb install -r` (package update time 00:52:38); `pokedaisy.xml`, `00000001/`, and sibling `00000001.metadata` remained present afterward. The game was not relaunched.
- Source review then found `MENU` and battle bag/party callbacks could still auto-open the touch screen over the top screen. The strict policy removes those auto-opens, lets the user close/reopen manually in every scene, and preserves other selected two-screen layouts.
- Strict one-screen regression run: `DsStagePolicyTest` 4 passed; `N3dsBattleTapsTest` 3 passed; `N3dsSavesTest` 2 passed; `N3dsCartTest` 4 passed / 1 ROM-dependent skip; zero failures. `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain`: PASS. Installed by `adb install -r` (lastUpdateTime 01:00:11); `shared_prefs/pokedaisy.xml`, Ultra Moon's `00000001/`, and sibling `00000001.metadata` remain. No game launch or personal choice was made.
- Follow-up `TOUCH SCREEN` access check corrected the tip path: 3DS now advertises the same toggle through the touch quick action, L2 hotkey, and feature hint as DS. The hint's TRY action toggles the lower screen instead of stepping the layout. `DsStagePolicyTest` 5 passed; `N3dsBattleTapsTest` 3 passed; `N3dsSavesTest` 2 passed; `N3dsCartTest` 4 passed / 1 ROM-dependent skip; zero failures. `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain`: PASS. Installed by `adb install -r` (lastUpdateTime 01:04:54); settings and Ultra Moon save path remained present.
- After the user's report of persistent dual-screen output, `dsStageLayout` was added so DS menu tracking cannot override a selected 3DS layout. The test also keeps the NDS menu override intact for DS titles. `DsStagePolicyTest` 6 passed; `N3dsBattleTapsTest` 3 passed; `N3dsSavesTest` 2 passed; `N3dsCartTest` 4 passed / 1 ROM-dependent skip; zero failures. `:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.x86_64=true --offline --no-daemon --console=plain`: PASS (APK 91,626,150 bytes). An x86_64 API 36 AVD installed this build via `adb install -r` and reached `LibraryActivity`; no Azahar library is packaged for x86_64, so this does not verify a 3DS boot or screen layout. The user reported that 3DS ROMs no longer start on the S25 Ultra; that remains undiagnosed pending fresh ARM64 device logs.
- Emulator diagnosis confirmed the test ROM initially appeared missing because `adb push` created the app's external `roms/` directory as the shell UID. Correcting ownership on the emulator made Ultra Moon appear in the app library; this was test setup, not a product scanner bug.
- Runtime reproduction on an API 36 x86_64 AVD found the game boot waited forever for an invisible touch `SurfaceView` in ONE SCREEN (`top=true`, `touch=false`, but touch was required). Added `n3dsTouchSurfaceRequired`: ONE SCREEN no longer waits for its hidden touch surface; explicit multi-screen layouts and second-display touch still require it.
- With the touch-surface gate fixed, Ultra Moon loaded as title `00040000001b5100` on x86_64 and settled at 29.8–30.0 game FPS / 59.8 system FPS. This surfaced a second layout issue: the one-surface argument was Azahar `LayoutOption::Default` (both screens). Changed it to `LayoutOption::SingleScreen` (`1`). The emulator frame now shows one 5:3 screen with no second-screen panel. Runtime remained at the initial language choice; no personal choice was made.
- Final focused run: `:app:testDevUnitTest --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.N3dsSavesTest`, plus `:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true --offline --no-daemon --console=plain`: PASS. Results: DsStagePolicyTest 7 passed; N3dsCartTest 4 passed / 1 ROM-dependent skip; N3dsSavesTest 2 passed; zero failures. APK updated on the emulator only with `adb install -r`; application data stayed present. Physical S25 Ultra was not touched in this follow-up.
- Follow-up surface lifecycle check: `ONE SCREEN` boot logs showed `top=1 touch=0 layout=1`. Ultra Moon rendered only its top screen. L2 opened the floating touch screen; the newly visible SurfaceView measured `708x531`, delivered surface 1, and Azahar created a secondary window/recreated its renderer at the frame loop. L2 closed the overlay again. After the brief renderer switch, performance returned to 29.5–30.0 game FPS / 58.7–59.8 system FPS. The emulator stayed at the language prompt, with no game choice. `:app:assembleDev` passed after the dynamic view-size fix; focused tests are rerun with the final cleaned build.
- 2026-11-22 routing regression: `:app:testDevUnitTest --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.N3dsSavesTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest --offline --no-daemon --console=plain`: PASS. Counts: DsStagePolicy 7/7; N3dsCart 5 passed, 1 ROM-dependent skip; N3dsSaves 2/2; OrasSmCart 2 passed, 6 ROM-dependent skips; zero failures. New synthetic cart assertions verify Pokémon Sun and Moon are playable, supported, mapped to the correct `GameKind`, and use the Gen VII Sun/Moon layout. Ultra Sun and Ultra Moon retain their recognized launch route and family layout while remaining excluded from companion-supported carts until their memory/save mappings are verified. No new ROM dumps were available for additional real-cart checks.
- 2026-11-22 x86_64 runtime follow-up: `:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true --offline --no-daemon --console=plain`: PASS; Dev APK updated on `emulator-5556` using `adb install -r`. No uninstall or app-data clear was used. `setup_done=true`, `ds_layout=0`, and `last_rom=PokemonY-UserCopy.3ds` remained in Dev preferences after the update. The user-authorized German choice was confirmed; the game then showed “German” and the `Begin game` / `Select language` menu. It was left before starting the game or entering personal setup. Language persistence across a process restart was not checked.
- The x86_64 API 36 runtime showed the intended `ONE SCREEN` lifecycle: the native top SurfaceView remains visible with the touch surface closed; L2 opens Azahar's second surface and displays the game's actual language-help screen; closing L2 returns to the top-only view. SurfaceFlinger showed one game SurfaceView while closed and a second while open. The local screenshots are in the ignored `work/emulator/` directory and are not committed. This is a Pokémon Y boot/screen-routing check, not a full gameplay, audio, save, or stability test.
- The ARM64 API 36 AVD was inspected but could not run on this Windows x86_64 host: the installed emulator reports `Avd's CPU Architecture 'arm64' is not supported by the QEMU2 emulator on x86_64 host`. No ARM AVD image or unrelated emulator data was changed. Physical S25 Ultra was not updated or used for this follow-up.
- Read-only inventory of the user's designated ROM folder found only Pokémon Y and Ultra Moon. There is no Pokémon X, Sun, Moon, or Ultra Sun image there, so no additional real-cart launches were attempted; the Sun/Moon and USUM additions remain supported by the documented title-routing and synthetic tests until those user-owned carts are available.
- Re-run after the final UI code change: `:app:testDevUnitTest --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.N3dsSavesTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest --offline --no-daemon --console=plain`: PASS, zero failures. `:app:assembleDev` with the x86_64 and N3DS flags above also passed.
- Android API 36 composition regression: a non-negative composition order is applied to the touch `SurfaceView` before it attaches. `:app:assembleDev :app:testDevUnitTest --tests com.pokedaisy.app.DsStagePolicyTest` with `-Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true --offline --no-daemon --console=plain`: PASS. Updated the x86_64 AVD only with `adb install -r`. Pokémon Y remained at the unconfirmed language selector. Screenshots verify closed = single 5:3 output, open = actual 4:3 touch screen, closed again = single 5:3 output. The S25 was not used; hide behavior below API 36 remains unverified.
- Ultra Moon follow-up on the same API 36 x86_64 build: native log title ID `00040000001b5100`; first language-selection screen rendered at about 27–28 game FPS / 55–56 system FPS after warm-up. No language was confirmed and no personal setup was entered. The current S25 Dev package was read-only verified as still installed (versionCode 23); it was not updated during this emulator check.
- Read-only device properties identify the S25 as `SM-S938B`, Android 17 / API 37, so the OS exposes the API 36 composition-order API used by the fix. This is platform compatibility evidence only; the updated APK was not installed on the phone in this follow-up.

## 2026-11-22 linked-folder and Ultra Moon follow-up

- `:app:testDebugUnitTest --tests com.pokedaisy.app.CompanionSupportPokemonTest --tests com.pokedaisy.app.RomFolderTest --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.N3dsSavesTest`: PASS, 19 tests, 0 failures, 2 skipped (ROM-dependent cases).
- On the API 36 x86_64 AVD, Ultra Moon was visible from the linked folder, tagged `NOT SUPPORTED`, and booted through the direct launch route. Azahar logged title ID `00040000001b5100`, then about 29.9 game FPS / 59.8 system FPS after warm-up. No crash or ANR was observed.
- The closed ONE SCREEN screenshot still showed a second 4:3 surface. On-screen D-pad input did not move the language cursor. No language choice or player-profile choice was made. These are unverified/failed behaviors, not a successful single-screen or controls result.
- The S25 Ultra was not updated. No helper script matching the timeout task was present in its Download, Documents or `/data/local/tmp` locations. Its seven-hour Windows restore task remains scheduled for 2026-11-22 08:42:31 +01:00; on successful restore it unregisters itself and removes its local PowerShell script and log.

## 2026-11-22 touch-surface composition follow-up

- Replaced the 3DS touch `SurfaceView` with a `TextureView`, which Android composites as part of the app window. The top surface remains a `SurfaceView`; the Azahar second window remains available for the touch display.
- `:app:assembleDev :app:testDevUnitTest --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.N3dsCartTest --tests com.pokedaisy.app.n3ds.N3dsSavesTest` with N3DS and x86_64 flags: PASS. The x86_64 API 36 AVD was updated only with `adb install -r`.
- Ultra Moon booted to the German `Begin game` / `Select language` screen; the accepted German setting persisted through this session. Azahar logged title ID `00040000001b5100` and ran at about 29.9 game FPS / 59.8 system FPS. No crash or ANR was observed. No character or profile choice was made.
- SurfaceFlinger showed only the top native `SurfaceView`; the touch surface is now a TextureView inside the app layer. The TOUCH SCREEN hotkey opened and closed its framed window without a crash, but its content was black on the settings-saved dialog. Normal in-game touch-screen output remains to be verified. The S25 package and data were untouched.
- A landscape rotation on the API 36 AVD showed the top 5:3 game view filling the central available area. Azahar remained live during rotation at 27.8–28.7 game FPS while settling; it returned to about 29.9 FPS after. Emulator rotation settings were restored to automatic portrait.

## 2026-11-22 DS-style 3DS screen follow-up

- `:app:assembleDev` with `-Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true`: PASS.
- The Dev app was updated on emulator-5558 with `adb install -r`; Ultra Moon rendered its setup displays side by side, then continued in top-only ONE SCREEN mode. The German language choice was confirmed and the intro advanced to `Wie heißt du?`. No protagonist name or profile was selected.
- 3DS scene-driven touch overlay and routing compile. Touch input on the virtual keyboard remains unverified. No unit tests were run for this follow-up. The Galaxy S25 Ultra app/data were untouched.
- Follow-up refinement compiled and installed with `adb install -r`: 3DS field menus and battle bag/party states now request the lower display automatically; manual opening works for both ONE SCREEN and PIP. The app launched back to the library without an AndroidRuntime exception. Battle submenus and stylus input have not yet been exercised in-game.
- External-display 3DS routing now retains the lower surface on the DS-style bottom-screen host and keeps it visible across scene/layout updates. Build-verified only; no external display is attached for runtime confirmation.
- The S25 timeout restore was verified: secure timeout `5000`, screen timeout `300000`; the scheduled task and local helper files were removed.

## 2026-11-22 latest layout follow-up

The first attempt after the preceding update exposed a startup wait on an invisible 3DS `TextureView`: logs showed `top=true`, `touch=false`, and `touchRequired=true`. The policy now waits for the second surface only while its window is visible or routed to another display. Ultra Moon then launched as title `00040000001b5100`; after warm-up it ran around 29.8-30.0 game FPS / 59.6-59.9 system FPS.

## 2026-11-22 diagnostic startup and surface transition

Focused policy, Sun/Moon reader, and cart routing tests passed, as did the x86_64 Dev build. The emulator and S25 Ultra were updated only with adb install -r. On emulator-5558, the closed screenshot shows the top display alone; opening TOUCH SCREEN created the second surface and showed the language-selection help content in the framed window; closing returned to ONE SCREEN. Renderer creation briefly reduced performance, then it returned to about 30 game FPS / 59.8 system FPS. No language or personal setup option was selected in this run. On the S25, setup_done=true and the existing last_rom remained present after the in-place update.

## Verified startup and touch-surface transition (2026-11-22)

- Regression test/build commands: `:app:testDebugUnitTest -Ppokedaisy.n3ds=true --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.SmReaderTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest` and `:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true`: PASS.
- The startup deadlock was an invisible 3DS `TextureView` being required before Azahar could start. The start policy now waits for a touch surface only while its window is visible or routed to another display.
- Ultra Moon (`00040000001b5100`) booted on emulator-5558, showed the top screen alone while closed, opened the lower screen in a framed window, then closed back to ONE SCREEN. After the transition, logs returned to roughly 30 game FPS / 59.8 system FPS. No language or personal profile choice was made.
- The x86_64 emulator and S25 Ultra were updated with `adb install -r`. The S25's `setup_done=true` and existing `last_rom` preference remained present. No uninstall or data clear was used; the S25 display was not manually inspected.

## Black 2 / Ultra Moon comparison follow-up (2026-11-22)

- Focused Windows Gradle run: `:app:testDevUnitTest -Ppokedaisy.n3ds=true --tests '*DsStagePolicyTest' --tests '*OrasSmCartTest'` — PASS. `DsStagePolicyTest`: 11 tests, 0 failures. `OrasSmCartTest`: 8 tests, 0 failures, 6 skipped because the associated ROMs are absent. These tests cover policy and existing synthetic Sun/Moon routing; they do not prove USUM RAM mapping or S25 battle-touch behavior.
- Source review against the live S25 screenshots confirmed Ultra Moon's config-null path suppresses both Companion snapshots and scene polling. A clear unmapped-title Companion message and battle auto-window dismiss policy were added; the focused run passed against the first change set. A final incremental test/build is still required after the last source cleanup.
- The first Windows native-capture configure correctly failed because no native C/C++ compiler is installed. A Linux Docker builder was used instead. Its initial run exposed a missing C++20 requirement on `n3ds_dump`; `target_compile_features(n3ds_dump PRIVATE cxx_std_20)` is now explicit. Two task-created containers wrote to one Ninja directory at once; they were stopped, and a single rebuild is running. No executable or ROM analysis result is recorded yet.
- No S25 APK update was installed as part of this comparison follow-up; package data and the live Ultra Moon setup prompt were preserved. No ROM or save was changed.
# 2026-11-22 headless Ultra Moon EU startup

- `n3ds_dump` configured and built successfully against the pinned Azahar core in Docker (`83/83` incremental steps).
- The cart was mounted read-only; output/config/save paths were isolated under `work/usum-diagnostic-scratch`.
- Verified title ID `00040000001b5100`, frame-600 language chooser, `findcrs` static root `0x008b2000`, and module name
  `LangSelect`. Captured/reloaded a 7.7 MB savestate successfully for faster follow-up observations.
- A frame-600 interpreter run took several minutes. A subsequent JIT run also booted to the language screen; a savestate
  attempt after an A-button experiment crashed in Boost serialization (`unregistered class`). That crash was confined to
  the diagnostic process and scratch directory. A screenshot showed the permanent language choice had not been
  confirmed. No profile/photo prompt was passed, and no production app, ROM, or save was changed.
- The harness initially failed because disabling Azahar PCH removed the transitive `<bit>` include required by
  `video_core/pica_types.h`; a target-local PCH for `<bit>` resolved it. No vendor source was modified.

## Full Dev validation after the Companion fallback changes (2026-11-22)

- `:app:testDevUnitTest :app:assembleDev -Ppokedaisy.n3ds=true` — PASS (`BUILD SUCCESSFUL`, 9m30s).
- JUnit XML confirms 4,213 tests across 191 suites, 0 failures, 0 errors, and 60 skipped.
- The ARM64 Dev native library and Dev APK were built, then updated on the S25 with `adb install -r` while PokeDaisy
  was in the library. Package data was retained: `setup_done=true` and the existing `last_rom` preference are present.
  The installed package still reports versionCode 23/versionName 1.6.2-beta.1; its update time advanced. The screen was
  not brought to the foreground for this check.
- `git diff --check` reports no whitespace errors; Git only reports line-ending normalization warnings for edited files.

## Ultra Moon startup module polling (2026-11-22)

- A separate `n3ds_dump` process loaded the existing language-selector savestate and queried module root `0x008b2000`;
  output: `modules LangSelect`. No game input was sent and the ROM remained mounted read-only.
- `:app:testDevUnitTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest -Ppokedaisy.n3ds=true` — PASS.
- `:app:assembleDev -Ppokedaisy.n3ds=true` — PASS; ARM64 Dev APK built.
- S25 update used `adb install -r`. `setup_done=true`, the `last_rom` preference remains present (currently Pokémon Y),
  and the shared save directory preference is still set. No game was launched and no save was written. Screen-level
  confirmation is pending.

## Focused Gen 6 / USUM startup regression and build (2026-11-22)

- `:app:testDebugUnitTest --tests 'com.pokedaisy.app.n3ds.Gen6Test' --tests 'com.pokedaisy.app.n3ds.OrasSmCartTest' --offline --no-daemon --console=plain` — PASS. JUnit XML: `Gen6Test` 11 tests (0 failures, 7 skipped); `OrasSmCartTest` 9 tests (0 failures, 6 skipped). The skipped cases require ROM fixtures not present in the checkout.
- `:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true --offline --no-daemon --console=plain` — PASS. Dev APK: `app/build/outputs/apk/dev/app-dev.apk` (131,804,599 bytes). No APK was installed in this run.
- Independently compared `work/usum-appdemo-heap.bin` and `work/usum-photo-view-heap.bin`: each is 16 MiB; 86,455 bytes differ. The strings and `+0x19AC` checks are recorded in `RESEARCH.md`; they provide no reliable runtime-state marker or Ultra Moon address map.

## 2026-11-22 Ultra Moon EU ROM-backed test

With `POKEDAISY_USUM_ROM` set to the local read-only Ultra Moon Europe `.3ds` image, the focused USUM layout and routing tests passed. Coverage confirms German text tables, move/personal records, named Route 1 zones, wild encounter records, icon extraction, and map extraction from the USUM-specific `a/2/3/8` archive. The same focused run passed in 24 seconds. A feature-enabled `assembleDev` build for ARM64 and x86_64 also succeeded in 19 seconds. ROM contents were not modified or included in outputs.

## 2026-11-22 Ultra Moon setup screen routing

Focused tests passed for the known language and profile/intro module groups, unknown/partial groups remaining unmapped, and the paired-screen stage policy. This verifies classifier and layout-policy behavior; no game input was sent and no device visual check was performed.

The classifier/layout change builds in the feature-enabled Dev APK (`:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true`; successful, 2026-11-22). The APK was not installed during this check, so the emulator remains at the existing photo/profile prompt.

## 2026-11-22 Companion unmapped-status display

The feature-enabled Dev build succeeded with ARM64 and x86_64 Azahar libraries (`:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true`). The focused `:app:testDebugUnitTest --tests '*CompanionLoadingStatusTest'` invocation compiled the changed app and test sources, but the test worker produced no report and did not exit; it was interrupted after the second bounded run. No test pass is claimed. The APK was not installed on either emulator or the S25 Ultra.

## 2026-11-22 classifier correction

Removed the temporary rule that treated `AppDemo + SequencePlayer + Object` as a known screen: the same module group spans ordinary narration and photo/profile setup. Focused tests now assert the group remains unmapped, while `LangSelect` remains recognized. The feature-enabled ARM64+x86_64 Dev build passes after this correction; no game input or APK installation is part of this check.

## 2026-11-22 USUM candidate-memory diagnostic

- `:app:compileDevKotlin -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true` — PASS. Alongside the existing Gen 7 heap dump, the debug path now captures the published USUM trainer, bag, money, and two party-candidate ranges with a TSV address manifest. It does not activate those offsets in Companion or screen policy.
- The full `:app:testDevUnitTest` run completed 4,220 tests with 1 failure and 61 skipped. The failure was an outdated USUM layout assertion expecting `N3dsLayout.SM` after USUM received its own static layout. The assertion and test name were updated; `:app:testDevUnitTest --tests com.pokedaisy.app.n3ds.OrasSmCartTest` then passed all 9 class cases (6 ROM-dependent skips).
- The full suite was not rerun after that test-only correction. No APK was installed, so the active emulator session was preserved; the sparse candidate capture has not yet been run on-device. The S25 was not accessed.

### Live sparse capture follow-up

The feature-enabled Dev APK had since been installed in place on `emulator-5556`; the prior shared-preferences SHA-256 was unchanged. A read-only `n3ds_dump` marker captured the published candidate ranges while Ultra Moon continued running. The current game screenshot reads “Die Einstellungen wurden gespeichert.”, so no game input was sent and this is not assumed to be the previously documented Kukui photo prompt.

- Capture `work/usum-candidates-1795357810421.bin` matches its TSV manifest and contains exactly 9,652 bytes.
- At primary candidate `0x33F7FA44`, six candidate slots have zero encryption constants and species 0 (empty slots).
- At alternate candidate `0x330128E4`, the sampled slots with nonzero encryption constants fail the PK7 stored-checksum validation using the existing Gen 6/7 Pokémon cipher; no valid Pokémon was decoded.
- This does not validate either candidate as the live party, nor establish trainer, bag, scene, or battle mappings. The capture is only evidence from the current setup/session state; do not enable a production Companion or automatic screen map from it.
- Sparse capture succeeded and the existing 16 MiB heap file was also produced. The S25 received no commands or input.

## 2026-11-22 live Ultra Moon paired-screen mismatch

A read-only capture of the active API 36 x86_64 emulator confirms the German Ultra Moon `LangSelect` setup scene is reported as `SCREEN`. Dev preferences still say `ds_layout=0` (`ONE SCREEN`) and Android reports only display 0. For this scene the current policy changes floating layouts to `SIDE_BY_SIDE`; the captured upper screen occupies the expected left 5:3 box, but the expected right-hand lower-screen box is black. Logs show Azahar started with `touch=false`, then the scene callback made the lower `TextureView` ready (`touch=true`) while the readiness predicate still says `touchRequired=false`. This is a concrete mismatch between the visual result and the intended paired-screen policy. It may be related to the second renderer being attached after boot, but that is not proven; the bottom-frame debug request produced no image file, so its actual framebuffer content is unknown.

During an attempted app hotkey check, a tap aimed at the shoulder control labeled `L` may have sent the game's L input; I had misidentified it as L2. The first screenshot afterward was black, but after a 10-second observation the original “Die Einstellungen wurden gespeichert.” screen returned. PID `32726` stayed alive, logs still report `LangSelect`, and no photo/profile selection or confirmation was made. Do not repeat this tap. The S25 was untouched.

## 2026-11-22 effective 3DS scene layout at boot

`wantsTouchSurface` previously used the persisted layout (ONE SCREEN) to decide whether Azahar must wait for its second surface, even when a verified startup `SCREEN` scene had already promoted the effective layout to SIDE BY SIDE. That allowed the core to start on the top surface alone and attach the lower renderer later. The predicate now uses the same effective scene layout as `syncDsStage`, and a policy regression test covers SIDE BY SIDE startup versus top-only FIELD plus manual/automatic visibility.

- `:app:testDevUnitTest --tests=com.pokedaisy.app.DsStagePolicyTest --offline --no-daemon --console=plain` — PASS, 12 tests, 0 failures, 0 skipped.
- `:app:assembleDev -Ppokedaisy.n3ds=true -Ppokedaisy.n3dsX86_64=true -Ppokedaisy.x86_64=true --offline --no-daemon --console=plain` — PASS; APK `app/build/outputs/apk/dev/app-dev.apk`, 135,398,409 bytes.
- Not installed on the emulator, so the live lower-screen black-box observation is not yet resolved or rechecked. Ultra Moon's startup is initially UNMAPPED, so this change specifically fixes mapped startup SCREEN scenes (including X/Y) and does not claim a complete USUM fix. S25 was not touched.

## 2026-11-22 shared DS and 3DS save-folder routing

Audited the existing folder settings and launch path. `PokeDaisyActivity` resolves a ROM save with `SavesLocation.saveFor` before `EmulatorEngine.start`; the engine passes that exact file path to the native DS/GBA core. A configured shared `DS GAME SAVES` folder is searched first and prevents fallback beside the ROM. `3DS GAME SAVES` selects Azahar's complete user-data root; switching roots copies the existing tree only when the destination is empty and never deletes the old root.

- `:app:testDevUnitTest --tests=com.pokedaisy.app.SavesLocationTest --tests=com.pokedaisy.app.n3ds.N3dsSavesTest --offline --no-daemon --console=plain` — PASS, 9 tests total (7 DS save-location rules, 2 3DS save-format checks).
- This verifies path-selection logic and known metadata formats, not an end-to-end save write/reload on the physical S25. No device settings or saves were changed in this check.

## 2026-11-22 shared DS folder applies over prior per-game choices

The all-DS folder must also cover games with a previously saved library `SAVE FOLDER` path. While `DS GAME SAVES` is selected, `SavesLocation.saveFor` now suppresses that per-ROM override and routes the DS game to the shared folder. Selecting the shared folder copies existing saves from those per-game paths when the target filename is free; resetting the global folder restores the original per-game choices. The settings page explains this precedence. This avoids requiring a folder pick for each DS game while preserving the user's earlier per-game paths and files.

- `:app:testDevUnitTest --tests=com.pokedaisy.app.SavesLocationTest --offline --no-daemon --console=plain` — PASS, 8 tests, 0 failures/errors/skips, including global-over-per-game precedence and per-game restoration policy.
- The tests cover path policy only; the app was not installed on the emulator or S25.

## 2026-11-22 mapped 3DS Companion refresh cadence

- `:app:compileDevKotlin -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain` — PASS. This compile includes the N3DS session change from a 1,000 ms to a 200 ms Companion sampling interval.
- The 200 ms interval matches the Windows DS source's 12-frame Companion refresh at 60 FPS. It is a cadence comparison only; no USUM runtime map was added and no game-session behavior was claimed as verified by this compile.
- `:app:assembleDev -Ppokedaisy.n3ds=true --offline --no-daemon --console=plain` — PASS; ARM64 Azahar JNI packaged in `app/build/outputs/apk/dev/app-dev.apk`.
- `:app:testDevUnitTest --tests=com.pokedaisy.app.DsStagePolicyTest --tests=com.pokedaisy.app.n3ds.Gen6Test --tests=com.pokedaisy.app.companion.N3dsCoreTest --offline --no-daemon --console=plain` — PASS, 28 tests, 0 failures, 0 errors, 8 ROM-dependent skips.
- The APK was installed to the Galaxy S25 Ultra's `com.pokedaisy.app.dev` via `adb install -r`. Device reports `versionName=1.6.2-beta.1`, `versionCode=23`, and updated `lastUpdateTime`; the package data was not cleared. The `pokedaisy.xml` SHA-256 matched before and immediately after installation. On app launch, `LibraryActivity` resumed and `setup_done` / `last_rom` keys were present. The app's normal launch wrote the preferences file afterward, so its post-launch hash differs; their values were not dumped or changed manually.
- This was a library-start smoke check only. No ROM was launched; no gameplay, Ultra Moon scene mapping, or S25 touch interaction was verified.

## 2026-11-23 feature-enabled quick Dev unit suite

- Command: `./gradlew --project-prop=pokedaisy.n3ds=true -Pquick :app:testDevUnitTest --offline --no-daemon --console=plain` (run through the installed Git Bash wrapper on Windows).
- Result: PASS, 982 tests across 157 suites; 0 failures, 0 errors, 48 skipped.
- This compiles the current Dev Kotlin sources with N3DS enabled and runs the quick unit subset. It does not build/package native Azahar or replace S25 gameplay validation. `git diff --check` also passed; Git printed only existing LF/CRLF conversion warnings.

## 2026-11-23 Windows translation and generated-data checks

- Fixed `scripts/check_translations.py` to read Kotlin sources as UTF-8 explicitly; the host's default cp1252 had raised `UnicodeDecodeError` before the checker examined translation content.
- `python scripts/check_translations.py` — PASS on Windows without `-X utf8`: 2,126 entries, 1,808 used keys, 0 problems.
- `python -X utf8 scripts/gen_gamedata.py --check` — PASS: 5 generated files up to date, 125 configs, 336 records.

## 2026-11-23 full feature-enabled Dev unit suite

- Command: `./gradlew --project-prop=pokedaisy.n3ds=true :app:testDevUnitTest --offline --no-daemon --console=plain` (Git Bash wrapper on Windows).
- Result: PASS, 4,249 tests across 198 suites; 0 failures, 0 errors, 62 skipped.
- This is the full `app:testDevUnitTest` task with the N3DS feature property. It does not run Android-device gameplay or package native Azahar.

## 2026-11-23 S25 Ultra Dev update

- `:app:assembleDev` with `pokedaisy.n3ds=true` — PASS; APK SHA-256 `953E6B3BD062421DE78BD2EC7AFFD70748ADA02AB9F57FB8E3CFB1ACAD27BD35`.
- Installed to `com.pokedaisy.app.dev` using `adb install -r`; Android kept version code 23 / version name `1.6.2-beta.1` and recorded the new update time.
- The app was not running before installation. `shared_prefs/pokedaisy.xml` SHA-256 was identical before and after (`5dd6ce9f0a29bc38b8b7fb81c9474bdf9f13565fbea8d7ac2121225caa98151a`), confirming the existing preference file was preserved byte-for-byte by installation.
- Device remained Dozing with keyguard shown, so no app launch or gameplay input was sent. Pokémon Y / Ultra Moon runtime validation on the S25 remains pending an unlocked session.

## 2026-11-23 feature settings, full suite, and ARM64 native smoke

- `:app:testDevUnitTest` with `pokedaisy.n3ds=true` — PASS, 4,251 tests across 199 suites; 0 failures, 0 errors, 62 skipped. This includes `N3dsSettingRowsTest` (2/2), which verifies Azahar/Ultra Moon settings search and NEW-feature registration.
- `:app:assembleDev` with `pokedaisy.n3ds=true` — PASS; APK SHA-256 `E31AFFA21BF5B0CC4B288E97A1B92F7E393ACE8862A4D9A4FDBEA33457269A4B`.
- `python scripts/check_translations.py` — PASS, 2,126 entries, 1,808 used keys, 0 problems. `git diff --check` passed.
- `:app:connectedDevAndroidTest` with `N3dsNativeInitTest` — PASS on both the SM-S938B (Android API 36, ARM64) and the API 36 x86_64 AVD. `libpk_n3ds.so` loaded and `N3dsNative.init()` succeeded without a game ROM.
- **Device-test side effect:** the Gradle UTP log records uninstalling `com.pokedaisy.app.dev` and its `.test` package after the instrumentation run. This removed the Dev app's private data. The Dev APK was reinstalled with `adb install -r`, and the exact pre-test preference backup `work/s25-prefs-before-isolated-um.xml` was restored; its SHA-256 again matches `5dd6ce9f0a29bc38b8b7fb81c9474bdf9f13565fbea8d7ac2121225caa98151a`. The six canonical shared `.sav` files in `/storage/emulated/0/Emulation/PokeDaisy/saves` match the pre-test snapshots byte-for-byte. No game was launched and no ROM/save was opened by the instrumentation test. Private save-state files and other app-private data were not present in the available backups, so their preservation cannot be confirmed.
- Do not run `connectedDevAndroidTest` against the personal Dev installation again. Use an isolated AVD, or a separately validated manual instrumentation invocation that does not uninstall the target package.
