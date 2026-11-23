# Ultra Moon scene and touch automation audit

Audit date: 2026-11-23. This is an evidence note only; no gameplay offsets or production behavior are changed here.

## Findings

- The generic 3DS policy currently treats every `Scene.FIELD` as touch-window-worthy, along with `MENU` and `SCREEN`. See [`DsStagePolicy.kt`](../app/src/main/kotlin/com/pokedaisy/app/DsStagePolicy.kt#L35).
- That is broader than the DS policy. The DS core supplies per-frame `FIELD_TOUCH` and, when enabled, `FIELD_TOUCH_BATTLE_MENU` flags; `EmulatorEngine.watchScreens()` derives `dsTouchWanted` from those bits and handles fade timing. It does not equate ordinary field play with a requested touch window. See [`EmulatorEngine.kt`](../app/src/main/kotlin/com/pokedaisy/app/EmulatorEngine.kt#L1524).
- The broader 3DS `FIELD` rule does **not** currently apply to Ultra Moon. `Gen6Reader.configFor()` has no USUM entry. USUM instead uses the separate startup-module reader at `0x008B2000`; only `LangSelect`, `HeroSelect`, and `StrInput` return `SCREEN`, and every other non-empty module set returns `UNMAPPED`. `N3dsSession` sends `config == null` through that reader. See [`Gen6Reader.kt`](../app/src/main/kotlin/com/pokedaisy/app/n3ds/Gen6Reader.kt#L395) and [`N3dsSession.kt`](../app/src/main/kotlin/com/pokedaisy/app/n3ds/N3dsSession.kt#L379).
- The isolated captures available at `C:\Users\beben\Documents\Codex\pokedaisy\work\usum-scene-investigation\` include `ultra-moon-field-60.png`, `ultra-moon-field-a60.png`, `um-field-before-x.state`, and `um-field-x-menu.png` / `.state`. The two PNGs labeled as field and X-menu captures show the outdoor field on the top display and an all-black lower-display area. The X-menu-labeled capture therefore does not verify that the menu opened or that its touch page rendered. The adjacent `.state` files were not decoded or modified in this audit.
- The harness’s `shot` command writes the current top and bottom renderer buffers into a 400x480 image, placing the bottom at y=240; the lower black area is part of the captured renderer output, not unused PNG canvas. See [`n3ds_dump.cpp`](../native-capture/n3ds_dump/n3ds_dump.cpp#L108) and [`README.md`](../native-capture/n3ds_dump/README.md#L31). This establishes what those captures render, but does not explain why the bottom buffer is black.
- `rom-cro-symbol-inventory.txt` is a static ROM inventory. It lists CRO names and import relationships such as `FieldMenu` importing `FieldRo`; it is not a runtime `modules` trace and cannot prove which CROs were loaded in either captured frame. No per-frame module trace or touch-state trace accompanies the listed field PNGs.

## PKMN-NTR reference

The public [PKMN-NTR 3.6-beta3 release](https://github.com/drgoku282/PKMN-NTR/releases/tag/3.6-beta3) describes USUM support for update 1.2. Its [LookupTable.cs](https://github.com/drgoku282/PKMN-NTR/blob/master/PKMN-NTR/Helpers/LookupTable.cs) lists US/UM candidates for trainer data (`0x33012818`, line 60), bag data (`0x33011934`, line 138), money (`0x33015210`, line 297), and party data (`0x33F7FA44`, with `0x330128E4` as an alternate, line 827). Those fields support data-reading research; they do not by themselves describe whether the bottom screen is visible or requests touch input.

The reference’s [Bot_SoftReset7.cs](https://github.com/drgoku282/PKMN-NTR/blob/master/PKMN-NTR/Bot/Bot_SoftReset7.cs) offers more relevant but still indirect probes:

- Its battle check reads `0x67107C` and expects the pointer value `0x304B742C` for US/UM (lines 136–170).
- Its menu routine explicitly presses X and then checks `0x6A60EA` for a value in `[0, 0x10000000)` (lines 204–221 and 1065–1081).
- After pressing A to open the bag, it checks `0x66EB54` for a value in `[0x01, 0x10001)` (lines 238–255 and 1097–1113).

These are candidate runtime predicates from an NTR bot flow, not verified PokeDaisy field/touch signals. The menu predicate is a range check, not a documented boolean flag. The headless field/X-menu captures listed above do not contain memory reads for those addresses, and this audit does not promote them into offsets or infer screen policy from them.

## Decision for follow-up

Unconditional `FIELD` does not match the DS trigger contract in the abstract. For Ultra Moon, however, it is not active yet because USUM has no gameplay config and its unknown gameplay modules remain `UNMAPPED`. To extend automation safely, a later investigation needs a paired runtime trace from the isolated Ultra Moon field, X menu, battle, and lower-screen buffer: loaded modules plus repeated reads around the NTR menu/battle candidates while the relevant screen is visibly opened and closed. A stable transition must be correlated with the lower screen actually changing. Until then, `UNMAPPED` plus manual `TOUCH SCREEN` remains the evidence-supported behavior.
