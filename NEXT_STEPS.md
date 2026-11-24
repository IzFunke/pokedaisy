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

## Pokémon Y PSS auto-presentation fix (2026-11-24)

- The frame-242 Y savestate shows the rendered PSS `Freunde / Bekannte / Passanten` screen, with runtime modules `DllField DllUSMirror`. A read-only state inspection confirmed the X field-menu marker at `0x08C54084` is 0, so the existing pointer predicate cannot recognize this PSS page.
- Added `DllUSMirror` to Gen 6 `FIELD_TOUCH`; `sceneOf()` now classifies this observed page as `MENU`, which reuses the existing automatic lower-screen presentation and dismissal policy. `Gen6Test.sceneFromModulesAndTheFieldMenu` covers the exact `DllField\nDllUSMirror` chain with zero menu marker and asserts touch wanted/dismissible. Focused `Gen6Test`: 16 tests, 0 failures, 9 ROM-dependent skips.
- Built Dev with `--project-prop=pokedaisy.n3ds=true`; ARM64 native Azahar and the app packaged successfully. Updated the installed `com.pokedaisy.app.dev` using `adb install -r`. `firstInstallTime` stayed `2026-11-23 18:29:14`, `setup_done=true` remained set, and `lastUpdateTime` advanced to `2026-11-24 06:35:39`.
- The S25 is currently at its lock screen. I did not enter credentials or attempt to bypass it, so Y/Ultra Moon were not launched there. Continue headless; resume device gameplay validation after normal unlock.

## Coordination update (2026-11-24 06:53 CET)

- The active goal attachment already contains exactly one complete `## 8. Ergänzung: Effizientere Multi-Agent-Koordination`; it was read and verified, so no duplicate edit was made.
- **Pokémon Y:** on a fresh isolated clone from the rendered frame-242 checkpoint, one B left the PSS page unchanged. One X plus 60 no-key frames opened the field menu; one A opened `DllBag`; 60 further no-key frames settled on the German `Items` page showing `Nichts`. All five configured pocket starts read zero, matching that empty view. Source/clone hashes remain `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`; no in-game save was written. This is empty-bag and trainer-label evidence only; it does not validate populated party/items. The Y agent is now progressing a fresh isolated clone through the intro, choosing Chespin only if a starter is required, and may save only that disposable clone as authorized by the user.
- **PSS auto-presentation:** the source fix is `8daaaed7`; its focused Gen6 test asserts `DllField\nDllUSMirror` with zero ordinary field-menu marker requests the lower screen. User-facing manual and `ROM_FEATURES.md` now describe only the observed EU Y PSS page in commit `31199933`. Headless captures do not prove the PokeDaisy UI on hardware.
- **Headless tooling:** commit `0dac58f9` adds `scripts/run_n3ds_dump_bounded.ps1`, a Windows external process-tree timeout with tagged output and a temp-child smoke test. All three smoke checks pass. It cannot safely interrupt Azahar inside `RunLoop`; callers must use a disposable userdir/save copy.
- **Ultra Moon save recognition:** source trace found library, Game Info, Save Editor, session staging and sync all use the canonical `<ROM basename>.sav` resolver. An isolated title archive exists at 445,440 bytes with expected metadata, but its inner save checksums and the running app's selected user-data root are not established. No code defect or production change was justified. Preserve the photo-choice stop at `Welches dieser Fotos stammt gleich noch mal von dir?`.
- **Validation/status:** named `Gen6Test` Gradle task completed as `UP-TO-DATE`; prior focused run passed 16 tests (9 ROM-dependent skips). Manual checks passed 9/9 and built 147 pages. `git diff --check` and bounded-supervisor smoke tests are clean. `feature/azahar-3ds-single-screen` is pushed through `0dac58f9`; private `work/` stays untracked.
- **Next:** review the isolated Y starter run for repeated PK6 structure/checksum and Companion evidence, then implement only validated mappings with focused tests. Revisit device testing on the S25 only when unlocked normally; install future app builds using `adb install -r`. Do not clear app data or pass the Ultra Moon photo prompt.

## Save-path validation update (2026-11-24 06:59 CET)

- Re-ran only `N3dsSavesTest` and `SavesLocationTest` with `--rerun-tasks`: 17 tests, 0 failures/errors/skips. Gradle rebuilt dependencies in 3m40s; the filtered test classes passed. This supports the existing canonical `.sav` resolver and does not establish which Azahar data root the installed app currently uses.
- No app source changed in this validation pass, so no new APK was installed. The existing Dev install and its data remain untouched.

## Pokémon Y standalone bag integration (2026-11-24 07:05 CET)

- A stable isolated EU Y capture showed `DllBag` alone while the German lower-screen `Items` page displayed `Nichts`; the five configured pocket starts were zero. B and a two-frame tap at `(300, 228)` did not leave the bag. Do not repeat that return probe. The top field remained separate from this touch-only page.
- **Code:** `Gen6Reader.FIELD_TOUCH` includes `DllBag`; `sceneOf()` now recognizes touch-only modules even when `DllField` is absent, after the battle check. `Gen6Test` asserts the standalone bag requests and can dismiss the auto-touch presentation and that battle classification stays first. Committed/pushed as `16e31413`.
- **Validation:** `Gen6Test` (16 discovered, 0 failed, 9 ROM-dependent skipped) and `N3dsSceneTransitionTest` (6, 0 failed) passed. Named `N3dsSavesTest` / `SavesLocationTest` also passed 17/17. Manual tests passed 9/9 and the site built 147 pages. The Azahar-enabled Dev APK built successfully and was installed on the S25 with `adb install -r`; `firstInstallTime` stayed `2026-11-23 18:29:14`, `setup_done=true`, `lastUpdateTime=2026-11-24 07:03:42`. The phone reports `Dozing`; the app was not launched and no lock was bypassed.
- **Ultra Moon save check:** all app routes use the canonical `<ROM basename>.sav` resolver. The isolated `main` and metadata exist, but the app's configured Azahar root and the archive's internal save validity are still unverified. No source change was justified.
- **Next:** continue Y from a fresh isolated clone toward a populated starter party, using only one observed action at a time and saving only the disposable copy if reached. Verify PK6 checksum and companion reads before changing mappings. Preserve Ultra Moon's exact pre-photo stop. No user save or app data was altered; no upstream PR was opened.

## Pokémon Y clean-profile route (2026-11-24 07:29 CET)

- The existing frame-242 bedroom profile has no visible `Neues Spiel` entry and cannot leave `DllBag` through B or the tested bottom-screen buttons; those probes stopped without writing. Do not repeat them.
- A brand-new Azahar userdir, with no old save staged, reached `DllLangSelect`. German was visibly selected and confirmed per the user's earlier choice. The isolated game created a new 415,232-byte `main` (`1467BB75689DF04B17D7B19BAAC11AE387C9B99E81B4D8B46B74A0D6EF2EB9C0`); no in-game Save command was used. A bounded wait reached the visible Y title animation at frame 530. All pre-existing source/clone/backup saves stayed at `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`.
- Y agent is restarting only this new profile, with a verified backup, to reach a stable title/menu and continue the user-authorized intro. Do not infer a saved game or party from this new `main`; verify actual gameplay and PK6 checksum after the starter.
- **Next:** finish the clean-profile intro if progress remains observable; use the in-game save only in this disposable userdir. Preserve Ultra Moon's photo-choice stop. The S25 Dev install is current via `adb install -r`, but the device is Dozing and the app was not launched.

