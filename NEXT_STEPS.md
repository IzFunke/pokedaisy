# Coordination update (2026-11-24 01:46 CET)

- **Ultra Moon — input mapping:** the Pokemoon reverse-engineered EU Moon consumers confirm GFL masks 16/32 are bitmasks, but no available source maps them to physical buttons; no controls were guessed. See RESEARCH.md for exact symbols and links.
- **Ultra Moon — headless progression:** `userdir20-run4` was hash-verified before/after; `wait 1` did not return within 32 s, so the isolated process exited cleanly without input. `RunLoop()` defaults to a full CPU slice, and `wait N` counts completed renderer frames, not instructions. Run3 stayed at frame 0 because it had no advance command; that was not proof of deadlock. The optional `N3DS_WAIT_PROGRESS=1` logger is integrated as commit `333391af`; the Linux Docker build linked `n3ds_dump` through step 1507, with only existing Azahar/GCC warnings. Runtime validation remains to be done on a fresh isolated clone using the logger.

- **Ultra Moon — confirmed stop:** the isolated `wait 120` eventually returned at frame 559. The same exterior scene and `FieldRo FieldMenu FieldEffectCommon` remained; slot 0 at candidate `0x33F7FA44` was still zero. The process exited through the harness shutdown path. `userdir20/main` still exactly matches its pre-input backup (445,440 bytes; SHA-256 `90A3A00E0A4D9BCC05164BB54AF3800A996943F5CCDA4800A7E7D41DB35E1D725`). No save or other userdir changed. Next run is on a new clone and will use one short, observed input at a time toward the starter.
- **Pokémon Y — static follow-up complete:** `DllFieldDemo.nnroControlObject_` at code `+0x1d98` is a local five-case dispatch, with no renderer call. `SetIsCallDraw(bool)` at `+0x1d5c` only stores a flag. The 24 `DllSequence` imports are anonymous, and local `DllBackGround.cro` / `DllField.cro` are missing; neither bottom-screen requirement nor temporary non-rendering is proven. No screen-policy or test change is justified, and no more A/B probes will be repeated.

- **Ultra Moon live run — earlier checkpoint:** after the one-frame A press, `userdir20` was still at frame 439 with `FieldRo FieldMenu FieldEffectCommon`. The bounded idle then returned at frame 559; final state and cleanup are recorded above.
- **Goal coordination:** the persistent goal attachment already contains exactly one `## 8. Ergänzung: Effizientere Multi-Agent-Koordination` section; it was left unchanged to avoid a duplicate.

- **Pokémon Y:** static ROMFS extraction confirms `DllFieldDemo.cro` imports `DllSequence`; it still does not establish which display that scene needs.
- **UI/layout — `ui_3ds_parity`:** the follow-up review is complete in its separate worktree with no changes. `sceneOf` deliberately defaults unrecognized modules to `SCREEN` to avoid hiding a potentially required touch page. Treating `DllFieldDemo` as `NOT_SCREENS` would not by itself guarantee a safe scene transition because a null classification preserves the prior scene.
- **Ultra Moon:** the before/after touch-state candidates remain unchanged and do not establish menu/battle behavior.
- **Lead — root:** keep current scene policy and user data unchanged. Next required evidence is paired top/bottom frames with the Y module trio stably loaded, plus the scene/touch request immediately before and after its transition. A black fade frame alone is inconclusive.

No product code changed in the classification review. The goal remains active; do not submit an upstream PR or infer new RAM mappings from the module names.

---
# Coordination update (2026-11-24, resumed)

## Active workstreams

