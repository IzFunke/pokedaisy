# Architecture

PokeDaisy has an optional Android 3DS module at `n3ds/`, enabled with `-Ppokedaisy.n3ds=true`. The current implementation embeds the pinned Azahar core behind JNI and packages ARM64 native code. The debug build proves the native target compiles and packages; it does not prove a game boots.

The app already includes Pokémon X/Y support, game detection and a single-screen companion UI. Source areas include `app/src/main/kotlin/com/pokedaisy/app/n3ds/` (session, cartridge and generation-six readers), `n3ds/src/main/cpp/` (native integration), and the N3DS companion UI and tests. Existing `docs/3DS_GAMES_PLAN.md` is the detailed upstream design reference.

Azahar Libretro is not the selected Android route: the current app has no generic Libretro Android frontend, and the Azahar core expects renderer context integration. JNI is appropriate for the existing native core and companion memory-read path.

The app targets ARM64 for Azahar. Use physical ARM64 hardware for core runtime checks; the available x86_64 translation AVD produced SIGILL and is not suitable evidence. The app keeps the 3DS feature optional so standard builds do not require the added native core.