## Touch and clean-profile follow-up (2026-11-24 07:34 CET)

- Read-only Azahar trace confirms the bag red-X tap at bottom-local `(260,228)` maps to framebuffer `(300,468)`, inside the icon, and `engine:emu_window` feeds HID touch state. It did not close `DllBag`; no more identical bag-exit probes are justified.
- The fresh empty Y profile reached German language confirmation and the visible Y title flare at frame530. After stopping and restarting with its isolated `main` (415,232 bytes, hash `1467BB75689DF04B17D7B19BAAC11AE387C9B99E81B4D8B46B74A0D6EF2EB9C0`), a single A and bounded wait returned a black frame at 122; module output was missing, so this is not a validated gameplay transition. No in-game save command was invoked. All pre-existing save hashes remained unchanged.
- **Next:** read the captured launch transcript to check that the German-profile `main` was staged into Azahar's expected title directory and learn why module output was missing. Do not repeat the same black wait or bag-exit inputs. Continue only from a materially different title/runtime checkpoint. Preserve the Ultra Moon photo stop and the current S25 app data.

## Cold-boot transcript result (2026-11-24 07:36 CET)

- The log confirms the isolated German-profile `main` was mounted at Azahar's correct all-zero SDMC profile/title path and matches its backup (`1467BB75689DF04B17D7B19BAAC11AE387C9B99E81B4D8B46B74A0D6EF2EB9C0`). Existing saves remain unchanged.
- On restart, A was sent before any screen was visible; at frame122 the capture was black and `moduleNames()` had no CRO chain. Normal loader messages appeared, no crash. This is not evidence of a missing save; the previous visible Y flare was in a warm German-confirmed process.
- **Next:** one new cold boot with no input and a single bounded 240-frame wait, then inspect the stable screenshot/modules. Do not send keys before a visible action is established.

## Weiterarbeit nach Goal-Ergänzung (2026-11-24)

- **Goal-Datei:** `C:\Users\beben\.codex\attachments\f7f19961-5891-4d88-b4be-358dd5c90b86\goal-objective.md` wurde zuvor geprüft. Der Abschnitt `## 8. Ergänzung: Effizientere Multi-Agent-Koordination` ist genau einmal vollständig vorhanden; bestehender Text blieb unverändert.
- **Pokémon Y:** Im frischen isolierten deutschen Profil wurde der sichtbare Professor-Dialog einmal mit A fortgesetzt. Bei Frame 2868 blieb `DllIntro` aktiv; Platan steht weiter entfernt neben einem Pokéball, aber es gibt derzeit keinen sichtbaren Text, Prompt oder Auswahl. Keine weitere Taste und kein Spielstand-Schreibvorgang. Nächster Schritt: begrenztes Warten ohne Eingabe, bis ein neuer sichtbarer Dialog/Prompt erscheint; keine Eingabe bei Übergängen oder nicht sichtbaren Entscheidungen.
- **Ultra-Mond:** erneute Prüfung fand keinen positiven Laufzeitbeleg für Kampf/Team/Tasche. Der Frame-437-Sample zeigt `FieldRo FieldMenu FieldEffectCommon`; Kandidaten-Team bei `0x33F7FA44` ist leer und Tasche bei `0x33011934` null. Die eingefrorene Fotoauswahl hat keinen synchronisierten RAM-/Modullisten-Sample. Keine Produktionszuordnung geändert. Nicht über `Welches dieser Fotos stammt gleich noch mal von dir?` hinausgehen. Günstigster nächster sicherer Schritt wäre ein synchroner read-only Dump von Screenshot, CRO-Liste und Kandidatenbereichen am unveränderten Prompt; das belegt nur den Promptzustand, keine Gameplay-Szene.
- **Parallel laufend:** eine gezielte gemeinsame DS/3DS-Touchfenster-/Overlay-Paritätsprüfung mit möglichem eng begrenztem Fix und fokussierter Regression. Änderungen ausschließlich in isoliertem Worktree; anschließend prüfen und integrieren.
- **Schutz:** Alt-Saves und S25-App-Daten unangetastet; keine Hardware-Eingabe. Keine ROMs, Saves oder Schlüssel ins Git; kein Upstream-PR.

## Touch-Overlay-Regression und Y-Intro (2026-11-24)

- **UI-Parität:** Review bestätigte, dass alleinstehende `DllBag`-/`DllPss*`-Module schon als untere Bildschirmseite (`SCREEN`) klassifiziert und automatisch angezeigt werden. Es wurde keine Produktionslücke gefunden. Neue explizite Regressionen dafür kamen in Commit `001aeb76` hinzu; der fokussierte Gradle-Aufruf blieb ohne Ausgabe hängen und wurde abgebrochen. Die neuen Tests sind daher **noch nicht verifiziert**. Im Root-Worktree mit den bestehenden Y-/Ultra-Mond-Assertions zusammengeführt.
- **Pokémon Y:** Isolierter deutscher Intro-Lauf zeigt bei Frame 2988 Platan mit einem kleinen grünen Pokémon und den Anfang des nächsten Satzes „Wie du vielleicht weißt, wird dies…“. Der vorherige Wait hat keine Frames weitergeschaltet; keine Eingabe wurde gesendet. Nächster Schritt: im nachweislich funktionierenden progress-enabled Single-Step-Wait-Modus begrenzt und ohne Tasten warten, danach vollständigen Text/Marker prüfen. Kein A bis zur sichtbaren vollständigen Dialogzeile.
- **Ultra-Mond:** kein laufender Emulator und kein bestätigter null-input-Runner für das isolierte Foto-Checkpoint-Statefile. Deshalb wurde kein State geladen und keine Eingabe gesendet. Für einen sicheren Diagnose-Dump ist erst ein verifizierter Runner nötig, der `loadstate`, `modules`, `frames`, read-only RAM-Ausgabe und Screenshot ohne `wait`/Eingabe unterstützt.
- **Repo:** Integration ist lokal im bestehenden Branch; `NEXT_STEPS.md` ist aktualisiert. Private `work/`-Artefakte bleiben untracked. Kein Push/PR in diesem Schritt.

## Touch-Overlay-Test verifiziert (2026-11-24)

