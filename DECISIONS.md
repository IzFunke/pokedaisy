# Decisions

- Continue in the user's personal fork and feature branch; use `origin` for pushes and never push to `upstream`.
- Reuse the integration already present in upstream source; do not rebuild emulator features already implemented.
- Keep Azahar behind `-Ppokedaisy.n3ds=true`; package ARM64. A Windows x86 host can cross-build the Android ARM64 target, but that says nothing about runtime compatibility on x86.
- Use the isolated `.dev` application ID for device experiments. Preserve any existing production install and app data.
- Do not change the Azahar pin until there is a concrete defect or a verified benefit plus compatibility results.
- Correct the submodule setup command to include DSperate because Android build/license tasks require it.
- Do not create or submit the upstream pull request before user approval.
- Keep Ultra Sun/Ultra Moon out of `Gen6Reader.configFor()` until title-specific RAM addresses are verified against their own runtime. Reuse Gen 7 data/UI decoders where formats match, but do not infer RAM offsets from Sun/Moon.
- For recognized 3DS titles with no gameplay map, publish a clear Companion status and leave manual TOUCH SCREEN available. A narrowly observed startup module may drive only its verified screen behavior; unknown modules remain `UNMAPPED` rather than borrowing another game's map.
- A tap on the dim area closes an auto-opened 3DS battle bag/party page by sending B only when sampled scene and battle state identify that page; do not send a speculative button press for other battle states.

## 2026-11-22 3DS core option feasibility

The pinned Azahar 2126.2 source and official release assets confirm an Android ARM64 Libretro build exists. The app currently ships its own native Azahar front end, sets `ENABLE_LIBRETRO=OFF`, and contains no Libretro host. Azahar's CMake switches frontend configuration globally, so this is not a drop-in second `.so`. A Libretro option would need a functioning Android host and graphics/audio/input/storage/lifecycle bridges, and it would still run the same Azahar emulator engine. Continue with a contained host feasibility spike or identify a genuinely distinct maintained 3DS backend before adding a new selectable option. The Settings selector remains `AUTO` / `AZAHAR` until a second backend is real.