- **Pokémon Y — `pokemon_y_route`:** `DllFieldDemo`/`DllSequence` CROs show `CommandKeyWait` but no touch symbols. One-frame A loads `DllSequence`; one-frame B changes modules to the exact trio `DllSequence DllBackGround DllFieldDemo` at frame 17. A second B1 at frame 19 leaves the trio/zone/coordinates unchanged and the raw buffers black. Isolated main hash `0D1D7665…D95F2` is unchanged. Headless Azahar key indices are A/B/Up/Down/Left/Right=`0/1/4/5/6/7`; Android masks are `0x01/0x02/0x10/0x20/0x40/0x80`. These transport IDs do not decode GFL scene-input semantics. Do not repeat these pulses without a new event/checkpoint; no long holds/waits, in-sequence savestates, game save, or S25 input.
- **Ultra Moon — `ultra_moon_integration`:** the `userdir26` intro run is stopped at `AppDemo SequencePlayer Object`; German persisted there, no profile/photo choice or in-game save. Its 600-frame A-every-60 batch did not reach `FieldRo`; never repeat it. Static CRO checks show GFL edge-trigger IDs 16/32, but no local definition maps them to physical buttons; do not guess. New live evidence from the separate isolated `userdir20` at frame 437 confirms `0x33012818` trainer block in two identical reads: language `5` (German), UTF-16LE name `ELL`. Two identical reads from party candidate `0x33F7FA44` decrypt to checksum 0/species 0 (empty-looking, not a non-empty party validation); bag candidate stays zero. The agent is now backing up `userdir20/main` and progressing only that isolated copy toward a starter for a real PK7 validation. Source hash is `90A3A00E…35E1D725`; S25 untouched.
- **Ultra Moon source audit — `ultra_moon_integration`:** completed without code changes. Existing code recognizes `LangSelect`, `HeroSelect`, `StrInput`, and `FieldRo`; no USUM gameplay `Config` is justified because party/trainer candidates remain unvalidated.
- **Architecture/upstream — `upstream_fixes`:** completed a targeted audit in its own worktree. It found no safe compliance fix to make without resolving the existing Azahar license/provenance blockers. The original requirements and current limits are documented in `docs/3DS_UPSTREAM_READINESS.md` and `docs/3DS_AZAHAR_LICENSE_AUDIT.md`.
- **UI/layout — `ui_3ds_parity`:** completed a fresh source/test review in its own worktree with no change. Layout transitions already share `dsStageLayout`; the remaining DS per-frame fade flags versus 3DS scene polling are core-specific, and changing FIELD or battle-menu visibility without runtime evidence could hide required game content.
- **Lead — root:** keep the user branch and save data intact; review agent evidence and make the next product change only where a concrete behavior is verified. Do not run Gradle builds concurrently with agents.

## Live resource and safety check

- Root branch baseline: `feature/azahar-3ds-single-screen` at `916dd4a8`. This turn changes only `NEXT_STEPS.md` and `RESEARCH.md`; the existing ignored/untracked `work/` diagnostics remain excluded. Do not commit `work/`.
- The S25 appears in `adb devices`, but `dumpsys power` reports `mWakefulness=Dozing`; do not wake or send game input. Existing app process is present, but this is not evidence of a currently displayed game state.
- A read-only Android check confirms the resumed activity is `com.pokedaisy.app.dev/LibraryActivity`; the last 500 `AndroidRuntime` log lines contain no crash. No game is open, and this does not validate Pokémon Y/Ultra Moon runtime behavior.
- The Android emulator is awake but has no resumed PokeDaisy app process. The separate Docker-backed headless Ultra Moon process was found later and remains isolated from the Android emulator.
- Pokémon Y's established isolated in-game save at `work/y-headless-input-matrix/userdir/.../main` remains SHA-256 `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`. The separate recent A/B probe clone at `work/y-runtime-companion-capture/userdir/.../main` remains `0D1D76657703452704D84BE800762F7D9522A43F610B02C691182D7F411D95F2`. Both were untouched; no further test should substitute one for the other.
- Ultra Moon setup and field-idle module evidence is available, but the published candidate menu/battle/party addresses remain unverified. Do not enable those mappings until the isolated gameplay sample validates structure/checksums and repeats.

## Immediate next steps

1. Keep the Y screen policy unchanged; the CROs and black raw frames still do not prove lower-screen or touch semantics. Resume gameplay only from a verified safe event/checkpoint, without repeating the established A/B pulses.
2. For Ultra Moon, do not guess physical buttons for GFL trigger IDs 16/32. Seek a trustworthy labeled mapping or wait for a naturally reached gameplay checkpoint where a known control can be tested on the isolated clone.
3. Implement title-state/Companion mappings only after repeated structure/checksum validation. Add focused regression tests, all-language strings, and manual updates with any visible feature change.
4. When a code change is ready, reserve the root Gradle output directory, build, and install with `adb install -r` only when the S25 is awake/appropriate. No upstream PR without explicit approval.

---
# Coordination update (2026-11-24 01:43 CET)