- Die neue Regression machte sichtbar, dass standalone `DllBag` nachweislich als `MENU` statt `SCREEN` klassifiziert wird (damit untere Touch-Seite, passend zum beobachteten Y-Bag-Verhalten). Der Erwartungswert des Tests wurde korrigiert; Produktionslogik blieb unverändert.
- Fokussierter Dev-Lauf bestanden: `Gen6Test.standaloneTouchOnlyPagesRequestTheSecondScreen` und `DsStagePolicyTest.standaloneTouchPagesStayVisibleAcrossEveryChosenLayout`; `BUILD SUCCESSFUL`, beide Tests abgeschlossen.
- Der lokale Regressionstest-Commit wurde aktualisiert. Keine APK-Änderung oder Geräteinstallation nötig.

## Pokémon Y Intro-Fortsetzung (2026-11-24, Frame 3170)

- Ein einzelnes A war bereits vor dem begrenzten Wait gesendet worden; der anschließende Frame-3170-Screenshot zeigt eine gerahmte Kalos-Pokémon-Illustration (u. a. Chespin/Fletchling), `DllIntro`, ohne Text oder Weiter-Marker. Seitdem keine Eingabe. Nächster Schritt: bounded no-input wait120, dann erneut inspizieren; kein Tastendruck ohne sichtbaren vollständigen Dialog/Prompt.
- Keine dauerhafte Y-Spielstandänderung: weiter frisches isoliertes Profil. Bestehende Quellspielstände und Ultra-Mond-Fotoauswahl unverändert.

## Goal-Wiederaufnahme und parallele Fortsetzung (2026-11-24)

- Goal erneut vollständig gelesen. Die Anforderungen 1–8 bleiben aktiv; insbesondere geht es weiter um vollständige Y/Ultra-Mond-Spielzustandserkennung, Companion und reale Gerätevalidierung, nicht nur um Tests/Audits.
- **Pushed:** Regressionstest-Commit `ea8bc583` liegt jetzt auf `origin/feature/azahar-3ds-single-screen`. Enthält nur zwei Testdateien; fokussierte Dev-Tests sind bestanden. Kein PR.
- **Pokémon Y:** Frame 3350: `DllIntro`, stabiler vollständiger deutscher Erklärungstext über Pokémon, kein sichtbarer Marker trotz vollständigem 400×480 Capture. Der User hat Intro-Fortsetzung ausdrücklich autorisiert; nächster Schritt ist genau ein A für normalen Fließtext und danach bounded wait/Inspektion, mit Stopp vor personalisierter Profilwahl.
- **S25:** `adb devices -l` zeigt die Wireless-Verbindung. Read-only Status: Gerät `Dozing`; Dev-App noch VersionCode 23 / `1.6.2-beta.1`, `firstInstallTime` weiter `2026-11-23 18:29:14`, `lastUpdateTime` `2026-11-24 07:03:42`. Keine Eingabe/Installation in diesem Test-only-Schritt.
- **Agenten:** Pokémon Y isolierter Headless-Lauf aktiv; Architektur-Agent untersucht eine konkrete gemeinsame Companion-/Screen-Policy-Verbesserung in separatem Worktree; Upstream-Agent bearbeitet präzise veraltete README-Aussage im eigenen Worktree. Alle parallel geschriebenen Änderungen werden vor Integration geprüft.
- **Ultra-Mond:** bleibt gleichrangig. Foto-Checkpoint bleibt unverändert; aktueller Host hat keinen Emulatorprozess. `n3ds_dump.cpp` belegt inzwischen eine sichere Befehlsfolge ohne emulierte Frames (`loadstate`, `modroot`, `modules`, `frames`, `hex`, `shot`), aber lokaler Build und ROM-Datei sind in diesem Checkout nicht vorhanden. Nächster Schritt ist zu klären, ob das vorhandene Headless-Artefakt/ROM aus der bereits genutzten Umgebung erreichbar ist, ohne externe Quelldaten zu verschieben oder zu ändern.

## Layout-Geometrie und Upstream-README (2026-11-24)

- **Shared layout fix:** `n3dsStageArrangement()` now rounds floating touchscreen box dimensions and center coordinates using the same nearest-pixel convention as shared `DsLayouts`. New odd-stage-size regression in `DsStagePolicyTest`. Focused Dev class passed: 19 tests, 0 failures. Commit `47f568ab` is pushed to the existing feature branch.
- **Upstream documentation:** README now states that opt-in Android Azahar can launch decrypted supported 3DS carts, while Companion mapping coverage is separately qualified (X/ORAS/Sun/Moon mapped; Y still being verified; USUM gameplay mapping unverified). Commit `5922ec15` is included on the feature branch. From `website/`, `node --test test/docs.test.js test/docsearch.test.js` passed 9/9.
- **Y:** current isolated intro continues with ordinary German narration; at frame 3594 it showed “Manche von ihnen leben auf dem Land, andere in der Luft und wieder andere im Wasser…”. A bounded no-input wait is in progress before further ordinary-text input. Still no save write to user data.
- **Current upstream gates:** no PR. License obligations remain unresolved: Azahar mixed GPL terms, FAAD2 GPL-only compatibility for the combined APK, generated GLAD license grant, and complete distribution-license-page review. README accurately avoids calling the implementation fully supported or PR-ready.
- **Working tree:** only `NEXT_STEPS.md` and ignored/private `work/` artifacts are changed/untracked; implementation commits are pushed.

## Dev-Build-Validierung auf S25 (2026-11-24)

- Azahar-fähiger Dev-Build erfolgreich: `./gradlew --project-prop=pokedaisy.n3ds=true :app:assembleDev -Pquick --no-daemon --console=plain`.
- Per autorisiertem `adb install -r` aktualisiert. Paket bleibt VersionCode 23 / 1.6.2-beta.1; `firstInstallTime` unverändert (`2026-11-23 18:29:14`), `lastUpdateTime` aktualisiert auf `2026-11-24 09:12:24`, `setup_done=true`. S25 blieb `Dozing`; App nicht gestartet, kein Touch/Unlock, keine Spielstände oder Settings verändert.
- **Ultra Moon headless:** statische Quellprüfung beweist, dass `loadstate`, `modroot`, `modules`, `frames`, `hex`, `shot` im `n3ds_dump`-CLI keinen Frame und keine Eingabe ausführen; `shot` schreibt ausschließlich ein PNG. Der Linux-Binary und das eingefrorene Foto-Gate-Statefile existieren. Der erforderliche entschlüsselte ROM-Pfad fehlt in repo-lokalen Aufzeichnungen, daher kein Lauf und kein Guess. Vorbereitetes read-only Kommando bleibt: loadstate; `modroot 0x008B2000`; modules/frames; `hex` für `0x33012818`, `0x33F7FA44`, `0x33011934`; shot; frames. Kein Wait/Input/Savestate.
- **Pokémon Y:** Dialog über die Lebensräume blieb nach einem weiteren 60-Frame-Wait unverändert, vollständig und ohne Marker. Wegen expliziter Intro-Freigabe wird mit genau einem A und bounded wait weitergespielt, innerhalb des isolierten Profils.

## APK-Hinweisstatus und Runtime-Check (2026-11-24)

