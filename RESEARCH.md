# Research

- PokeDaisy `main` already includes an optional Azahar integration and X/Y single-screen feature set. Avoid duplicating this work; validate and fix concrete gaps.
- Existing source references: `docs/3DS_GAMES_PLAN.md`, `n3ds/build.gradle.kts`, `n3ds/src/main/cpp/CMakeLists.txt`, app N3DS Kotlin sources, and N3DS companion screenshot/unit tests.
- The selected integration uses direct JNI. The repository does not expose Azahar through a reusable Libretro Android frontend.
- The core submodule is pinned to Azahar commit `219d743a1e698101fe1ab8bef43e6563126c3c27` (2126.2). A newer upstream RC was found during initial research, but no upgrade was attempted; version changes require compatibility testing and are outside the verified changes.
- The latest suitable PokeDaisy beta checked was v1.6.2-beta.1. Its reference APK SHA-256 matched the GitHub release digest.
- The Reddit reference demonstrates planned/intended X single-screen controls and field HUD; it does not establish Y support or internal implementation details. Keep demonstrated and planned behavior distinct.
- Black 2 uses `Poller.readGen5Telemetry()` and common `SnapshotView`/Companion screens; its DS scene transitions come from melonDS screen flags in `EmulatorEngine.watchScreens()`. These are reusable patterns, not reusable DS RAM addresses or core flags.
- The user-provided [Windows release](https://github.com/drake-dot-o/pokedaisy-windows/releases/tag/v1.6.2-beta.1-windows) is a useful DS interaction reference. Its [commit `37b8520`](https://github.com/drake-dot-o/pokedaisy-windows/commit/37b8520) describes DS HUD corner anchoring, touch/quick-menu behavior, and zoom handling across the Windows DS core paths. This is DS/Windows code and does not add an Android 3DS backend or a USUM memory map; use it to compare visible behavior, not as drop-in Azahar code.
- Android's `SCREEN > 3DS CORE` currently offers only `AUTO` and `AZAHAR`; `AUTO` resolves to the bundled native Azahar backend. The Android build explicitly disables Azahar's `ENABLE_LIBRETRO`, and the app does not have a general Android Libretro host for 3DS. A second core choice needs a working backend plus display, audio, input, save and companion-memory integration; a UI-only option would be misleading.
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

## Ultra Moon intro memory evidence (2026-11-22)

Independently compared the 16 MiB Gen 7 heap snapshots from the live `AppDemo` intro and Kukui photo/profile view. They differ at 86,455 bytes across the snapshots, but neither contains an ASCII/UTF-16 `AppDemo`, `SequencePlayer`, `HeroSelect`, `Photo`, or `PlayerPhoto` state string. Both have zero at heap base + `0x19AC`; that does not validate Sun/Moon's zone address for Ultra Moon. Asset-name strings such as `HeroSel_Chara_` and `Intro_MIN_` occur in both states and are not state markers. No field, party, or menu address is inferred from this comparison.

A public PKSM-Scripts Gen 7 USUM save-layout note lists save-file offsets for the bag (`0x0000`), Trainer1 (`0x1000`), Trainer Card (`0x1400`), and party (`0x1600`): https://github.com/FlagBrew/PKSM-Scripts/blob/master/dev/docs/gen7/usum.txt. These describe the save archive, not live process RAM, so they are only a possible future save-reader reference and cannot drive live Companion or screen behavior.

## Windows PokeDaisy release reference (2026-11-22)

The user-provided [Windows v1.6.2-beta.1 release](https://github.com/drake-dot-o/pokedaisy-windows/releases/tag/v1.6.2-beta.1-windows) is described as adding Windows support plus companion, HUD, and zoom functionality. Its tagged commit `37b8520a` specifically changes the Windows DS core and Libretro DS HUD/zoom layout, hit testing, and anchoring. That is useful as a DS presentation reference, but it does not provide a 3DS core, Azahar state detection, or a USUM memory map, so it cannot directly supply the missing Android 3DS integration.

## Ultra Moon EU static RomFS validation (2026-11-22)

A read-only probe of the user's own decrypted Ultra Moon Europe cart confirmed that USUM reuses the Sun/Moon Gen 7 archive paths and record formats for moves, personal data, zones, encounters, and icons, while German text table indices differ. USUM indices are species 60, moves 118, items 40, abilities 101, natures 92, types 112, and places 72. Its island-map SARC is at `a/2/3/8` (Sun/Moon uses `a/2/3/4`). `N3dsLayout.USUM` now has these indices, its own output art directory, and the map archive override. The ROM-gated `readsUltraMoonEuRomFsLayoutWhenProvided` test verified decoded German labels, Tackle power, Bulbasaur types, Route 1 zones, wild entries, icons, and map extraction. This is static ROM support; it provides no live RAM mapping or Companion state.

The linked Windows v1.6.2-beta.1 release remains useful as a reference for DS-visible integration (HUD anchoring, zoom, and touch mapping). Its Windows DS implementation is not an Android Azahar core or a source of USUM runtime addresses.

## Azahar Libretro availability and integration boundary (2026-11-22)

The user's Windows build remains a useful visual/interaction reference for DS, especially zoom, HUD anchoring, and touch mapping; it does not provide an Android 3DS backend.

New source verification: the pinned Azahar `2126.2` submodule includes `src/citra_libretro` and its Android build path. The [official Azahar site](https://www.azahar-emu.org/) and [official releases](https://github.com/azahar-emu/azahar/releases) list Android Libretro builds, including ARM64. PokeDaisy currently builds Azahar's native frontend libraries and explicitly forces `ENABLE_LIBRETRO=OFF`; the app has no general Libretro host. Azahar's CMake treats Libretro as a mutually exclusive frontend configuration, disabling incompatible frontends/audio/services and wiring core callbacks differently. Therefore the released `.so` cannot simply be added beside `libpk_n3ds.so`: PokeDaisy would still need a Libretro host for environment negotiation, graphics context/video, audio, input, storage, lifecycle, and Azahar options. The existing native Azahar bridge already uses the same emulator engine, so shipping a second frontend for the same engine is not automatically equivalent to adding a genuinely different DS core backend.

A real 3DS core selector remains in Settings (`AUTO`, `AZAHAR`), but there is only one in-app backend. Keep the selector honest; do not expose Libretro as selectable until that host and all required bridges are implemented and validated. Investigate the smallest host integration spike against the pinned Android ARM64 core, preserving the existing native path, before changing user-facing core choices.

## Measured Ultra Moon setup modules (2026-11-22)

The x86_64 Azahar session at the Kukui photo/profile setup prompt reported `AppDemo`, `SequencePlayer`, and `Object`; the same sequence appeared during the intro capture. The startup scene classifier now recognizes the full group as `SCREEN`, which feeds the existing `n3dsStageLayout` DS-style paired-display behavior. A unit test checks the module group in either order and asserts partial/unknown groups remain `UNMAPPED`. This extends only the observed setup sequence; the later field, menu, battle, and party modules remain unmapped, and the current prompt was left untouched.

## AppDemo signal rejected after cross-scene check (2026-11-22)

The `AppDemo`, `SequencePlayer`, `Object` trio appears during ordinary narration as well as the photo/profile setup. It cannot distinguish a touch-dependent screen from a top-screen dialogue. A temporary automatic paired-screen mapping was removed, and regression coverage now requires this group to remain `UNMAPPED`. The 16 MiB heap diff remains too broad for a memory address claim. Continue with a state marker observed across multiple stable samples or a verified game-owned structure.

## Ultra Moon lower-screen capture path (2026-11-22)

The pinned Azahar Vulkan renderer can service `RendererBase::RequestScreenshot` using a temporary `SingleFrameLayout` that enables only the lower screen, then copies the rendered framebuffer to host memory. This path is independent of the secondary Android surface. A read-only 320x240 capture from the current live German narration state was fully black while the top display continued to show Kukui's narration. This is evidence for that one scene, not a general scene classifier: no title input or personal setup choice was made. The existing scene policy should remain unchanged until a lower-screen sample and module/state marker are correlated at a safe gameplay checkpoint.

## 3DS core-selection runtime wiring (2026-11-22)

The persisted `N3dsCore` choice previously had no launch-time consumer: the activity always constructed `N3dsSession` directly. The launch path now resolves `AUTO` or explicit `AZAHAR` against the bundled backend list and dispatches through the resolved backend. The focused `N3dsCoreTest` covers auto resolution, explicit selection, and missing-backend behavior. Azahar remains the sole bundled 3DS backend; no alternative core is claimed.