- **Lead — root:** statically extracted Pokémon Y's `DllFieldDemo.cro` to ignored `work/`; it exports `nnroControlObject_` and the shared background callback, and imports `DllSequence`. No runtime/game input was used for this extraction.
- **UI/layout — `ui_3ds_parity`:** reopened in its own worktree to evaluate this specific new classification case. It will change policy only if the evidence supports preserving DS-style touch behavior without hiding required 3DS touch pages.
- **Ultra Moon:** state comparison is complete. Existing before/after touch checkpoints do not change the three candidate words; a real menu/battle sample is still needed.
- **Pokémon Y:** scene remains unresolved after the black sequence; the user save is unchanged. Do not repeat the same A/wait probes.

The extracted file inventory and symbols are in `work/y-romfs-cro-analysis.txt`; no ROM or generated CRO is tracked. The branch remains `feature/azahar-3ds-single-screen`, with only the existing `work/` diagnostics untracked.

---
# Coordination update (2026-11-24 01:41 CET)

## Workstream status

- **Pokémon Y — `pokemon_y_route`:** static/runtime review complete. No Y `DllFieldDemo.cro` export inventory exists yet. `DllFieldDemo` is omitted from `Gen6Reader.NOT_SCREENS`, so the current generic scene fallback classifies the observed `DllSequence DllBackGround DllFieldDemo` trio as `SCREEN`; whether that is correct remains unverified. Do not change the policy from a module name alone.
- **Ultra Moon — `ultra_moon_mapping`:** complete. Existing before/after touch savestates have identical candidate bytes; a confirmed X-menu, battle, or bag transition remains necessary.
- **UI/layout — `ui_3ds_parity`:** complete in its worktree with no code change; shared layout policy/test coverage is present, while runtime scene detection remains title-specific.
- **Lead integration — root:** continue with a static Y CRO/control-flow pass, then take a focused code change only if the field-demo screen semantics can be established.

## Y anti-loop evidence

The headless frame counter advanced while `DllSequence DllBackGround DllFieldDemo` remained loaded, but screenshots stayed fully black. The prior A and wait probes are documented and must not be repeated. The last safe resume point is `work/y-runtime-companion-capture/y-after-dark-dialogue.state`; do not save states after the sequence modules load. The isolated save hash remains `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`.

## Next steps

1. Inspect the Pokémon Y ROMFS for its `DllFieldDemo` asset and its CRO imports/exports, using read-only extraction in `work/`.
2. If that does not establish scene semantics, compare the pre-sequence safe state to the available runtime state without repeating inputs, or wait for safe physical-device access.
3. Leave `Gen6Reader.NOT_SCREENS` unchanged until the title-specific behavior is verified. Keep all existing user data, S25 app data, and the branch intact.
4. Keep Pokémon Y and Ultra Moon at equal priority; no speculative mappings or upstream PRs.

---
# Coordination update (2026-11-24 01:36 CET)

## Active workstreams

- **Pokémon Y — `pokemon_y_route`:** read-only static/CRO investigation of `DllSequence DllBackGround DllFieldDemo` and the black transition after leaving the house. No further emulator inputs until a distinct evidence-based route is identified.
- **Ultra Moon — `ultra_moon_mapping`:** comparison complete in a separate scratch process. The pre/post touch states had identical modules and candidate words; no production signal was verified. No code change.
- **UI/layout — `ui_3ds_parity`:** review complete in its own worktree. No narrow code fix was justified because shared layout policy and regression tests already cover basic 3DS single-screen behavior; the gap is verified title-state detection.
- **Lead integration — root:** update durable research/plan, then resume implementation when source/runtime evidence identifies a safe, testable change.

## Current state and guardrails

- The Y isolated `main` hash is still `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`; the process is stopped on the pre-sequence safe checkpoint. Do not repeat the same black-screen wait/A probes or savestate inside `DllSequence`.
- Ultra Moon field-idle and pre/post touch captures do not validate the candidate scene words. Do not infer X-menu, battle, or bag behavior; preserve all existing setup/profile stops.
- The S25 is locked and has not been touched this pass. Preserve app data and saves; no uninstall/reset, no personal-save changes, no ROM/save/key commits, and no upstream PR.

## Next steps