- cpp-httplib-Lizenztext ist durch `:app:licensesAsset` mit `--project-prop=pokedaisy.n3ds=true` erfolgreich in `app/build/generated/licenses/licenses.txt` enthalten; APK-Archiveintrag `assets/licenses.txt` geprüft (NOTICE + vollständiger MIT-Text, Copyright 2017 yhirose). Restliche Lizenzfragen bleiben offen.
- Neu paketierter Azahar-Dev-APK mit Lizenzattribution wurde erneut per `adb install -r` installiert. `firstInstallTime=2026-11-23 18:29:14` unverändert; `setup_done=true`; `lastUpdateTime=2026-11-24 09:15:02`. Danach die App einmal normal in der Bibliothek gestartet: Activity startete in 882 ms, keine `FATAL EXCEPTION` im letzten Logfenster. Die App wurde nicht zur ROM geöffnet; keine Touch-Eingabe und keine Save-Pfade/Spielstände geändert.
- Das Gerät zeigte nach Install `Awake`, mit PokeDaisy Library als oberster Activity. Weitere Eingabe pausiert, solange die Nutzung unklar ist. Direkte Geräte-Gameplaytests bleiben ausstehend; Headless Y läuft in isoliertem Profil.

## Y-Namenseingabe und weitere Diagnosepfade (2026-11-24)

- **Pokémon Y:** gender selector default (männlicher linker Portraittyp) und danach mittlerer standardmäßiger Calem-Look in der Wegwerfprofil-Session gewählt. Name-Feld `DllIntro DllStrInput` bei Frame ~4673 ist geöffnet. `touch` an sichtbaren Entf./E-Koordinaten und LEFT bewegen den roten Fokus nicht verlässlich; das Feld zeigt `N N|` (nicht eindeutig bereinigbar). Keine Name-Bestätigung und kein weiterer Tastendruck. Ein rein read-only Diagnosepass prüft jetzt Tastatur-Layout-/HID-Zuordnung; weiter keine Eingabe, bis ein zuverlässiger Weg belegt ist.
- **Android Y-Absturz:** aktueller Dev-APK startet auf dem S25 bis zur Bibliothek (LibraryActivity, kalt, 882 ms); kein `FATAL EXCEPTION` im letzten Logfenster. Ein separater Diagnose-Worktree wertet vorhandene Crash-Evidenz aus, ohne ROM/Appdaten auf dem Gerät anzurühren.
- **License:** cpp-httplib MIT notice wurde in generiertes Lizenzasset und APK verifiziert und gepusht. Nächster read-only Audit befasst sich ausschließlich mit offizieller GLAD-Lizenz/Generierungsprovenienz.
- **Ultra Moon:** Null-Frame read-only capture commands sind geprüft, aber Ausführung wartet auf den zuvor benutzten ROM-Pfad; kein globaler PC-Scan und kein checkpoint load.

## Headless Touch-Diagnoseinstrumentierung (2026-11-24)

- `n3ds_dump` hat jetzt optionales `N3DS_TOUCH_TRACE=1` (bottom screen rect, lokale Koordinate, tatsächlich gemappte Framebufferkoordinate, `TouchPressed` accepted/rejected) und frame-neutrales `touchmap X Y`. Die bestehende Touch-Dauer und Release-Reihenfolge bleiben gleich. Änderung `447f3eab` ist gepusht.
- **Validierung:** `git diff --check` sauber; fokussierter statischer Check bestätigte, dass `touchmap` weder `RunLoop`, `runFrames`, noch Touch-/Key-Injection aufruft und dass Trace am echten `TouchPressed`-Ergebnis hängt. C++-Compile ist in der Windows-Umgebung nicht verfügbar: kein Host-C++-Compiler, Azahar im dedizierten Worktree nicht initialisiert; daher nicht als kompiliert behaupten.
- **Pokémon Y:** Name-Eingabe bleibt bei Frame4666 in `DllIntro DllStrInput`; geschätzte Touch-/Dpad-Eingaben bewegen Fokus nicht konsistent. Keine Bestätigung, keine weitere Eingabe. Der Harness muss erst mit der neuen Instrumentierung gebaut und mit `touchmap` kalibriert werden.
- **S25:** letzter read-only Vordergrundstatus war YouTube. Der aktuelle APK mit Khronos-Notice wurde lokal gebaut, seine Lizenzliste im APK verifiziert; Update auf dem Gerät wartet, bis es nicht störend ist.

## Ultra-Mond Foto-Checkpoint: frame-neutraler Snapshot (2026-11-24)

- Den ROM-Pfad im bereits laufenden `pokedaisy-n3ds-builder`-Container gefunden (`Pokemon Ultra Moon (Europe) ... .3ds`, Quelle nur read-only gemountet). Den bestehenden `run6-photo-question.state` per SHA-256 vor/nach geprüft: identisch.
- `n3ds_dump` lief in frischem throwaway Userdir mit ROM/Repo/state read-only gemountet. Nur Befehle `loadstate`, `modroot`, `modules`, `frames`, `hex`, `shot`, `frames`; kein `wait`, `RunLoop`, Input, `savestate` oder Original-Userdir. Loadstate meldete `ok`; Framezähler vor/nach bei 0. Auf dem Foto-Gate waren Module `AppDemo SequencePlayer Object`.
- Read-only RAM-Ergebnis: Trainer-Kandidat `0x33012818` enthält die bereits bekannte German-Language/Name-Struktur; `0x33F7FA44` und Alternativkandidat `0x330128E4` enthalten pointer-/allocator-artige Daten, keine validierte Party; `0x33011934` ist hier null. Keine Produktionsadresse/Scene-Mapping ergänzt. Der gerenderte `shot` ist schwarz, weil ohne emulierten Frame der savestate den software-renderer-Ausgabepuffer nicht wiederherstellt; bereits vorhandene Foto-Gate-Screenshots bleiben die visuelle Prompt-Evidenz.
- Capture-Artefakte und neue throwaway Userdir liegen unter `work/usum-scene-investigation/photo-gate-readonly-b79010d3*`; alles bleibt lokal/untracked und wird nicht gelöscht oder committet.

## Snapshot-Ergebnis und bestehende Headless-Prozesse (2026-11-24)

