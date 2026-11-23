# 3DS architecture recovery

This note records the implementation boundary against the existing PokeDaisy DS path. It is a development record, not a claim that 3DS behavior has feature parity with DS.

## Upstream guidance used

- `CLAUDE.md` describes the DS screen pipeline in terms of the game thread, `DsStagePolicy`, `DsTransitionHold`, the captured screen pair, and state-aware touch-screen presentation. It also requires `docs/ROM_FEATURES.md` and the player manual to be updated when game-facing support changes; no player-facing setting or ROM capability was added in this work.
- `docs/DEVELOPMENT.md` documents `EmulatorEngine` as the DS lifecycle owner and `GameSampler`/`RamSnapshot` as the companion capture/decode pipeline. These are the architectural reference points, not a mandate to run Azahar through the DS core.
- `docs/manual/devices.md` defines the player-facing DS contract: selected screen layouts, game-requested touch-screen presentation, and keeping both pictures together through transitions. The current 3DS policy follows the same layout vocabulary, while its scene map is less complete.
- `docs/manual/README.md` requires manual changes to match shipped behavior and use the established concise player-facing style. This internal record therefore does not alter the player manual.
- `docs/ROM_FEATURES.md` records support by specific game and feature, with evidence and limitations. The 3DS reader currently recognizes only verified observations; unknown screens remain explicitly unmapped.

## DS architecture used as the model

1. `app/src/main/kotlin/com/pokedaisy/app/EmulatorEngine.kt` owns the DS core lifecycle and samples from the running game thread, where memory reads can be made at frame boundaries.
2. `app/src/main/kotlin/com/pokedaisy/app/companion/GameSampler.kt` captures one immutable RAM image, decodes it away from the game thread, coalesces bursts, and discards delivery after close. The resulting `SnapshotView` is the shared data contract.
3. `app/src/main/kotlin/com/pokedaisy/app/DsStagePolicy.kt` centralizes the selected layout and conditions under which the second screen is presented. `DsTransitionHold.kt` retains a paired image while a verified fade or dark transition resolves.
4. The Android stage and Companion consume those policy outputs rather than owning game-specific memory logic.

## 3DS implementation boundary

The 3DS code reuses the existing app-facing contracts and keeps Azahar-specific execution behind a small adapter:

- `N3dsSession.kt` owns Azahar's thread, input pump, surface readiness, pause/stop sequence, scene observer, and Companion sampler. It publishes the shared `SnapshotView` and feeds the shared `GbaInput` action source. This replaces the former idle DS engine construction on a 3DS launch; it is not a second instance of `EmulatorEngine`.
- `N3dsNative.kt` is the JNI boundary. `readObservation()` combines loaded CRO names and requested memory ranges at one Azahar frame boundary; the observer falls back to separate legacy reads if the combined JNI symbol is unavailable. This is analogous to DS frame-boundary reads, but uses Azahar's blocking request path and separate native runtime.
- `Gen6Reader.kt` contains title-specific memory configs and scene decoding. X/Y, ORAS and Sun/Moon have configs; Ultra Moon has only a verified startup module observation. Unknown gameplay scenes remain `UNMAPPED` rather than being guessed.
- `DsStagePolicy.kt` is shared for presentation policy. `n3dsStageLayout()` preserves the user's chosen layout; 3DS scenes can request the lower screen in the ONE SCREEN window path without silently changing that choice. `N3dsSceneTransition` / paired captures are 3DS-specific because Azahar exposes two independently rendered surfaces and frame-paired screenshots.
- `companion/AdaptiveTouchWindow.kt` now provides the same 4:3 fit and 92% stage-width / 88% content-height limits to both DS's `DsTouchWindow` and 3DS's floating touch screen. 3DS supplies its actual touch-screen aspect and subtracts the Azahar frame before fitting. Its explicit dual-screen layouts continue through `DsLayouts`; only the floating single-screen window uses this helper.
- The Companion view model, `SnapshotView`, `GameActions` and `StateSlots` are shared. Azahar's run loop, JNI, surfaces and native state handling remain 3DS-specific.

## Retained, changed, and unresolved

Retained: `Gen6Reader`'s verified title configs and guarded pointer chains; `N3dsSession` as Azahar lifecycle adapter; `N3dsNative` as JNI boundary; shared app input, Companion snapshot model, actions, state slots, and stage policy.

