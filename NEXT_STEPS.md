# Next Steps

1. Capture the result of focused `N3dsCartTest`, `Gen6Test`, and `N3dsBattleTapsTest`; investigate failures specifically related to 3DS behavior.
2. Review upstream `docs/3DS_GAMES_PLAN.md` alongside test reports and establish a legal homebrew/core smoke test path that does not require commercial game data.
3. When the user's Pokémon Y dump becomes available, verify format/readability without changing the original and run on the physical ARM64 device with only the user's supporting files.
4. Validate single-screen X/Y UI on hardware, including transitions, battle controls, overlays, touch, audio, saves, memory, and stability. Capture screenshots/logs and record device conditions.
5. Reconcile any code defects found against existing upstream behavior; keep changes focused and add regression coverage.
6. Run focused tests, relevant full tests, both standard and feature-enabled builds, and `git diff --check` before committing.
7. Commit validated changes and push only to `origin`. Review fork diff and licensing before drafting (not submitting) an upstream PR; request user approval before submission.
- Focused N3DS unit tests could not run because a stale Gradle test executor held a lock on generated test output; retry after clearing the generated test-results directory or rebooting.