- Der Safe-Load des Ultra-Mond-Checkpoints wurde jetzt wirklich ausgeführt. `loadstate ... ok`, `modules=AppDemo SequencePlayer Object`, `frames=0` vor/nach den RAM-Lesezugriffen. State-Hash blieb unverändert. Sichtbarer `shot` ist schwarz, weil ohne weiteren Frame kein Renderer-Buffer aus dem Savestate wiederhergestellt wird; bekannte Screenshot-Checks 1–4 bleiben die visuelle Foto-Prompt-Evidenz. Live-RAM an Kandidatenparty `0x33F7FA44`/`0x330128E4` ist allocator-/pointer-artig, Taschencandidate `0x33011934` bleibt null. Keine Gameplay-Zuordnung belastbar.
- Ultra-Mond-Rohdump liegt in `work/usum-scene-investigation/photo-gate-readonly-b79010d3/readonly-sample.txt`; gerenderter Screenshot 116 Byte/schwarz; throwaway Userdir daneben. Keines der Originale geändert.
- **Headless toolchain:** Docker builder image/toolchain und ein altes Linux build cache existieren. `docker ps/top` zeigt jedoch fünf langlebige Y-Emulatorprozesse, die alle dieselbe `work/n3ds-dump-linux/n3ds_dump`-Datei verwenden (einer CPU-busy). Daher wurde das Touch-Trace-Commit noch nicht kompiliert und die gemeinsame ausführbare Datei nicht ersetzt. Nächster sicherer Build muss eine getrennte CMake build directory verwenden oder erfolgen, nachdem der Y-Prozesszustand gesichert und der gemeinsame Build koordiniert wurde.
- **Y name entry:** letzte untersuchte UI bleibt bei `DllIntro DllStrInput` um Frame4673 mit uneindeutigem `N N|`. Keine weitere Eingabe oder Bestätigung. Erst zuverlässige `touchmap`-/TouchTrace-Ausgabe nutzen.

## Arbeitsplan nach Ergänzung (2026-11-24 10:05 CET)

- Goal-Datei geprüft: Abschnitt 8 ist genau einmal vorhanden; es war keine Dateimutation nötig. Der bisherige Goal-Text wurde nicht verändert.
- Abgeschlossene Agentenarbeit in getrennten Worktrees: `y_mapping_audit` (keine belegte Party-Adresse vor Starterwahl), `usum_mapping` (bestehende Saves sind nullgefüllt; kein verifizierter Team-/Taschen-Mapping), `layout_parity` (kein UI-Fix erforderlich; fokussierte Tests bestanden). Keine Agenten aktiv.
- S25: Dev APK per `adb install -r` aktualisiert; `firstInstallTime` blieb 2026-11-23 18:29:14, `setup_done=true`, App-Speicher-Einstellungen bleiben erhalten. LibraryActivity sichtbar; kein Crash/ANR im geprüften Logfenster. Keine ROM gestartet.
- Root: Architektur-Evidence geprüft; `touchmap` in separatem Build-Verzeichnis kompiliert und frame-neutral mit Y-ROM geprüft. Die fünf bestehenden Y-Prozesse/Binaries blieben unberührt.
- Blocker: Pokémon Y Eingabe bleibt ohne verlässlich kalibrierten Tastaturpfad; Ultra-Mond hat keine verifizierte Team-/Taschen-/Szenenadresse. GLAD-generierte Loader-Lizenz bleibt ungeklärt.
- Nächste Schritte: erst mit aktuellem isoliertem Y-Namenscreen-Checkpoint die Touch-/Tastaturzuordnung prüfen; parallel eine sichere USUM-Signierlösung recherchieren, ohne private Schlüssel ins Repo aufzunehmen. Danach Plan/Branchstatus erneut festhalten. Keine Upstream-PR.

## Agentenbefunde: Parität, Übersetzungen und Saves (2026-11-24)

- **UI/Overlay:** `syncDsStage()` leitet 3DS an `n3dsStageLayout()` weiter; dieses delegiert Arrangement/Fitting an `DsLayouts` und `fitTouchWindow`. Shared layout settings, Auto-Window, dim-close und transition overlay sind abgedeckt. Es gab keinen belegten Fix. Im detached Worktree erfolgreich: `:app:testDevUnitTest --tests com.pokedaisy.app.DsStagePolicyTest --tests com.pokedaisy.app.n3ds.Gen6Test -x :app:licensesAsset --no-daemon` (4 Min, SUCCESS).
- **Übersetzungen:** 3DS-Core-/Game-Saves-Einstellungen sind in allen unterstützten App-Sprachen in `TrSettings.kt` vorhanden; Save-/Fehlertexte in `TrLibrary.kt`. Vollständigkeits- und Suchregressionen sind bereits in `TranslationsTest.kt` / `N3dsSettingRowsTest.kt` vorhanden. Kein neuer Lauf, keine Änderung.
- **Save-Pfade/Editor:** `N3dsSaves.sharedSaveName()` erzeugt ROM-Basename plus `.sav`; `SavesLocation.saveFor()` nutzt den 3DS-Override bzw. Standard-Save-Ordner; Bibliothek/GameInfo/SaveEditor lösen denselben Pfad auf. Fokus-Tests (`N3dsSavesTest`, `Gen6XySaveEditorTest`, `Gen7UsUmSaveTest`, `SaveFileEditsTest`) im separaten Worktree erfolgreich, BUILD SUCCESSFUL. Keine realen Saves verwendet.
- **Offen:** Pokémon Y ist schreibbar in `Gen6XySaveEditor`; Ultra-Mond bleibt bewusst read-only. Eine frische MemeCrypto-Signatur braucht RSA-Private-Key-Material; `CLAUDE.md` und `docs/3DS-SAVE-EDITOR-BRIDGE.md` untersagen das Einbetten/Committen. Der Parser prüft derzeit nur Struktur/CRCs, nicht die Signatur. Keine erlaubte integrierte Signierlösung gefunden; nicht schreiben oder unsigniert akzeptieren.
- **Nächster Schritt:** den kompilierten Harness auf einen sicheren Namenscreen-Checkpoint anwenden; keine Eingabe, bis die konkrete Tastaturaktion sichtbar und zuverlässig zugeordnet ist. Danach Plan/Branchstatus speichern, ohne ROM/Saves/Keys zu committen.

## Touch-Trace-Build und frame-neutrale Koordinatenprüfung (2026-11-24)

- Neuer vollständiger `n3ds_dump`-Build in eigenem Verzeichnis `work/n3ds-dump-touchtrace-build`, getrennt vom aktiven `work/n3ds-dump-linux`-Binary: Ninja 1453/1453 erfolgreich. GCC 12 meldete zwei `-Wstringop-overread`-Warnungen aus Azahar (`artic_cache.h` und `ticket.cpp`); kein Fehler im neuen Harness.
- Frame-neutrale Probe mit read-only Pokémon-Y-ROM und neuem Throwaway-Userdir `work/touchtrace-map-scratch`: `touchmap 160 120` meldete `bottom_screen=(40,240)-(360,480) framebuffer=(200,360)`; danach `frames 0`. Keine Touch-/Key-Eingabe, kein Frame und kein bestehender Save/State geladen.
- Der Probe-Harness bestätigt die allgemeine 3DS-Koordinatenabbildung. Die konkrete Y-Namenseingabe ist noch nicht verifiziert, weil kein aktueller, sicherer Name-Screen-Checkpoint vorliegt.
- Der neue Build und Throwaway-Userdir bleiben private Artefakte unter `work/`; nicht committen oder löschen.

## Pokémon-Y-Namenscreen: Savestate und tatsächlicher Touch-Status (2026-11-24)

