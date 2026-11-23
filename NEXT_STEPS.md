# Next Steps

## Active work plan (2026-11-24 00:41 CET)

The long-term 3DS goal remains open. This is the current execution plan; older numbered entries below are historical context
and must be reconciled against current evidence before following them.

### Active workstreams

- **Pokémon Y — root:** continue from the isolated game-created save in `work/y-headless-input-matrix/userdir`; German
  mirror/clothing sequence and second in-game save are complete. Reached the living room on the isolated clone, but the
  latest north/south/east and circle-pad probes in the hall did not produce an outdoor transition. Last checkpoint is
  `work/y-runtime-companion-capture/y-cpad-west.state`; the `n3ds_dump` process was stopped to release CPU. The isolated
  `main` remains SHA-256 `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`. Before another input, inspect
  the doorway/trigger or verify player coordinates so we do not repeat the same blocked directional probes. Never use the
  S25 save or untouched source copy.
- **3DS Save Editor:** read-only Ultra Sun/Ultra Moon trainer/party/PC-box viewing is integrated (`ab47ea16` and
  `78af8ac5`). Writes stay disabled because valid Gen 7 edits require RSA signing with a private key the project does not
  have. Existing DS/3DS routing uses the configured/default save directory and `<ROM basename>.sav`.
- **3DS core selector:** implemented and integrated as `46c570c2`, then pushed to `origin`; only the available Azahar core
  is offered while the stored AUTO preference remains compatible. Targeted core/translation tests passed in the agent
  worktree. No unready Panda3DS or Libretro backend is presented as production-ready.
- **UI/layout:** completed in `../pokedaisy-worktrees/layout-policy`; focused `DsStagePolicyTest` passed 17/17, and no
  change was justified because portrait/landscape, explicit layouts, touch auto/manual, PIP and second-display behavior
  are already covered.
- **Ultra Moon — `usum_runtime_analysis`:** completed read-only analysis. Candidate addresses come from the local
  PKMN-NTR reference; none is runtime-verified. Preserve the Kukui photo/profile stop. If the live pipe is unavailable,
  use a separate isolated process/checkpoint with only `modules`, `regions`, `hex`/`peek`, `shot`, and `savestate`.
- **Upstream/CI — root:** advisory Android 3DS CI job is implemented in `.github/workflows/ci.yml`, syntax-parsed, and
  committed/pushed as `268de665`; observe a GitHub PR run when one exists. Keep it out of signed release calls while
  Azahar licensing and provenance remain unresolved.

### Verified state and blockers

- Pokémon Y's private in-game save is `415232` bytes, SHA-256 `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`.
  `main-after-clothes-save` matches it byte-for-byte; the source copy remains `0D1D76657703452704D84BE800762F7D9522A43F610B02C691182D7F411D95F2`.
  Headless savestates/screenshots remain under `work/` and must not be committed.
- S25 Dev APK built by `:app:assembleDev -Ppokedaisy.n3ds=true -x :app:licensesAsset` and updated with `adb install -r`.
  Package data was retained (`firstInstallTime=2026-11-23 18:29:14`, `lastUpdateTime=2026-11-24 00:34:54`). `versionCode`
  remains 23. Launcher process stayed alive and no AndroidRuntime crash appeared, but screenshot showed the AOD/lockscreen;
  no game was opened and no input or save change was made. Do not wake or unlock the S25; direct gameplay validation waits
  until it is already unlocked and available.
- No populated Y party capture exists yet. Ultra Moon still has no verified gameplay Companion map; the PKMN-NTR
  addresses remain hypotheses. Never pass the user's Kukui photo/profile stop without renewed authorization.
- USUM parser errors and localized ROM-data failures are mapped in all eight languages. PKHeX's BEEF footer byte order
  (`46 45 45 42`) has a regression assertion. Focused `Gen7UsUmSaveTest` passed in the agent worktree; root
  `:shared:testDebugUnitTest --tests com.pokedaisy.app.companion.i18n.TranslationsTest` passed after making device locale
  deterministic. `scripts/check_translations.py` reports 2,131 entries, 1,814 used keys, 0 problems.
- GitHub Action execution, real-device gameplay rendering, Azahar license/notice resolution, and final upstream diff
  review remain open.

### Pokémon Y movement anti-loop

Confirmed: the isolated game remains in the living-room/hall scene (`DllField`, `DllUSMirror`). Repeated D-pad cardinal
and circle-pad cardinal inputs changed facing/camera but did not trigger an outdoor scene transition. The game-created
`main` save did not change; only private Savestates were written. Do not repeat blind cardinal probes.

- Current hypothesis: the character is at a collision boundary or the exit trigger is on a different side of the hall.
- Alternative: the mother/neighbor intro event still requires an interaction or acknowledgment before the field exit opens.
- Alternative: the harness binding is accepted but movement is being suppressed by the scene/event state; verify with a
  small before/after player-position memory observation after locating a candidate position field.
