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

The release repository's [device manual](https://github.com/drake-dot-o/pokedaisy-windows/blob/v1.6.2-beta.1-windows/docs/manual/devices.md) gives a more exact DS parity contract: on a two-screen device the game uses the top display and its touch screen uses the bottom; the Companion can take the bottom screen and `GAME SCREEN` restores the touch screen. On a single screen, `ONE SCREEN` fills the display with the main game and floats the touch screen only for mapped interaction states, while `TOUCH`/hotkey/manual controls remain available. `SIDE BY SIDE`, `LARGE + SMALL`, `PICTURE IN PICTURE`, and `STACKED` are explicit user layouts; mapped game pages that need both displays use paired presentation, and transitions retain a dimmed prior frame until the next page is ready. The [HUD manual](https://github.com/drake-dot-o/pokedaisy-windows/blob/v1.6.2-beta.1-windows/docs/manual/hud.md) also specifies that the field HUD yields to battles/menus and returns afterward. These observable rules—not Windows-specific rendering code—are the target for 3DS parity. Current Ultra Moon support still lacks verified field/menu/battle scene mappings, so those automatic transitions remain incomplete.

## DS versus Azahar transition audit (2026-11-22)

The Android DS path (`EmulatorEngine.watchScreens`) samples core-provided screen flags in the frame loop. It debounces changes, distinguishes a touch-only page from a page that uses both displays, waits for bottom-screen fade-in, and exposes a held transition state so the previous picture/layout remains visible until the next screen is ready. The 3DS path (`N3dsSession.scenes`) instead polls Azahar's loaded module names every 100 ms and recognizes `FIELD`, `MENU`, `BATTLE`, or `SCREEN` only for titles with a verified `Gen6Reader.Config`; its mapped states currently change presentation without an equivalent frame-by-frame fade/transition hold. Ultra Moon has no `Config`: only EU `LangSelect` is verified as `SCREEN`, and other module sets remain `UNMAPPED`. A read-only check on 2026-11-22 found the emulator process alive and its last recorded module trace for the current process still `LangSelect`; no game input was sent. Do not implement USUM scene labels or touch requirements from CRO filenames alone. First collect verified field/menu/battle runtime module and memory evidence; then add title-specific recognition and test scene transitions against the DS behavior contract.

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

## Repeated Ultra Moon dialog heap comparison (2026-11-22)

A fresh read-only 16 MiB capture from the running API 36 x86_64 emulator was compared with the earlier Kukui dialog and photo-view snapshots. The two dialog captures differ at 7,132 bytes (34 of 4,096 pages), while the earlier AppDemo/dialog pair differs from the photo-view capture at 86,455 bytes. Across three dialog captures, 516 aligned 32-bit words were identical and different in the photo-view capture; these cluster at heap offsets `0x00E3B43C` through `0x00E3BB00`, where the bytes resemble render/geometry values rather than a stable state enum or pointer. The rest of the candidate fields did not remain stable across repeated dialogs. No address or scene rule is adopted from this diff.

The new snapshot was pulled to the ignored local `work/` analysis directory, and its temporary device copy was removed. The game received no input; the S25 was not accessed.

## Published USUM runtime address candidates (2026-11-22)

The local, read-only [PKMN-NTR source](https://github.com/drgoku282/PKMN-NTR/blob/143d4a88f6a88b84f916b215effa18d8e4acbdda/PKMN-NTR/Helpers/LookupTable.cs) provides candidate Gen 7 process addresses for Ultra Sun / Ultra Moon: trainer data at `0x33012818` (trainer name `0x33012850`), bag at `0x33011934`, money at `0x33015210`, and a six-slot party candidate at `0x33F7FA44` (the source also notes `0x330128E4` as an alternative). The party records use the Gen 7 PKM shape already decoded by `Gen6Reader` (0xE8 encrypted bytes plus the 0x1C party-stat tail), so the existing checksum/decrypt path is reusable if the live range validates.

History places the party/trainer offsets in PKMN-NTR's [initial Ultra Sun support](https://github.com/drgoku282/PKMN-NTR/commit/c681e9cec47ce676a2ab11ff43e9c12acec82eac) and [USUM trainer-data update](https://github.com/drgoku282/PKMN-NTR/commit/f7258d6a9c5859eca38edcca13d01f0160483438). Its [USUM 1.2 support commit](https://github.com/drgoku282/PKMN-NTR/commit/95a4178ecc0633dd1670eef38d4089180a0d8c38) changes a separate breeding offset and leaves the party candidate unchanged. This makes the party address a stronger lead than copying Sun/Moon offsets, but the source is an external NTR tool, its alternative party address is unresolved, and it does not prove the user's European base-ROM build or current emulator state. Do not enable a production config from these addresses alone. Next validation should read these sparse ranges from the already-running emulator at the authorized setup checkpoint, verify encrypted-party checksums and trainer-language/name structure where initialized, then compare repeated samples before enabling only the fields that pass. The current active scene must remain untouched.

## USUM party address source cross-check (2026-11-22)

The live diagnostic address `0x33F7FA44` is independently listed as the Ultra Sun/Ultra Moon party base by [PKMN-NTR](https://github.com/drgoku282/PKMN-NTR) and implemented in [PokeStreamer-Tools' Gen 6/7 auto-layout reader](https://github.com/EverOddish/PokeStreamer-Tools/blob/master/gen_6_7/auto_layout_gen6_gen7.py). That reader uses a 484-byte slot stride and appends 22 bytes of party stats. Our emulator capture at that base contains six empty records, consistent with the observed early setup session before choosing a starter; therefore the address is a strong research candidate, not a live-validated Companion map. A later safe checkpoint with a Pokémon in the party is still needed to verify record decoding, EU title/version behavior, trainer data, and other addresses. Do not infer bag, battle, field, or screen-state offsets from this address.

## Ultra Moon CRO inventory and current module trace (2026-11-22)

Read-only scripts `work/usum-cro-inventory.py` and `work/usum-cro-symbols.py` were run against the user's local EU ROM. The RomFS contains distinct CRO assets for `FieldRo`, `FieldMenu`, `Battle`, `Bag`, `Status`, `HeroSelect`, `PhotoView`, and `TrainerPhoto`; their CRO export tables mostly expose only the generic `nnroControlObject_`, so file presence/export names do not identify the active runtime scene or touch-screen need. The current live log for PID `32726` records an empty module list followed by `LangSelect` and `startup scene: SCREEN`; it provides no field/menu/battle trace yet. The screenshot taken during this still-running session showed “Die Einstellungen wurden gespeichert.”, so the visual dialogue and startup module should not be conflated with later scene mappings.

A read-only `shot bottom-now` debug command was consumed (`n3ds_cmd.done` updated) but produced no image file. That attempt does not establish the lower screen's current pixels. Do not infer SCREEN policy from it. Next meaningful module evidence requires observing an authorized gameplay transition; until then leave all non-LangSelect USUM scenes unmapped and retain manual TOUCH SCREEN behavior.
