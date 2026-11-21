# Known Issues and Limitations

- Pokémon Y now boots on the Galaxy S25 Ultra to the first language prompt (about 59.8 FPS / 100% in the observed session). The selected language is German and awaits confirmation. Gameplay compatibility, audio, save persistence, broader controls, and stability remain unverified.
- Physical hardware app startup, JNI/Android setup, Pokémon Y title boot, rendering, and D-pad navigation are verified. Audio, save handling, broader controls, and stability are not yet verified.
- The Azahar loader recognizes 3DSX homebrew, but PokeDaisy's current game entry path accepts only decrypted supported retail NCSD carts. No homebrew test harness or content is currently wired through the app.
- The x86_64 AVD with ARM64 translation crashes with SIGILL on startup. Do not retry ARM64 Azahar runtime there; use it only if a separate x86_64 native build becomes available and tested.
- The full app unit suite completes with 4,194 tests, 17 failures and 59 skips; focused 3DS logic and single-screen screenshot suites pass. See `TEST_RESULTS.md` for exact failure classes.
- Azahar's native binary makes the optional APK large. Review size/performance before release.
- Some historical notes in `docs/3DS_GAMES_PLAN.md` describe earlier device-validation status; they are dated and remain as historical context. Current device evidence is summarized in `PROJECT_STATUS.md` and `TEST_RESULTS.md`.
- Ultra Sun / Ultra Moon are recognized for decrypted launch routing and Gen 7 presentation, but their distinct RAM maps are not implemented; companion data is intentionally disabled for those titles. No live USUM boot has been tested.
- Importing private preferences from the installed official package is blocked: it is a non-debuggable app with `allowBackup=false`. Dev's own preferences are preserved by `adb install -r`; Android does not permit reading the official package's private SharedPreferences on this unrooted device.