Changed toward DS conventions: app actions/state-slot ownership now uses common abstractions; scene-driven presentation is routed through the existing stage policy; Companion delivery is guarded by a per-session generation token so queued callbacks from an ended game cannot replace a newer session; scene and memory observations are batched at a frame boundary where the JNI entry point is available; and both DS / 3DS touch windows share adaptive sizing and aspect-ratio fitting.

Not replaced: the DS `EmulatorEngine` remains the DS core adapter. Forcing Azahar through it would conflate a distinct native lifecycle and its two Vulkan surfaces with the DS core's thread and framebuffer contract.

Remaining deviations: 3DS has a custom Kotlin sampler rather than `GameSampler`'s capture/decode abstraction; scene transition detection is not the DS `DsTransitionHold` implementation; Azahar's two-surface acquisition and lifecycle are independent; and Sun/Moon/Ultra Sun/Ultra Moon gameplay Companion data is incomplete. Ultra Moon's `FieldRo` module is now recognized as `FIELD` from an isolated live headless capture alongside the observed setup-screen modules `LangSelect`, `HeroSelect`, and `StrInput`; its preceding intro narration remains unmapped. The field module does not validate memory addresses or enable Companion data. Pokémon Y uses the X memory layout but a complete play-through and all Companion tabs have not been verified. No speculative memory addresses or scene labels were added here.

## Safety and validation

### Companion sampling reuse: deviation and next step

The concrete remaining deviation is `N3dsSession.sampler` (`app/src/main/kotlin/com/pokedaisy/app/n3ds/N3dsSession.kt`): it owns a `Thread` that wakes every `SAMPLE_MS`, performs `N3dsNative.readMemory(Gen6Reader.ranges(config))`, decodes the Gen 6 data, performs text/wild/map lookups, and posts each `SnapshotView`. By contrast, the upstream `GameSampler` (`app/src/main/kotlin/com/pokedaisy/app/companion/GameSampler.kt`, also present in `upstream/main`) separates synchronous capture (`prepare`) from deferred decode, prevents overlapping reads, coalesces a burst into one follow-up, and cancels queued delivery on `close()`. `docs/DEVELOPMENT.md` calls out this capture/decode/coalescing ownership for the shared game hosts.

This is a real pipeline reuse opportunity, but a direct substitution is not yet safe. The 3DS loop has behavior beyond constructing `SnapshotView`: it tracks and reloads localized `Gen6Text`, updates `OwnItemNames`, caches zone-derived wild/map data, follows the Sun/Moon town-map pointer with an additional Azahar read, throttles stale-read invalidation through `N3dsSnapshotHealth`, and merges battle data under `snapshotLock`. `GameSampler` currently accepts only a deferred `SnapshotView?` producer and invokes `prepare` on the caller; it does not model the N3DS-specific unavailable/error event or async read failure result. Moving the loop wholesale would either put current synchronous decoding back on the Azahar sampler worker or risk races in those stateful caches and an out-of-order `lastView`/battle merge.

`app/src/main/kotlin/com/pokedaisy/app/n3ds/N3dsSamplingPipeline.kt` now provides the isolated primitive: typed success/capture-failure/decode-failure outcomes, separate serialized capture and decode executors, one coalesced follow-up, close invalidation, and a session-generation check. It deliberately does not replace the session's stateful sampler yet. The next integration step is to use it from `N3dsSession` while keeping the Sun/Moon pointer-chained town-map read inside capture, the mutable text/wild/map caches inside serialized decode, `N3dsSnapshotHealth` semantics for capture failures, and `snapshotLock`/battle merge at generation-checked delivery. The focused tests in `app/src/test/kotlin/com/pokedaisy/app/n3ds/N3dsSamplingPipelineTest.kt` cover coalescing, close/generation invalidation, typed capture failure, and decode/delivery ordering. Integration should proceed one responsibility at a time with non-layout tests before any game/device verification.

The pipeline is intentionally not wired into `N3dsSession` in this change; its current stateful loop remains in place until the host-specific caches and delivery ownership can be migrated incrementally. The five focused pipeline unit tests pass. No ROM/save, device, or ADB operation was performed.