1. Finish the independent Y static sequence/CRO analysis. Resume the isolated checkpoint only if it supplies a materially different diagnostic.
2. Seek a confirmed Ultra Moon X-menu/battle/bag sample only within the already authorized isolated test context; otherwise continue static code/reference analysis without enabling guessed addresses.
3. Make the next code change only when a concrete behavior defect is established, keep new UI strings in all eight locales, and update the manual for visible behavior.
4. Recheck this plan before a context handoff; Pokémon Y and Ultra Moon remain equal priorities.

---
# Coordination and runtime update (2026-11-24 01:35 CET)

## Active workstreams

- **Pokémon Y — `pokemon_y_route`:** read-only static/CRO investigation of the transition into `DllSequence DllBackGround DllFieldDemo`; determine whether a safe, evidence-based continuation exists. No emulator input or file edits.
- **Ultra Moon — `ultra_moon_mapping`:** read-only comparison of saved isolated field/menu/touch states at the three PKMN-NTR candidate words (`0x3067107C`, `0x306A60EA`, `0x3066EB54`), using a separate scratch userdir if a process is needed. No frames, keys, touch, or save writes.
- **UI/layout — `ui_3ds_parity`:** review complete in its own worktree. No change was justified: existing `DsStagePolicy` shares layout transitions and already tests 3DS single-screen behavior; a live title-state mismatch is still needed before changing the shared policy.
- **Lead integration — root:** preserve the isolated gameplay checkpoints, reconcile CRO/runtime evidence, and implement only after a concrete signal/behavior is verified.

## Pokémon Y evidence and anti-loop boundary

The isolated in-game `main` remains SHA-256 `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`; it has not changed since the saved in-game checkpoint. The route reached the downstairs entrance rug from the upper room. Movement is now measured: X at `0x08C670AE`, Y at `0x08C670B6` (not `0x08C670B8`); a 15-frame LEFT changed X `0x4404 → 0x4400`, and four 15-frame DOWN steps changed Y to `0x4428`. Zone remained `2` while inside.

At the entrance, DOWN stopped changing position and the field-hold word at `0x08D03400` read `0x3D3`. A 15-frame A hold changed it to `0x120`; after 20 frames the modules became `DllSequence DllBackGround DllFieldDemo` and the hold word became `0x100`. The captured image stayed black through another 60 frames and a 20-frame A hold; module/zone state did not leave that sequence. This is the current anti-loop limit: do not repeat the same wait/A probes.

Safe resume checkpoint: `work/y-runtime-companion-capture/y-after-dark-dialogue.state`, before the sequence modules loaded. Do not savestate once `DllSequence`/`DllFieldDemo` is loaded: the harness throws `boost::archive::archive_exception: unregistered class`. Do not run the older `y-bedroom-exit2.state` again: its loaded process SIGSEGV'd during `wait 10`. Both failures affected only isolated headless state; the in-game `main` hash above is unchanged. The Y process is stopped.

## Ultra Moon passive baseline

At isolated field-idle frame 437, modules were `FieldRo FieldMenu FieldEffectCommon`; raw bytes were `9D FF 9D FF` at `0x3067107C`, `6F 43 50 51` at `0x306A60EA`, and `55 AD 8D 73` at `0x3066EB54`. The paired screenshot shows field on top and a black lower framebuffer. This confirms only field-idle; it verifies no scene/menu/battle predicate. The process remains on that frame with no user save changes.

## Next steps

1. Finish the independent static Y sequence/CRO analysis and the read-only Ultra Moon saved-state comparison.
2. If a reliable Y transition test is found, resume from the safe checkpoint; otherwise switch to static CRO/control-flow evidence or wait for an already-unlocked S25 session. Keep all tests on the isolated copy.
3. Integrate no speculative memory address or screen policy. Preserve all existing DS behavior and the S25 app data.
4. Continue translating every new player-visible setting/message in all eight locales and update the user manual for any newly shipped behavior.

---
# Coordination update (2026-11-24 00:58 CET)

The persistent goal now includes the user's multi-agent coordination rules. Its pre-existing bytes were verified unchanged; the appended section occurs once (goal SHA-256: `7DA64A5704FD8560D65B73CB9427EFBE4211CFB60FF8CF186B595E9858829527`).

## Active parallel work

