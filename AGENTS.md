# Repository agent rules

- On native Windows, invoke Gradle only with `.\gradlew.bat`. Use `./gradlew` only inside Linux, WSL, or Docker.
- Never execute the extensionless POSIX `gradlew` on Windows and never change Windows file associations to work around a wrong wrapper command.
- Keep `gradlew.bat` in the checkout. Host-side Python scripts that run Gradle must use `scripts/gradle_wrapper.py`.
- Tell every subagent which shell/environment it is using and require the matching wrapper before it starts builds or tests.
