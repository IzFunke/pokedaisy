# Known Issues and Limitations

- Pokémon Y content was not present in the designated dump directory at inventory time. No gameplay compatibility claim can be made.
- Physical hardware startup is verified; Azahar initialization, ROM loading, rendering, audio, controls, save handling, performance and stability are not yet verified.
- The x86_64 AVD with ARM64 translation crashes with SIGILL on startup. Do not retry ARM64 Azahar runtime there; use it only if a separate x86_64 native build becomes available and tested.
- The broad app unit-test task produced multiple failures in existing tests and stalled before a final summary. Focused 3DS tests are being checked separately.
- Azahar's native binary makes the optional APK large. Review size/performance before release.
- Existing source documentation notes Pokémon Y lacks device validation. Confirm current docs and actual test results before updating that claim.