- **Pokémon Y route — `pokemon_y_route`:** read-only analysis of existing bedroom/hall checkpoints and static trigger evidence; no game input, save writes, or device use.
- **Ultra Moon mapping — `ultra_moon_mapping`:** read-only analysis of existing captures, CROs, and local references; preserve the Kukui photo/profile stop; no game input or save writes.
- **DS/3DS UI parity — `ui_3ds_parity`:** independent review and, only if a concrete parity defect is proven, a narrowly scoped implementation and regression test in its own worktree; no device or ROM/save use.
- **Lead integration — root:** reconcile agent evidence with current architecture and continue the implementation in focused steps. Current source/docs review confirms the Android route is native Azahar via `N3dsSession`/JNI; Companion rendering and `SnapshotView` are shared, while 3DS scene/memory classification remains title-specific and incomplete for Ultra Moon.

## Current blockers / constraints

- The S25 is not available for gameplay validation while locked; do not wake/unlock or send input. The installed Dev update preserved app data.
- No active `n3ds_dump` process was found on this host. Existing checkpoints under `work/` are the evidence base until a fresh isolated run can be started safely and productively.
- Pokémon Y and Ultra Moon remain equal priority. Never commit ROMs, saves, keys, or `work/`; no save edits, no uninstall/data reset, and no upstream PR without user approval.

## Immediate next steps

1. Review each agent's concrete evidence on completion; ask for a next independent task only when useful and non-overlapping.
2. Continue the Y and Ultra Moon runtime/data mapping from isolated copies/checkpoints, distinguishing verified addresses from hypotheses.
3. Integrate any validated UI parity change from its own worktree and run its focused regression checks.
4. Keep all new options/errors localized in all eight app languages and maintain user-facing manual/docs per `CLAUDE.md`.
5. Update this coordination block at the next milestone and before any context handoff.

---
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

## Coordination update (2026-11-24, headless logger resumed)

- The active persistent goal attachment already contains exactly one `## 8. Ergänzung: Effizientere Multi-Agent-Koordination`; no duplicate was added.
- **Ultra Moon — root:** isolated `userdir20-run6` is cloned from the unchanged 445,440-byte save (`90A3A00E0A4D9BCC05164BB54AF3800A996943F5CCDA4800A7E7D41DB35E1D725`). German was confirmed by “Die Einstellungen wurden gespeichert” and German intro dialogue. The exact photo question now appears: `Welches dieser Fotos stammt gleich noch mal von dir?`. No photo grid was opened and no answer was selected. The emulator was stopped immediately after creating `work/usum-scene-investigation/run6-photo-question.state` at frame 1487; no more input is authorized beyond this prompt. The candidate RAM language byte remained 2 in this setup scene and is not accepted as a valid mapping.
- **Headless harness — root:** added opt-in `N3DS_STEP_RUNLOOP=1`, time/call/frame progress for `wait`, `step N`, and `waittight N`. A default full-slice `wait 1` stalled on its second `RunLoop()` at first boot. Single-instruction stepping rendered frames but was slow; after loading a valid AppDemo savestate, `waittight 60` completed and returned 60 frames in about 46 seconds. Keep all these probes on the isolated clone. The n3ds_dump target rebuilt successfully in the Linux Docker builder; only existing Azahar/GCC warnings appeared.
- **Pokémon Y — `y_companion_mapping`:** separate-worktree review found `Gen6Reader.configFor()` routes Y through the X config; existing `Gen6Test` already covers Y early-game empty party/bag, language/zone, PSS/menu/field scene and Y-specific ROM data. No new memory mapping or screen policy is justified without additional runtime evidence; no files changed.
- **UI/layout — `layout_parity_tests`:** `DsStagePolicyTest` already covers transitions across portrait/landscape, saved layout restoration, hidden/automatic touch and shared DS behavior. No distinct gap or change found.
- **Settings/save-path maintenance:** the 3DS core subtitle had translations in all eight locales but was not wrapped in `tk()`, so literal-check coverage missed it. Commit `93830873` fixes registration without changing visible wording; focused `TranslationsTest` passed and `scripts/check_translations.py` reports 2,131 entries, 1,815 used keys, 0 problems. The save-path agent verified the shared resolver/default folder/ROM-basename `.sav` path and existing `N3dsSavesTest` coverage; no speculative change was made.
- **Photo-choice stop gate — `usum_photo_gate`:** the exact safe stop is the visible German prompt `Welches dieser Fotos stammt gleich noch mal von dir?` in `work/usum-intro-progress/photo_choice.png`, before the photo grid. Send no more input once this text appears. `AppDemo SequencePlayer Object` is used both in ordinary narration and at the prior prompt, so it is not a discriminator; `HeroSelect`/`StrInput` are later and therefore too late as a safety gate. Static CRO trigger IDs are not physically mapped.
- **Active workstreams:** root stopped Ultra Moon at the user's explicit photo-choice boundary; `pokemon_y_headless_playthrough` is progressing a separate isolated Y clone toward a starter checkpoint. Layout, translation, save-path, Y evidence, and photo-gate agents completed their bounded tasks in separate worktrees. No S25 build/install/input was performed.
- **Next:** preserve the Ultra Moon photo prompt until the user reauthorizes crossing that choice; continue Y from the isolated clone and validate a populated party after an in-game save. The harness and translated subtitle fix are already on the feature branch; implement only mappings supported by repeatable evidence. No upstream PR.

