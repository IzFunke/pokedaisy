# Architecture

PokeDaisy has an optional Android 3DS module at `n3ds/`, enabled with `-Ppokedaisy.n3ds=true`. The current implementation embeds the pinned Azahar core behind JNI and packages ARM64 native code. The debug build proves the native target compiles and packages; it does not prove a game boots.

The app already includes Pokémon X/Y support, game detection and a single-screen companion UI. Source areas include `app/src/main/kotlin/com/pokedaisy/app/n3ds/` (session, cartridge and generation-six readers), `n3ds/src/main/cpp/` (native integration), and the N3DS companion UI and tests. Existing `docs/3DS_GAMES_PLAN.md` is the detailed upstream design reference.

Azahar Libretro is not the selected Android route: the current app has no generic Libretro Android frontend, and the Azahar core expects renderer context integration. JNI is appropriate for the existing native core and companion memory-read path.

The app targets ARM64 for Azahar. Use physical ARM64 hardware for core runtime checks; the available x86_64 translation AVD produced SIGILL and is not suitable evidence. The app keeps the 3DS feature optional so standard builds do not require the added native core.

`N3dsNativeInitTest` is a dev-build instrumentation smoke check. It verifies the optional JNI library loads and initializes the Android callback/storage setup on an ARM64 device without needing game content. It does not boot `Core::System` with a title; game-loader and renderer validation remain separate requirements.

## Runtime integration compared with the DS path

Black 2 is not a separate companion UI: `Poller` identifies its ROM and reads Gen 5 telemetry into the shared `SnapshotView`, while the DS core exports per-frame scene/screen flags that `EmulatorEngine.watchScreens()` turns into lower-screen visibility. The 3DS path reuses the common snapshot and Companion UI, but `N3dsSession` needs title-specific memory configuration to sample party/game state and to classify its live modules for screen transitions.

For Pokémon Ultra Moon, that config is intentionally absent until verified. `N3dsSession` therefore cannot currently publish a data snapshot or detect its scene; the user-selected layout and manual TOUCH SCREEN action remain available. A clear in-Companion unmapped-data status now replaces the indefinite generic startup indicator. The map must be validated against Ultra Moon runtime memory before automatic screen presentation and party/battle data are enabled.
# Ultra Moon EU runtime evidence

The pinned Azahar headless harness reports Ultra Moon Europe (`00040000001b5100`) at `0x008b2000` with `LangSelect`
loaded at the initial permanent language chooser. The existing Sun/Moon USA static root (`0x00869000`) is not a verified
replacement for this title. Keep USUM's `Gen6Reader.configFor` unset until field/battle/party data is measured; the
generic Companion status should explain that limitation instead of holding at an indefinite loading state.
