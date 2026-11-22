# Nintendo DS / 3DS integration parity

This comparison uses PokeDaisy's existing DS behavior (`docs/manual/devices.md`, `EmulatorEngine`, `DsStagePolicy`, and the shared Companion) and the user's Windows DS release as the interaction reference. It distinguishes the shipped Android behavior from the remaining Azahar work.

| Area | DS / Black 2 contract | Android 3DS implementation | Status |
| --- | --- | --- | --- |
| Scene source | The melonDS integration exposes game scene/touch flags to `EmulatorEngine.watchScreens()` every frame. Black/White/Black 2/White 2 menus and battles select a paired or floating touch presentation by game state. | `N3dsSession` reads Azahar CRO modules and title-specific `Gen6Reader.Config` data every 100 ms, then applies shared stage policy. | Partial: state input exists for configured X/Y, ORAS and Sun/Moon. Ultra Moon has no gameplay map. |
| ONE SCREEN | The main game fills the display; the touch screen floats in for the game's mapped interaction pages. Menus/parts that use both screens become side by side, and unknown actions retain manual TOUCH SCREEN controls. | `n3dsTouchWanted` supports mapped field menus and battle bag/party. A mapped `SCREEN` can request side by side. The lower Azahar surface remains available for manual control. | Partial: Ultra Moon only maps its observed language selector. Field, menu and battle remain `UNMAPPED`. |
| Transitions | DS reads core fade/dark-screen flags each frame and holds the previous composition until the next page is ready. | 3DS scene recognition debounces module reads but does not have a matching per-frame Azahar fade/transition hold. | Gap: still needs an Azahar per-frame state signal and transition policy. |
| Touch input | The game gets touch coordinates in its native touch-screen space; the active layout and floating window do not change their meaning. | `N3dsNative.touch` sends coordinates through Azahar's active window/layout mapping. | Implemented in the bridge; physical S25 interaction still needs verification on the next Dev build. |
| Companion | Shared Companion UI consumes the DS core's game-specific RAM reader; Windows DS refreshes visible telemetry every 12 frames. Black 2's party/battle state is available through its Gen 5 reader. | Shared snapshot/UI consumes `Gen6Reader.Config`; mapped 3DS snapshots now refresh every 200 ms. | Partial: the reader currently maps X/Y, ORAS and Sun/Moon. Ultra Sun/Moon have no verified config, so Companion data correctly reports unavailable. |
| Field HUD / zoom | The shared DS HUD follows verified field state and yields during menus/battles. | 3DS has a separate Gen 6 HUD/camera adapter for configured titles. Azahar camera zoom is not the same mechanism as DS EXPAND. | Partial and title-dependent; Ultra Moon live fields are unavailable until its config is verified. |
| 3DS core choice | DS exposes the app's available DS cores and their real capabilities. | Settings resolve `AUTO` / `AZAHAR` to the one bundled native Azahar backend. | Honest single-backend wiring; the Windows release contains no 3DS core or second Azahar backend. |

## S25 comparison status

The source/manual comparison is documented above. The feature-enabled Dev build is now installed on the Galaxy S25 Ultra, and `LibraryActivity` opened successfully with the existing setup/last-game keys present. A same-build Black 2 versus Ultra Moon interaction comparison remains pending; this update did not launch a ROM or send game input.

## Reference source

- [DS screen and one-screen behavior](manual/devices.md#ds-games)
- [Windows v1.6.2-beta.1 release](https://github.com/drake-dot-o/pokedaisy-windows/releases/tag/v1.6.2-beta.1-windows)
- [Windows DS core source](https://github.com/drake-dot-o/pokedaisy-windows/blob/v1.6.2-beta.1-windows/windows/ds-core/pd_ds_core.cpp)
- Android DS scene policy: `app/src/main/kotlin/com/pokedaisy/app/EmulatorEngine.kt`, `DsTransitionHold.kt`, and `DsStagePolicy.kt`
- Android 3DS scene/Companion path: `app/src/main/kotlin/com/pokedaisy/app/n3ds/N3dsSession.kt` and `Gen6Reader.kt`