## Coordination update (2026-11-24, resumed investigation)

- The requested attachment was reread; its single coordination section remains present. No app settings, app data, ROMs, or source saves were changed.
- **Pokémon Y:** the isolated no-input run reached frame 540 and is still in `DllTitle DllBackGround DllSequence`, transitioning through the Y logo. The Y agent plans one START pulse, then will inspect the title/menu before any continuation choice. No input or save write has occurred yet.
- **Ultra Moon:** a focused read-only follow-up found no safe `FieldRo` battle/party/bag predicate. The existing frame-437 capture validates trainer data at `0x33012818` (German language byte 5 and trainer name `ELL`), but candidate party `0x33F7FA44` is empty and bag `0x33011934` is zero. Relevant gameplay CROs were not extracted, so no positive gameplay contrast exists. Do not map these candidates or advance beyond the saved photo-question prompt.
- **Upstream delta review:** the recent harness diagnostics and `tk()` subtitle fix have no actionable `CLAUDE.md` / `docs/DEVELOPMENT.md` deviations; no files changed.
- **Local build check:** `:app:compileDevKotlin` completed. The full `:app:testDevUnitTest` run was started by mistake and stopped after the project rule was identified; it did not complete. Future validation must be limited to named focused tests unless the user approves a full run.
- **Active workstreams:** Y isolated intro/runtime evidence; Ultra Moon static module investigation; root integration and state protection. No device test or upstream PR.

## Validation update (2026-11-24)

- Focused tests passed: `:app:testDevUnitTest --tests '*N3dsSceneTransitionTest' --tests '*DsStagePolicyTest' -Pquick --no-daemon` (23 tests, zero failures).
- `:app:compileDevKotlin` had already completed successfully during the interrupted full-suite attempt. That full suite did not finish and was not retried; no broad run was authorized.
- Ultra Moon follow-up is now searching read-only ROM/ROMFS archives for the missing gameplay CROs so the earlier static-analysis blocker can be addressed without touching the game state.

## Session update (2026-11-24, Y transition stall and CRO recovery)

- **Pokémon Y:** the isolated title reached German `Weiter` with existing profile `lzFunk3`; the private recovery state is `work/y-y-runtime-evidence-menu.state` at frame 658. One A was sent, but the menu faded black; no in-game save was written and the source save remained hash-identical to its clone (`9B68354B…EE100`, 415,232 bytes). A no-input `wait 120` stalled at frame 844 after >90 seconds, so it was interrupted. The next attempt will restore frame 658 in a fresh isolated userdir and use the measured `N3DS_STEP_RUNLOOP=1` / bounded `waittight 60` path. Do not send further input if that stalls.
- **Ultra Moon:** the archive blocker is cleared: `work/extract-usum-gameplay-cros.py` extracts EU `FieldRo.cro`, `FieldMenu.cro`, `Battle.cro`, `Bag.cro` and related CROs into ignored `work/usum-static-gameplay-cro/`. Static symbols show field-to-battle and battle-to-bag/party relationships, but inspected import entries are linker veneers, not scene callers. There is still no positive gameplay RAM sample, so no production runtime/scene mapping is justified. The photo-question stop remains in place.
- **Regression checks:** the focused DS/3DS layout and scene transition tests passed (23 tests, 0 failures). `git diff --check` is clean; only this persistent plan file is modified outside ignored `work/` artifacts.

## Scheduler follow-up (2026-11-24)

