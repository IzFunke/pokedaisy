# 3DS architecture recovery

This note records the implementation boundary against the existing PokeDaisy DS path. It is a development record, not a claim that 3DS behavior has feature parity with DS.

## Upstream guidance used

- `CLAUDE.md` describes the DS screen pipeline in terms of the game thread, `DsStagePolicy`, `DsTransitionHold`, the captured screen pair, and state-aware touch-screen presentation. It also requires `docs/ROM_FEATURES.md` and the player manual to be updated when game-facing support changes; no player-facing setting or ROM capability was added in this work.
- `docs/DEVELOPMENT.md` documents `EmulatorEngine` as the DS lifecycle owner and `GameSampler`/`RamSnapshot` as the companion capture/decode pipeline. These are the architectural reference points, not a mandate to run Azahar through the DS core.
- `docs/manual/devices.md` defines the player-facing DS contract: selected screen layouts, game-requested touch-screen presentation, and keeping both pictures together through transitions. The current 3DS policy follows the same layout vocabulary, while its scene map is less complete.
- `docs/manual/README.md` requires manual changes to match shipped behavior and use the established concise player-facing style. The 3DS layout behavior is documented in `docs/manual/devices.md`.
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
- `DsStagePolicy.kt` is shared for presentation policy. A 3DS `SCREEN` scene uses the same temporary `SIDE_BY_SIDE` layout transition as a DS page when the chosen layout floats; leaving the page restores the player's stored choice. Field menus and battle bag/party continue to request the lower screen through the floating path. `N3dsSceneTransition` / paired captures are 3DS-specific because Azahar exposes two independently rendered surfaces and frame-paired screenshots.
- `companion/AdaptiveTouchWindow.kt` now provides the same 4:3 fit and 92% stage-width / 88% content-height limits to both DS's `DsTouchWindow` and 3DS's floating touch screen. 3DS supplies its actual touch-screen aspect and subtracts the Azahar frame before fitting. Its explicit dual-screen layouts continue through `DsLayouts`; only the floating single-screen window uses this helper.
- The Companion view model, `SnapshotView`, `GameActions` and `StateSlots` are shared. Azahar's run loop, JNI, surfaces and native state handling remain 3DS-specific.

## Retained, changed, and unresolved

Retained: `Gen6Reader`'s verified title configs and guarded pointer chains; `N3dsSession` as Azahar lifecycle adapter; `N3dsNative` as JNI boundary; shared app input, Companion snapshot model, actions, state slots, and stage policy.

Changed toward DS conventions: app actions/state-slot ownership now uses common abstractions; scene-driven presentation is routed through the existing stage policy; Companion delivery is guarded by a per-session generation token so queued callbacks from an ended game cannot replace a newer session; scene and memory observations are batched at a frame boundary where the JNI entry point is available; and both DS / 3DS touch windows share adaptive sizing and aspect-ratio fitting.

Not replaced: the DS `EmulatorEngine` remains the DS core adapter. Forcing Azahar through it would conflate a distinct native lifecycle and its two Vulkan surfaces with the DS core's thread and framebuffer contract.

Remaining deviations: 3DS uses the shared, typed `N3dsSamplingPipeline` rather than routing Azahar through `GameSampler`'s coroutine host API; scene transition detection is not the DS `DsTransitionHold` implementation; Azahar's two-surface acquisition and lifecycle are independent; and Sun/Moon/Ultra Sun/Ultra Moon gameplay Companion data is incomplete. Ultra Moon's `FieldRo` module is recognized as `FIELD` from an isolated live headless capture alongside setup modules `LangSelect`, `HeroSelect`, and `StrInput`; its preceding intro narration remains unmapped. Because this is a module-only signal and the lower renderer capture is black, the scene drives field HUD/zoom but does not automatically open the lower screen. Pokémon Y uses the X memory layout but a complete play-through and all Companion tabs have not been verified. No speculative memory addresses or scene labels were added here.

## Safety and validation

### Companion sampling reuse: implementation and remaining boundary

`N3dsSession` now uses `N3dsSamplingPipeline` for Companion snapshots. A dedicated capture executor performs the Azahar memory read, captures Sun/Moon's pointer-chained map position with that read, and records the frame counter. A separate serialized decoder builds the `Gen6Reader.Sample`, loads language-specific text, updates wild/map caches, and constructs `SnapshotView`. Generation checks read the snapshot gate's live token, so stop or terminal publication invalidates queued work immediately. Stop closes the pipeline before asking Azahar to stop; stale battle-clear and end callbacks are gated as well. Delivery preserves `N3dsSnapshotHealth`, `snapshotLock`, `lastView`, and battle merging. Capture/decode causes are logged once per sustained failure streak instead of being discarded.

This follows `GameSampler`'s capture/decode/coalescing/close pattern but does not call `GameSampler` directly. Its current API captures synchronously on the caller and returns a `SnapshotView?` closure; it cannot express typed capture/decode failures, and dispatching Azahar capture through its coroutine host would change the current native-memory read ownership. `N3dsSamplingPipeline` is a small host-neutral primitive that isolates those needs; this API difference remains an architecture deviation to revisit if the shared sampler is generalized.

Focused root tests passed: 15 `DsStagePolicyTest`, 15 `Gen6Test` discovered (9 ROM-dependent skips), and 6 `N3dsSamplingPipelineTest`; 0 failures/errors. The field-policy regression asserts that a module-only field scene does not auto-present the lower screen without a mapped field reader. No ROM/save data was modified by these source changes; Ultra Moon observations remain in the isolated user directory.