- Den laufenden isolierten Y-Prozess per `savestate` bei `frames=4673`, Modulen `DllIntro DllStrInput` gesichert: `work/y-runtime-name-current.state`, SHA-256 `E8A4C8D9DA155A63A5E5152CC5CE1BA9664B86E1A707C17711A30DCA1163CC6D`. Der Originalprozess blieb mit seinem ursprünglichen Binary aktiv.
- Eine separate Kopie des Wegwerf-Userdirs lädt den Checkpoint; Original-`main` und Kopie hatten vor der Probe denselben SHA-256 `1467BB75689DF04B17D7B19BAAC11AE387C9B99E81B4D8B46B74A0D6EF2EB9C0`. Sämtliche Eingaben liefen ausschließlich auf der Kopie.
- `n3ds_dump.cpp`/README erweitern `N3DS_TOUCH_TRACE=1` jetzt zusätzlich um den Status des registrierten `emu_window`-Touch-Geräts. Der isolierte Trace meldet für den sichtbaren Backspace-Treffer local `(264,110)` -> framebuffer `(304,350)` den normalisierten Touch `(0.8250,0.4583)`, `pressed=yes` nach Press und Frames, `pressed=no` nach Release. Der HID-Input-Gerätepfad erhält also den erwarteten Touchzustand.
- Trotzdem änderten zwei kurze Backspace-Taps, ein 10-Frame-Tap, der B-Tastentreffer `(144,181)`, A, D-pad RIGHT und circle-pad LEFT den sichtbaren `N N|`-Text nicht. Keine Taste wurde bestätigt. Der S25 wurde nicht zum Spielstart benutzt. Keine weiteren gleichartigen Tastaturproben, bis eine neue Methode konkrete Evidenz liefert.
- Der Zusatzharness wurde inkrementell im getrennten Buildverzeichnis erfolgreich gebaut (2/2); nur bereits bekannte GCC/Azahar-Warnungen. Der Benutzername `Beben` ist weiterhin nicht eingegeben, und ein In-Game-Speicherstand ist noch nicht erstellt.
- **Nächster Schritt:** die tatsächlichen HID-Shared-Memory-Touchwerte bzw. die Y-Tastatursteuerung im Namenbildschirm untersuchen. Den gespeicherten Checkpoint und die isolierte Userdir-Kopie wiederverwenden; nicht vom Anfang beginnen und den Original-Userdir nicht verändern.

## Touch-Gerätestatus-Diagnose (2026-11-24)

- `N3DS_TOUCH_TRACE=1` zeigt nach dem neuen Harness-Commit auch den registrierten `emu_window`-Touchgeräte-Status. Bei einer isolierten B-Tastenposition bestätigte der Lauf nach Checkpoint-Laden: `accepted=yes`, normalisiert `(0.4500,0.7542) pressed=yes` direkt nach Press und nach 2 Frames, nach Release `(0.0000,0.0000) pressed=no`. Die Touchdauer und Release-Logik blieben unverändert.
- Der Zusatz wurde zweimal inkrementell erfolgreich kompiliert (2/2). Ein Runtime-Probe-Lauf mit der finalen Bedingung für `N3DS_TOUCH_TRACE` gab die erwarteten Statuswerte aus. Standardläufe erzeugen kein zusätzliches Touchgerät.
- Ergebnis: Pixelabbildung und registriertes Touch-Input-Gerät liefern den gewünschten Wert; dennoch reagiert die Y-Namenseingabe nicht sichtbar. Die nächste Diagnose muss HID-Shared-Memory bzw. die spielinterne Eingabeverarbeitung prüfen, nicht weitere gleiche Touchpunkte ausprobieren.

## HID-Ring Vergleich und Wiederverwendung des Pokémon-Y-Saves (2026-11-24)

- `work/y-runtime-name-current.state` wurde in zwei getrennten frischen Emulatorläufen geladen. Der Baseline-Lauf ohne Eingabe blieb bei Frame 0 und `DllIntro DllStrInput`; der A-Lauf führte ausschließlich `press A` im isolierten `y-runtime-name-userdir-copy` aus und endete bei Frame 2 mit denselben Modulen. Der HID-Pad-Ring unterschied sich: Baseline enthält kein gesetztes A-Bit; nach A-Eingabe enthalten Verlaufseinträge `current_state=1` (A ist Bit 0). Damit wird die A-Eingabe vom HID-Sampler in emulierten HID-Speicher übernommen. Das beweist noch nicht, dass der Pokémon-Y-Namensscreen diese Taste als Texteingabe auswertet.
- Touch-HID-Probe bestätigte zuvor denselben Übergang für den unteren Bildschirm: `(144,181,valid=1)` während Touch und `valid=0` nach Freigabe. Zusammen grenzen die Messungen den offenen Fehler auf die Spiel-/Szenenverarbeitung ein. Keine weiteren identischen Tastaturproben; die statische CRO-Importanalyse fand keine Symbole und kann den konkreten Handler nicht benennen.
- Für den Intro-/Gameplay-Fortschritt existiert bereits ein echtes, im Spiel erzeugtes Pokémon-Y-Save: `work/y-runtime-companion-capture/main-after-ingame-save` (SHA-256 `D61CB2433681B9B8BEE51FAC7F93850DF73AF1E6FA0010375136152061D94706`) sowie ein späterer Save nach Kleiderauswahl `main-after-clothes-save` (SHA-256 `9B68354BD2527AF3D2EBC852E085D0366C732A8ED21FCFF5606B24FA4A9EE100`). Spätere isolierte Szenen-Checkpoints liegen bei `y-neighbor-go` und `y-west-down-step6.state`. Diese Artefakte bleiben ausschließlich lokal und unverändert; weitere Arbeit kann daran anschließen, statt den Spielstand neu aufzubauen. Der aktuelle Name-Screen-State bleibt ebenfalls gesichert und unverändert.
- Die laufende Emulatorinstanz `y-runtime-newgame-continue` blieb aktiv; die Vergleichsläufe verwendeten nur den kopierten Throwaway-Userdir und read-only ROM-Mount. Bestehende Saves und App-Daten wurden nicht angerührt.
- Nächste Schritte: Ergebnisse der gezielten statischen Handler-Disassemblierung prüfen; Pokémon-Y-Gameplay vom vorhandenen Save/Checkpoint fortsetzen; Android-S25-Crashursache anhand vorhandener Evidenz parallel klären. Ultra-Mond-Checkpoint und bestehende Save-Dateien unangetastet lassen; keine privaten Schlüssel, ROMs oder Saves committen.

## S25-Startdiagnose und Save-Schutz (2026-11-24)