- Restoring the isolated Y frame-658 menu state into a fresh userdir preserved the source and clone hashes, but did not resolve input throughput: a single-frame A command stalled for ~36 seconds under `N3DS_STEP_RUNLOOP=1` before `waittight` could run. The agent stopped that isolated session without sending another input. Keep frame 658 as the safe recovery point and do not repeat the same press path; pursue a different headless strategy or use the previously built x86_64/Android route only when explicitly appropriate. No save write occurred.
- The Y title/menu and Ultra Moon CRO investigations remain parallel; no unverified production memory mappings were added.

## Diagnostics plan update (2026-11-24)

- The Y `press A` stall is not proven to be an emulator deadlock: `press`/`hold` call `runFrames()` without the wait-progress flag, so single-step `core.Step()` can block with no telemetry. Next engineering action is opt-in progress logging for key-pulse commands in a dedicated worktree. Do not retry Y until that harness diagnostic is reviewed and built.
- Ultra Moon disassembly now has real code bytes after the import-veneer region in `FieldRo.o`, but there are no local function symbols and simple ARM/Thumb branch scans did not recover callers of the imported battle-start veneers. Continue mapping internal relocations against code, with candidates explicitly marked static/unverified.

## Ultra Moon disassembly checkpoint (2026-11-24)

- `FieldRo` code contains imports for `EventBattleCall::CallWild/CallTrainer`, `GameEventManager::CallEvent`, and `BattleInst::StartBattle`; `Battle` imports party selection and battle-party data methods. The imported code addresses are linker veneers, and no direct ARM branch callsites or local symbols were recovered. `nnroControlObject_` is a generic dispatch table. `Battle` module presence is only a candidate scene signal, not validated; do not add it to `startupSceneOf` until a positive runtime battle sample proves it.
- Exact static offsets and reproduction commands are in ignored `work/usum-static-gameplay-cro/disassembly-followup.txt`. No emulator input or production source changes resulted.

## Headless progress patch verification (2026-11-24)

- Reviewed and integrated the 8-line `N3DS_WAIT_PROGRESS` forwarding change for `press` and `hold` plus its README update. Built the patched `n3ds_dump` target successfully with the existing Linux Azahar build cache; output had only existing Azahar/GCC warnings. Default behavior remains unchanged when the env flag is off.
- With telemetry enabled, `press A` on the isolated Y title-menu savestate returned normally (first RunLoop 0.022 s), kept `DllStartMenu`/German `Weiter`, and wrote no save. `waittight 60` advanced 59 frames in ~42.8 s then stalled before returning; the container was stopped. The exact causes remain undetermined.
- Next diagnostic uses `N3DS_STEP_RUNLOOP=1` and `wait 60` (not `waittight`) to test the slower single-step scheduler with observable progress. Keep the source save hash unchanged and stop if progress stalls.

## Pokémon Y load transition checkpoint (2026-11-24)

- Using the patched progress-enabled single-step scheduler, the isolated frame-658 German title menu accepted one A and `wait 120` returned normally after 70.9 s with modules `DllField`, frame counter 122. The screenshot remains black, so this is likely still a load/transition or a headless-render issue; it is not enough to claim playable field or companion support. No second key, save write, or source-save change occurred; the SHA-256 remains `9B68354B…EE100`.
- Next bounded probe reloads the same saved menu state in a fresh clone, sends one A, then waits 180 frames with progress and no keys to distinguish a short black transition from a persistent render/session issue. Stop on an unreadable personal-choice prompt or stalled progress.

## Pokémon Y first rendered runtime checkpoint (2026-11-24)

- The patched single-step run from the isolated German `Weiter` menu progressed with one A and no later input to frame 242 / modules `DllField DllUSMirror`. The screenshot `work/y-runtime-stepwait-240-final.png` shows the bedroom and German PSS categories (`Freunde`, `Bekannte`, `Passanten`); an emulator savestate is preserved at `work/y-runtime-stepwait-240-final.state` (11,245,302 bytes).
- Source and clone save hashes remain `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`; no in-game save was written. Trainer memory at the existing X/Y root reads German byte 5 and UTF-16 trainer name `IzFunk3` (case in the pixel-font screenshot is ambiguous). The captured party region is allocator/pointer-like rather than a validated packed party; bag bytes and zone values are not semantically validated. No mapping changes are justified yet.
- Next read-only check loads this state and inspects X/Y's field-menu root at `0x08C54044` to see whether the PSS module presents a verified touch/menu marker. Do not change the scene policy from this one sample.
