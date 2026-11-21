# Next Steps

1. Build a legal 3DSX homebrew smoke path. The Azahar loader has 3DSX support, but PokeDaisy currently routes only recognized decrypted retail carts to `N3dsSession`. Keep the smoke harness dev/test-only unless general homebrew support is intentionally designed.
2. Extend device validation from `N3dsNative.init` to a title boot, surface rendering, input, audio, stop/pause, and save lifecycle. Avoid commercial game content until the user's Pokémon Y dump is available.
3. When the user's Pokémon Y dump becomes available, verify format/readability without changing the original and run it on the physical ARM64 device with only the user's supporting files.
4. Validate X/Y single-screen UI on hardware, including transitions, battle controls, overlays, touch, audio, saves, memory, and stability. Capture screenshots/logs and record device conditions.
5. Triage the 17 full-suite failures recorded in `TEST_RESULTS.md` separately; focused N3DS logic, single-screen screenshot, and ARM64 JNI initialization tests pass.
6. Reconcile code defects against current upstream behavior; keep changes focused and add regression coverage. Run relevant tests, standard and feature-enabled builds, and `git diff --check` before committing.
7. Commit validated changes and push only to `origin`. Review fork diff and licensing before drafting (not submitting) an upstream PR; request user approval before submission.
