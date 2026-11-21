# Decisions

- Continue in the user's personal fork and feature branch; use `origin` for pushes and never push to `upstream`.
- Reuse the integration already present in upstream source; do not rebuild emulator features already implemented.
- Keep Azahar behind `-Ppokedaisy.n3ds=true`; package ARM64. A Windows x86 host can cross-build the Android ARM64 target, but that says nothing about runtime compatibility on x86.
- Use the isolated `.dev` application ID for device experiments. Preserve any existing production install and app data.
- Do not change the Azahar pin until there is a concrete defect or a verified benefit plus compatibility results.
- Correct the submodule setup command to include DSperate because Android build/license tasks require it.
- Do not create or submit the upstream pull request before user approval.
