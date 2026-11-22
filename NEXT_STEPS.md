# Next Steps

1. Build a legal 3DSX homebrew smoke path. The Azahar loader has 3DSX support, but PokeDaisy currently routes only recognized decrypted retail carts to `N3dsSession`. Keep the smoke harness dev/test-only unless general homebrew support is intentionally designed.
2. On the x86_64 AVD, resume the current Pokémon Y copy at the first language prompt. The user requested German; confirm only that authorized choice, then stop before any further personal setup prompt. Do not claim the language is persistent until a later restart proves it.
3. Extend physical-device validation to audio, broader controls, pause/resume, save lifecycle, and stability without changing the original ROM.
4. Validate X/Y single-screen UI on ARM64 hardware when the user has the S25 available, including transitions, battle controls, overlays, touch, audio, saves, memory, and stability. Capture screenshots/logs and record device conditions. The ARM64 AVD cannot run on the current x86_64 host.
5. Triage the 17 full-suite failures recorded in `TEST_RESULTS.md` separately; focused N3DS logic, single-screen screenshot, and ARM64 JNI initialization tests pass.
6. Reconcile code defects against current upstream behavior; keep changes focused and add regression coverage. Run relevant tests, standard and feature-enabled builds, and `git diff --check` before committing.
7. Commit validated changes and push only to `origin`. Review fork diff and licensing before drafting (not submitting) an upstream PR; request user approval before submission.
8. For a complete official-to-Dev settings migration, add an export to an official signed build or use a user-provided export/root-enabled device; current Android package isolation prevents direct extraction. Do not overwrite or reset either app's data.
9. Add/verify USUM memory maps from live title dumps before enabling companion snapshots; Ultra Moon boot is verified, but Ultra Sun boot and both titles' save lifecycle still need ARM64 hardware checks. No Ultra Sun dump was present in the user's checked ROM folders on 2026-11-22.