- Alternative: the house exit is a bottom-screen touch or a specific interaction trigger; inspect the current Field CRO
  and earlier scene screenshots before another input.

Next diagnostic: inspect the exit geometry/trigger in the Field CRO or establish a player-position delta from two existing
savestates. Only then send one targeted input and confirm a module/zone transition. Save through the game after outdoors.

### Next actions

1. Continue Pokémon Y from the latest useful private savestate; save again after the first outdoor/starter milestone.
2. Capture and decode Y party, MyStatus, bag, zone, menu, and scene state from a populated isolated runtime.
3. Keep the USUM editor read-only until safe signing exists; review whether a future signing design is feasible before
   claiming editable Gen 7 saves.
4. Apply the Ultra Moon sparse-read findings only when supported by a new read-only observation; preserve its setup stop.
5. Resume the Y isolated checkpoint only after a distinct doorway/position diagnostic. Verify physical-device save hashes
   before and after any authorized game test; never drive while the device is locked. Update this plan at the next milestone
   or before context compaction.

1. Build a legal 3DSX homebrew smoke path. The Azahar loader has 3DSX support, but PokeDaisy currently routes only recognized decrypted retail carts to `N3dsSession`. Keep the smoke harness dev/test-only unless general homebrew support is intentionally designed.
2. Keep the S25's existing Ultra Moon setup/game state and save unchanged; do not make profile/photo selections. Continue runtime mapping only from isolated copies. A recovered isolated headless checkpoint currently reports `FieldRo` / `FieldMenu` / `FieldEffectCommon`; an X press did not visibly open a menu. The latest device launcher attempt left Netflix's account-confirmation screen in front; do not interact with it during emulator checks.
3. Extend physical-device validation to audio, broader controls, pause/resume, save lifecycle, and stability without changing the original ROM.
4. Validate X/Y single-screen UI on ARM64 hardware when the user has the S25 available, including the API 36 touch-surface hide path, transitions, battle controls, overlays, touch, audio, saves, memory, and stability. Capture screenshots/logs and record device conditions. The ARM64 AVD cannot run on the current x86_64 host; also determine a verified hide/show fallback for Android API 35 and below.
5. Keep the full unit suite green after follow-up changes. The 2026-11-22 run passed 4,213 tests with 0 failures and 60 skips across 191 suites; rerun it after the next broad change.
6. Reconcile code defects against current upstream behavior; keep changes focused and add regression coverage. Run relevant tests, standard and feature-enabled builds, and `git diff --check` before committing.
7. Commit validated changes and push only to `origin`. Review fork diff and licensing before drafting (not submitting) an upstream PR; request user approval before submission.
8. For a complete official-to-Dev settings migration, add an export to an official signed build or use a user-provided export/root-enabled device; current Android package isolation prevents direct extraction. Do not overwrite or reset either app's data.
9. Build and run the existing `native-capture/n3ds_dump` once, against a scratch Azahar user directory and a read-only mount of the user's Ultra Moon image. Discover its CRO root/modules and inspect memory without changing the live save or advancing past the photo question.
10. Derive and validate Ultra Moon party, trainer, location, field-menu, battle-menu and battler mappings from available runtime evidence before enabling Companion samples or automatic lower-screen behavior. PKMN-NTR provides candidate trainer (`0x33012818`), bag (`0x33011934`) and party (`0x33F7FA44`, alternate `0x330128E4`) addresses; read them sparsely from the emulator and verify PK7 checksums/initialized trainer fields before adding a production config. If a required state cannot be observed before the user's stop point, continue with source analysis and request permission before progressing through that setup screen.
11. Once Ultra Moon behavior is verified, extend the same adapter architecture to Ultra Sun, Sun/Moon and X/Y/ORAS only where each title's addresses are separately validated. Keep the S25 Ultra's package data intact and use `adb install -r` for any authorized Dev update.
12. Do not run `:app:connectedDevAndroidTest` through Gradle UTP on the personal S25 install: the 2026-11-23 UTP log showed it uninstalls the target package and its app data. Use the dedicated AVD or validate a manual instrumentation invocation that leaves `com.pokedaisy.app.dev` installed.
# Next: finish measured USUM runtime mapping

The isolated Ultra Moon run verifies the EU root `0x008b2000`, setup modules, and the playable `FieldRo` module. The
Dev app keeps setup `SCREEN` pages automatic but leaves the module-only field lower screen manual because no touch
request is verified and the isolated bottom buffer is black. Next, correlate runtime field/menu/battle signals with
paired screenshots, then validate party, bag, trainer, and location reads before adding a USUM `Gen6Reader.Config`.
Preserve the stop before Kukui's photo selection; do not make the photo/profile choice.

## Ultra Moon intro continuation (2026-11-23)

