# Research

- PokeDaisy `main` already includes an optional Azahar integration and X/Y single-screen feature set. Avoid duplicating this work; validate and fix concrete gaps.
- Existing source references: `docs/3DS_GAMES_PLAN.md`, `n3ds/build.gradle.kts`, `n3ds/src/main/cpp/CMakeLists.txt`, app N3DS Kotlin sources, and N3DS companion screenshot/unit tests.
- The selected integration uses direct JNI. The repository does not expose Azahar through a reusable Libretro Android frontend.
- The core submodule is pinned to Azahar commit `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2). A newer upstream RC was found during initial research, but no upgrade was attempted; version changes require compatibility testing and are outside the verified changes.
- The latest suitable PokeDaisy beta checked was v1.6.2-beta.1. Its reference APK SHA-256 matched the GitHub release digest.
- The Reddit reference demonstrates planned/intended X single-screen controls and field HUD; it does not establish Y support or internal implementation details. Keep demonstrated and planned behavior distinct.
- The designated ROM directory contained Pokémon Ultra Moon but no Pokémon Y during inventory. Ultra Moon was not opened or changed. Do not acquire game ROMs, firmware, or keys from elsewhere.
