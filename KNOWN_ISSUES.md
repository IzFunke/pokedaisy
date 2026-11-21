# Known Issues and Limitations

- Pokémon Y content was not present in the designated dump directory at inventory time. No gameplay compatibility claim can be made.
- Physical hardware app startup and JNI/Android setup are verified; launching a title through Azahar, rendering, audio, controls, save handling, performance and stability are not yet verified.
- The Azahar loader recognizes 3DSX homebrew, but PokeDaisy's current game entry path accepts only decrypted supported retail NCSD carts. No homebrew test harness or content is currently wired through the app.
- The x86_64 AVD with ARM64 translation crashes with SIGILL on startup. Do not retry ARM64 Azahar runtime there; use it only if a separate x86_64 native build becomes available and tested.
- The full app unit suite completes with 4,194 tests, 17 failures and 59 skips; focused 3DS logic and single-screen screenshot suites pass. See `TEST_RESULTS.md` for exact failure classes.
- Azahar's native binary makes the optional APK large. Review size/performance before release.
- Existing source documentation notes Pokémon Y lacks device validation. Update that only after actual gameplay checks.