The isolated `userdir19` session advanced from the German “Drei Monate später…” message to an Alola night-map flyover after one A press. A further 420 frames kept `AppDemo` / `SequencePlayer` / `Object` loaded while the map camera zoomed toward the island; the lower framebuffer remained black. Current checkpoint: `work/usum-scene-investigation/ultra-moon-map-wait300.state` at frame 15,219, with the screenshot beside it. This still does not prove a gameplay or touch-needed state. Continue the intro only up to Kukui's photo/profile question, then stop without making a personal choice. Do not add an automatic mapping for the shared module trio.

12. Current x86_64 emulator session is at Kukui's photo/profile setup view after the authorized German choice. Do not confirm or choose a photo/profile. Continue source analysis and mapping work without sending game input; resume gameplay only after the user authorizes that setup choice.

The current AppDemo/photo-view heap comparison is non-diagnostic for game state: two snapshots differ in 86,455 bytes, but no reliable state marker was identified, and the Sun/Moon zone offset is not validated for Ultra Moon. The emulator process is currently still running on `emulator-5556` (PID 26486); its latest scene log moved from startup `SCREEN` to `UNMAPPED`. Continue with read-only CRO/module and sparse address-candidate analysis; do not send input or turn the heap diff into a memory mapping. The S25 is not part of this pass.

## Ultra Moon static data follow-up (2026-11-22)

USUM now has ROM-verified static Gen 7 data routing and island-map extraction, including its distinct German text indices and map archive path. Continue with runtime CRO/module and live-memory analysis without advancing the emulator past the profile/photo prompt. After a verified in-game memory map, test Companion state and automatic lower-screen behavior; static ROM parsing alone does not satisfy DS parity.

## 3DS core options follow-up

Research confirmed Azahar publishes an Android ARM64 Libretro core, but PokeDaisy's native app does not host Libretro and Azahar's build configuration treats Libretro as a separate frontend variant. Next, prototype the minimum Android host path without replacing the current native Azahar integration: EGL/GLES context and video delivery, callback-driven audio, input/touch, environment options, per-title save/user paths, and lifecycle. Determine whether the result adds practical behavior over the existing native Azahar path; if not, retain one honest core option and investigate an actual alternate maintained emulator.

## Live-session correction (2026-11-22)

Current `emulator-5556` PID is `32726` (not the stale PID `26486` above). Read-only capture confirms the game is still rendering, but the visible dialogue is “Die Einstellungen wurden gespeichert.”; treat the precise intro stage as uncertain. Do not send gameplay input. The live sparse candidate capture is in `work/usum-candidates-1795357810421.bin` with its TSV. Primary party candidate `0x33F7FA44` is empty; alternate `0x330128E4` does not yield PK7-checksum-valid slots. Do not activate these mappings. Continue source/runtime analysis without changing the game setup.

## Isolated Ultra Moon headless resume (2026-11-23)

Separate from the x86_64 Android emulator above, the existing Docker `n3ds_dump` process `558d93005ab6` runs the Ultra Moon ROM with isolated user directory `work/usum-scene-investigation/userdir19`. Its latest checkpoint is `ultra-moon-three-months-later.state` at frame 14,555. The screenshot is on the German “Drei Monate später…” title sequence; CRO modules remain `AppDemo`, `SequencePlayer`, and `Object`. One A press advanced from the title to this message, but repeated short steps only faded it to/from black. No profile/photo choice was made. Keep this run separate from the live Android emulator and real saves; next, inspect the game's sequence/CRO behavior or improve headless capture throughput before attempting additional runtime mapping. Stop before any photo/profile selection.

Read-only ROMFS CRO inventory now shows `AppDemo` importing `SequencePlayer`, which imports `Object`; `HeroSelect` imports `StrInput`. Each module exports `nnroControlObject_`; this inventory exposes module relationships but no verified runtime state offsets. The repeatable output is in `work/usum-scene-investigation/rom-cro-symbol-inventory.txt`, generated by `work/usum-cro-symbols.py`. The 3DS ARM disassembler from the installed Android NDK is available for focused follow-up on these modules.

## Next: 3DS gameplay parity and safe Ultra Moon continuation

- Do not confirm Ultra Moon's current permanent language prompt while using the existing shared save. Clone the save to an isolated test path before resuming setup; stop before the persistent trainer photo/profile choice.
- Continue investigating verified Ultra Moon gameplay memory roots / scene symbols from the isolated userdir. Do not mark Ultra Moon `SUPPORTED` or publish speculative `Gen6Reader` offsets.
- Exercise the new shared touch-window fit with Y and Ultra Moon in manual / auto-requested layouts using isolated save copies; compare both screen visibility and Companion telemetry against DS behavior.
- Keep the physical-device preference and six shared save hashes under observation. Avoid `connectedDevAndroidTest` on the personal S25 because UTP uninstalls its target.
