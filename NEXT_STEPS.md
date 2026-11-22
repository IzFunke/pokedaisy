# Next Steps

1. Build a legal 3DSX homebrew smoke path. The Azahar loader has 3DSX support, but PokeDaisy currently routes only recognized decrypted retail carts to `N3dsSession`. Keep the smoke harness dev/test-only unless general homebrew support is intentionally designed.
2. Do not advance Ultra Moon past Kukui's photo question or make profile/photo selections. The S25 was at the PokeDaisy library during the last update; preserve Dev app data and do not assume a live game session is resumable.
3. Extend physical-device validation to audio, broader controls, pause/resume, save lifecycle, and stability without changing the original ROM.
4. Validate X/Y single-screen UI on ARM64 hardware when the user has the S25 available, including the API 36 touch-surface hide path, transitions, battle controls, overlays, touch, audio, saves, memory, and stability. Capture screenshots/logs and record device conditions. The ARM64 AVD cannot run on the current x86_64 host; also determine a verified hide/show fallback for Android API 35 and below.
5. Keep the full unit suite green after follow-up changes. The 2026-11-22 run passed 4,213 tests with 0 failures and 60 skips across 191 suites; rerun it after the next broad change.
6. Reconcile code defects against current upstream behavior; keep changes focused and add regression coverage. Run relevant tests, standard and feature-enabled builds, and `git diff --check` before committing.
7. Commit validated changes and push only to `origin`. Review fork diff and licensing before drafting (not submitting) an upstream PR; request user approval before submission.
8. For a complete official-to-Dev settings migration, add an export to an official signed build or use a user-provided export/root-enabled device; current Android package isolation prevents direct extraction. Do not overwrite or reset either app's data.
9. Build and run the existing `native-capture/n3ds_dump` once, against a scratch Azahar user directory and a read-only mount of the user's Ultra Moon image. Discover its CRO root/modules and inspect memory without changing the live save or advancing past the photo question.
10. Derive and validate Ultra Moon party, trainer, location, field-menu, battle-menu and battler mappings from available runtime evidence before enabling Companion samples or automatic lower-screen behavior. If a required state cannot be observed before the user's stop point, continue with source analysis and request permission before progressing through that setup screen.
11. Once Ultra Moon behavior is verified, extend the same adapter architecture to Ultra Sun, Sun/Moon and X/Y/ORAS only where each title's addresses are separately validated. Keep the S25 Ultra's package data intact and use `adb install -r` for any authorized Dev update.
# Next: finish measured USUM runtime mapping

The isolated language-select savestate now verifies the Ultra Moon EU root `0x008b2000` and `LangSelect` module, and the
Dev app opens both displays automatically for that one scene. All other USUM modules still use `UNMAPPED` and the manual
touch-screen fallback. Next, obtain a verified field module/root and game-owned live structures for field menu, party,
and battle before adding a USUM `Gen6Reader.Config` or enabling further automatic stage changes. Preserve the stop before
Kukui's photo selection; do not make the photo/profile choice.

12. Current x86_64 emulator session is at Kukui's photo/profile setup view after the authorized German choice. Do not confirm or choose a photo/profile. Continue source analysis and mapping work without sending game input; resume gameplay only after the user authorizes that setup choice.

The current AppDemo/photo-view heap comparison is non-diagnostic for game state: two snapshots differ in 86,455 bytes, but no reliable state marker was identified, and the Sun/Moon zone offset is not validated for Ultra Moon. Continue with read-only CRO/module and source analysis until the user explicitly resumes game setup; do not turn this snapshot into a memory mapping.