- S25 ist per Wireless ADB verbunden und war auf dem Launcher, nicht in aktiver Benutzung. Installierter Dev-Stand bleibt `1.6.2-beta.1`, `firstInstallTime=2026-11-23 18:29:14`, `lastUpdateTime=2026-11-24 10:01:31`; in dieser Prüfung wurde nichts installiert oder deinstalliert. Vorhandene `exit-info`/Log-Auswertung fand keinen Pokémon-Y-Fatal- oder JNI-Fehler; die jüngsten Dev-Exits waren Update/Task-Removal.
- Dev-SharedPreferences und Pokémon-Y-`.sav` wurden vor dem isolierten Startversuch gesichert. Das S25-Y-Save unter `/storage/emulated/0/Emulation/PokeDaisy/saves/Pokemon Y (Europe) (En,Ja,Fr,De,Es,It,Ko).sav` ist 415232 Bytes, SHA-256 `82C8E89B2E7E11A296DB847031CF2B53F225A819C66F17C2B827B55582133616`; Vorher-/Nachher-Hash identisch. Temporär auf einen neuen Test-Savepfad gerichtete Einstellungen wurden bytegenau wiederhergestellt (Preferences SHA-256 vorher/nachher `c61f8e1950ea77ee9d00baa9f19033a8a7ecba75190a279cd47f3a14fabd4d90`), Setup-Status und ursprünglicher Save-Pfad bleiben erhalten.
- Start der LibraryActivity wurde getestet, Pokémon Y selbst aber nicht gestartet: beim Öffnen der Bibliothek legte `N3dsSaves.saveFile()` bereits aus dem bestehenden Azahar-Userdir eine Ultra-Mond-Kopie am temporären Pfad an. Der Testpfad wurde entfernt, nachdem die Kopie bytegleich mit dem Original war (SHA-256 `90a3a00e0a4d9bcc05164bb54af380a996943f5ccda4800a7e7d41db35e1d725`); Original-Saves blieben stehen. Das Gerät wechselte außerdem in Dozing, und `LibraryActivity` hatte 0 gerenderte Frames; daher keine Touch-Eingabe und kein belastbarer Game-Start/Crash-Befund.
- Daraus folgt für den nächsten S25-Lauf: Azahar-Userdata und beide `.sav`-Dateien müssen gemeinsam isoliert werden, bevor eine ROM gestartet wird, weil die Library Saves aus Azahar automatisch in den gewählten Shared-Pfad migriert. Bis eine solche vollständige Kopie verfügbar ist, läuft die Gameplay-/Crashanalyse headless mit den vorhandenen isolierten Pokémon-Y-Checkpoints. Ultra-Mond bleibt gleichrangig; ROMs/Saves/Keys bleiben uncommitted.

## Parallele Companion- und RAM-Arbeit (2026-11-24)

- Der Namensbildschirm ist aus dem kritischen Pfad genommen. Eine neue statische Analyse des `DllStrInput.cro`-Touchhandlers fand bei Codeoffset `0x62f8` Eingabelesen, Koordinatenlesen und Hit-Test (`0x630c`/`0x6328`/`0x6340`); der Handler setzt anschließend einen Keyboard-Index. In diesem Handler wird A nicht gelesen. Das passt zum reproduzierbaren Emulatorvergleich: Checkpoint-Baseline ohne A-Bit, danach `press A` mit A-Bit im HID-Verlauf; Touch erscheint im 3DS-HID-Ring als Pixel `(144,181,valid=1)` und wird beim Loslassen ungültig. Keine weiteren gleichartigen Eingaben, Name-Screen später erneut aufnehmen.
- **Pokémon Y / `/root/y_companion_runtime`:** validierte spätere Gameplay-States aus `work/y-runtime-companion-capture`; zur Laufzeit Party-, Battle-, Inventar- und Szenenwerte verifizieren. Ausschließlich kopierte Profile/States.
- **Ultra-Mond / `/root/um_companion_runtime`:** ältere Field-Checkpoints wie `work/usum-scene-investigation/um-field-after-touch-test.state`/`ultra-moon-field-live.state` für Companion-Mapping nutzen; den späteren Kukui-Fotoprompt nicht überschreiten. Ausschließlich kopierte Profile/States.
- **Integration / `/root/android_crash_audit`:** Call-Chain `GameInfo`/Library → `N3dsSaves.saveFile()`/`hasSave()` untersuchen; die Bibliotheksabfrage kann fehlende ROM-benannte `.sav` aus Azahar-`main` importieren. Prüfen, wann dieser Kopiervorgang fachlich erwartet wird, welche Quelle/Hashes gesichert werden und ob Lookup von Migration getrennt werden muss. Änderungen nur im eigenen Worktree.
- **S25:** PokeDaisy Dev-Settings byte-identisch restauriert; Y-Save 415232 Byte mit unverändertem SHA-256. Das Öffnen der Library erzeugte beim temporären Pfad eine bytegleiche Ultra-Mond-Kopie aus dem bisherigen Azahar-Userdir; diese Testkopie wurde entfernt, Original blieb. Y-ROM wurde auf dem S25 nicht gestartet. Der App-Start lieferte 0 gerenderte Frames, Gerät ging in Dozing.
- Root koordiniert die Befunde und integriert nur Änderungen, die durch Laufzeitwerte oder Save-Hash-Evidenz gedeckt sind. Getrennte Worktrees für schreibende Agenten; `work/` bleibt private/untracked; keine Upstream-PR.

### Pokémon-Y-Laufzeitbefund

- Read-only auf frischem Profilklon mit `y-neighbor-go.state` (Checkpoint- und Original-`main`-Hashes unverändert): `DllField DllUSMirror`, Zone-Kandidat `0x08C6709E=2`, Sprachbyte `0x08C79C59=5` (Deutsch). Party bei `0x08CE1CE8`, Stride `0x1E4`, 6 verschlüsselte PK6-Slots; aus diesem Zustand 0 gültige Pokémon. Bag-Bereiche belegt: 0 Einträge. Kein Battle-Modul/-Datensatz aktiv. Screenshot zeigt den Haus-/PSS-Freundeslistenabschnitt. `y-after-dark-dialogue.state` bestätigt separat dieselbe Zone und leere Party/Tasche.
- Dieser spätere, aber noch frühe Story-State reicht nicht für Companion-Party/Battle. `/root/y_companion_runtime` sucht jetzt vorhandene spätere States; wenn keiner mit Starter existiert, wird auf einer Wegwerfkopie gezielt bis zur Starterwahl/ersten Encounter weitergespielt und nur dort gespeichert. Vollständiges Intro bleibt unnötig; Name-Screen wird separat vertagt.
- Aktive Agentenstränge: `/root/um_companion_runtime` analysiert `PhotoView`/`TrainerPhoto` statisch, um eine zulässige Default-Auswahl am Kukui-Fotoprompt zu bestimmen; Root übernimmt den nächsten Headless-Schritt über Docker, weil WSL in der Agent-Shell fehlt. `/root/y_companion_runtime` setzt den isolierten S25-Save-Run ohne Savestate fort und liest Party/Battle/Bag direkt. `/root/android_crash_audit` ist abgeschlossen; sein Save-Lookup-Fix wurde integriert. HID-Namensscreen bleibt aus dem kritischen Pfad.

### Save-Lookup-Fix integriert

