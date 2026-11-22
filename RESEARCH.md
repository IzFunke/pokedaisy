# Research

- PokeDaisy `main` already includes an optional Azahar integration and X/Y single-screen feature set. Avoid duplicating this work; validate and fix concrete gaps.
- Existing source references: `docs/3DS_GAMES_PLAN.md`, `n3ds/build.gradle.kts`, `n3ds/src/main/cpp/CMakeLists.txt`, app N3DS Kotlin sources, and N3DS companion screenshot/unit tests.
- The selected integration uses direct JNI. The repository does not expose Azahar through a reusable Libretro Android frontend.
- The core submodule is pinned to Azahar commit `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2). A newer upstream RC was found during initial research, but no upgrade was attempted; version changes require compatibility testing and are outside the verified changes.
- The latest suitable PokeDaisy beta checked was v1.6.2-beta.1. Its reference APK SHA-256 matched the GitHub release digest.
- The Reddit reference demonstrates planned/intended X single-screen controls and field HUD; it does not establish Y support or internal implementation details. Keep demonstrated and planned behavior distinct.
- Black 2 uses `Poller.readGen5Telemetry()` and common `SnapshotView`/Companion screens; its DS scene transitions come from melonDS screen flags in `EmulatorEngine.watchScreens()`. These are reusable patterns, not reusable DS RAM addresses or core flags.
- Ultra Moon's S25 screenshot and source agree: the game boots, but `Gen6Reader.configFor(0x00040000001B5100)` is null. `N3dsSession.sampler` only publishes snapshots for non-null configs, and the scene loop has the same prerequisite. This is the direct reason the Companion stayed at its generic startup status and why game-driven lower-screen presentation never began.
- USUM must not inherit the Sun/Moon memory addresses without validation. Existing ROMFS/text/map decoders can be reused separately from runtime addresses. The missing evidence is title-specific USUM memory from supported scenes (field, X menu, party/bag, battle and battle submenus), plus an actual S25 check for lower-screen touch input.
- The repository's `native-capture/n3ds_dump` is the intended headless tool for finding CRO roots/modules and capturing process memory. Its CMake target needed an explicit `cxx_std_20` feature because Azahar's CMake sets that variable in a child directory. Docker Desktop is available; the Linux Azahar build is being compiled against the repository-pinned submodule and the user's own ROM will be mounted read-only with a scratch Azahar user directory.
- A first container build was interrupted while a second build wrote the same Ninja directory. Both task-created containers were stopped by ID; the shared build log required regeneration. Do not run simultaneous CMake/Ninja builds against `work/n3ds-dump-linux`.
- The designated ROM directory contained Pokémon Ultra Moon but no Pokémon Y during inventory. Ultra Moon was not opened or changed. Do not acquire game ROMs, firmware, or keys from elsewhere.
# Ultra Moon EU startup capture (2026-11-22)

Built the existing `native-capture/n3ds_dump` utility against the pinned Azahar source with GCC 12.2.0. The first build
found that the diagnostic target had disabled Azahar's PCH even though `video_core/pica_types.h` uses `std::bit_cast`
without directly including `<bit>`. Adding a `<bit>` PCH only to `video_core` fixed the standalone build; the product
source and Azahar submodule were not changed.

Ran the locally owned Ultra Moon Europe `.3ds` read-only from `E:` and gave Azahar a fresh scratch user directory under
`work/`. The cart reports title ID `00040000001b5100`. At frame 600, `findcrs` found the static CRO header at `0x008b2000`,
and `modroot 0x008b2000` reports `LangSelect`. This is direct title/build evidence for the startup module/root, not a
field, battle, party, or save-memory map. It differs from the USA Sun/Moon root currently encoded as `0x00869000`; do not
alias that address to Ultra Moon based only on the shared generation.

The headless title reaches the permanent language chooser. A savestate captured there reloaded successfully, which lets
subsequent UI experiments skip the several-minute cold boot. The one-frame button script is not reliable for this menu;
the language was not committed and the diagnostic run did not pass a personal profile/photo prompt. No real save or ROM
was modified. Screenshots and savestates from this capture are disposable artifacts in `work/`.