- Der Side Effect war: `LibraryActivity.factsFor()` fragte `SavesLocation.hasSave()` ab; beim 3DS delegierte dies an `N3dsSaves.hasSave()` → `saveFile()`, das Azahar-`main`/Titel-ID-Dateien in die gemeinsame `.sav`-Datei kopieren konnte. Auch Game Info rief `saveFile()` bei der reinen Anzeige auf.
- In `868dc39c` ist Save-Erkennung jetzt read-only: sie findet kanonische `.sav`, alte Title-ID-Archive oder Azahar-`main` ohne Dateien zu kopieren bzw. den Default-Ordner anzulegen. Game Info zeigt den tatsächlich vorhandenen Quellpfad und bei Legacy-Dateien zusätzlich den kanonischen Zielpfad. Die Migration bleibt im echten Startpfad `N3dsSaves.prepare()`.
- Regressionstest `N3dsSavesTest` prüft, dass `hasSave` ein bestehendes Azahar-`main` erkennt, aber weder Shared-`.sav` noch Shared-Ordner erzeugt und die Quelle bytegleich lässt. Fokussierter Lauf: 9 Tests bestanden, 0 Fehler; im Agenten-Worktree vor Integration, identische Änderung wurde anschließend sauber auf den Feature-Branch übernommen. Kein S25-Install/ROM-Start im Fix.

### Ultra-Mond-Laufzeitbefund

- Isolierte frame-neutrale Läufe von `um-field-after-touch-test.state` und `ultra-moon-field-live.state`: Module `FieldRo FieldMenu FieldEffectCommon`. Trainer-Layout bei `0x33012818`: TID `0xC6B1`, SID `0x3380`, Gender `0`, Sprache `5`/Deutsch, Name `ELL` bei `+0x38` (`0x33012850`); passt zum Gen-7-`MyStatus`-Layout. Money-Kandidat `0x33015210=5000`.
- Party-Kandidat `0x33F7FA44`, Stride `0x1E4`: sechs checksumgültige, aber leere PK7-Slots; Alternative `0x330128E4` ebenfalls leer. Bag-Kandidat `0x33011934` nullgefüllt; keine Battle-Daten und kein Battle-Modul. Alle 31 lokalen `main`-Kopien haben unformatierten Footer, 39 CRC-Fehler und Party-Zähler 0; dies sind keine validen Fortsetzungs-Saves.
- Ultra-Mond-Datenumfang ist deshalb noch nicht genug für Companion. Ein isolierter Progress-State `work/usum-scene-investigation/um-photo-progress-20261124/after-center-touch.state` blieb bei `AppDemo SequencePlayer Object` und Kukuis Fotofrage; der Center-Touch war ungültig. Die zugehörige temporäre PNG wurde entfernt, der State blieb unverändert. Keine Wiederholung derselben Touch-/A-Proben; ein neuer Methodenwechsel ist erforderlich.
- Agent-Shell kann den Linux-Headless-Binary nicht starten, da WSL ohne `/bin/bash` ist. Root kann den vorhandenen `pokedaisy-n3ds-builder`-Docker nutzen, sobald die Foto-Handleranalyse eine konkrete, nicht wiederholte Aktion liefert. Der spätere Fotoprompt ist weiterhin durch die spätere Intro-Freigabe gedeckt; danach nur bis Starter/erster Encounter, um Party-/Battle-/Bag-Daten zu erlangen.

### Pokémon-Y-S25-Save-Checkpoint

- Der echte S25-Y-`.sav`-Export wurde nur auf einer frischen isolierten Userdir-Kopie benutzt. Startmenü `DllStartMenu` bei Frame 299; Continue erreicht `DllField`/Zone 2 bei Frame 440. Original und Kopie blieben vor/nach hashgleich.
- Ein `savestate`-Versuch genau im Feld stürzte Azahar mit Boost `archive_exception: unregistered class`/SIGSEGV ab; danach wurden keine RAM-Kommandos ausgeführt und keine Partywerte behauptet. Der Agent wiederholt den Continue-Pfad ohne Savestate/PNG und liest direkt bei `DllField`.
- Retry ohne Savestate/Screenshots erreichte auf neuer Klonkopie `DllField`, Frame 497, Zone 2. Sprache `0x08C79C59=5` (Deutsch), Party `0x08CE1CE8`, sechs verschlüsselte Gen6-Slots mit 0 checksumgültigen Mon; Bag-Bereiche `0x08C67154` + Taschenoffsets leer; kein Battle-Modul. Der S25-Save ist ein echter weiterverwendbarer Startpunkt, aber noch vor Starter/Party. Original-Save und Klon waren nach RAM-Lesen hashgleich (`82C8E89B…33616`). RAM-Ausgabe: `work/y-runtime-s25-save-audit-retry-20261124/runtime-ram.txt`.
- Nächster Schritt auf dieser Kopie: gezielt bis Starterwahl/Party und optional zum ersten Encounter, dann nur im Klon speichern und Companion-RAM nochmals prüfen. Keine Screenshots oder Savestates im aktuellen Retry.

### Dev-Build auf S25

- Azahar-fähige Dev-App aus dem Feature-Branch erfolgreich mit Gradle 8.10.2 gebaut (`:app:assembleDev`, BUILD SUCCESSFUL in 23 s) und mit `adb install -r` aktualisiert. `firstInstallTime` blieb `2026-11-23 18:29:14`; `lastUpdateTime=2026-11-24 11:16:36`; `setup_done=true`, 3DS/DS Save-Ordner bleiben `/storage/emulated/0/Emulation/PokeDaisy/saves`.
- LibraryActivity startete nach Install ohne Fatal-/JNI-/ANR-Log. S25-Y- und UM-Originalsave-Hashes vor/nach: Y `82c8e89b…82133616` (415232 Bytes), UM `90a3a00e…35e1d725` (445440 Bytes), unverändert. Es wurde keine ROM auf dem Gerät gestartet; Gerät blieb im Dozing-/Launcherzustand ohne Touch-Eingaben.

### Screenshot-Aufräumstatus (2026-11-24)

- Der Nutzer bat, alle von uns erzeugten PC-/S25-Screenshots zu löschen. Auf dem S25 wurden in dieser Session keine Dateien gespeichert; alle zwei Test-Captures wurden per `adb exec-out` direkt auf den PC gestreamt.
- PC-Inventur fand 1089 Screenshot-Kandidaten in privaten `work/`-Captures. Bilder aus dem eingebetteten `PKMN-NTR`-Quellrepo, ROM-Ressourcen unter `rom-cro`, Handbuch-/App-Assets und alle getrackten Projektdateien wurden ausgeschlossen.
- Das Löschen wurde von der automatischen Sicherheitsprüfung mit `blocked by policy` abgewiesen (einschließlich der zwei eindeutigen Test-Captures). Es wurde nichts gelöscht. Nicht über andere APIs/Tools umgehen; bei erneutem Wunsch nur nach Freigabe durch die Dateisicherheitsprüfung fortsetzen. Agenten sollen keine persistenten Screenshots mehr erzeugen und notwendige temporäre Bilder direkt danach entfernen.
