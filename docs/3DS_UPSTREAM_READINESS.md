# Native 3DS upstream readiness

Audit of the current branch against the project instructions in `CLAUDE.md`, `README.md`, `docs/DEVELOPMENT.md`,
`docs/manual/README.md`, `docs/manual/devices.md`, and the release/build configuration. This is an engineering status
note, not a claim that the native 3DS build is ready for distribution or an upstream pull request.

## Current scope in code

The Android 3DS integration is optional: `settings.gradle.kts` includes the `n3ds` module only with
`pokedaisy.n3ds=true`, and `app/build.gradle.kts` adds Azahar's license texts only for that build. The library and app
paths use the existing game screen and companion rather than a separate 3DS application. The public README currently
calls the console port an early preview and links to the 3DS install guide.

`N3dsCart.KINDS` recognizes Pokémon X, Y, Omega Ruby, Alpha Sapphire, Sun, Moon, Ultra Sun and Ultra Moon. The
distinction between recognized and supported titles matters: `PLAYABLE` includes all eight, while `SUPPORTED` omits
Ultra Sun and Ultra Moon. `Gen6Reader` has X/Y and ORAS/SM layouts; the Ultra Moon setup-scene handling does not add a
companion memory configuration. Existing `OrasSmCartTest` assertions explicitly expect both Ultra titles to be
unsupported and their reader configurations to be absent. Therefore Ultra Moon is currently a boot-only target, not
parity with Pokémon Y or the other mapped titles.

The test sources include focused checks for cartridge identification, screen layouts, save handling, save-editor data,
scene transitions, touch battle input, snapshots, and companion rendering. The CI workflow runs translation checks,
generated-data checks, unit tests, and the default Android debug build. It deliberately excludes the optional Azahar
submodule, so that CI path does not compile or validate the Android 3DS variant. The separate `.github/workflows/3ds.yml`
builds the Homebrew 3DS port and its host regressions; it is not the Android Azahar build.

`docs/ROM_FEATURES.md` documents per-ROM hook support for the Game Boy through DS feature matrix. Native 3DS games
use a different core and reader, so this audit is linked there as the explicit scope boundary; no native 3DS claims are
folded into the older matrix.

## Upstream convention checks

- GitHub Actions references in the audited workflows are pinned to commit SHAs with version comments. Downloaded
  `makerom` and `bannertool` archives also have SHA-256 checks in the 3DS workflow.
- CI runs `scripts/check_translations.py` and the translation unit tests. A dedicated `N3dsSettingRowsTest` covers the
  optional 3DS settings rows. These checks are evidence of coverage in the source, not a test run for this audit.
- The manual's own contribution guide requires a player-facing manual update in the same pull request for supported
  games, settings, labels, and behavior. It also says translated manual pages are maintained in all eight locales
  when an existing translated page changes. This engineering note is not a substitute for those manual updates.
- `CLAUDE.md` requires per-ROM support changes to update `docs/ROM_FEATURES.md`, requires bundled code and fonts to be
  represented in `NOTICE` and the in-app license list, and prohibits committing game ROMs, saves, or secrets.
- No workflow change was necessary from this audit: the native 3DS app build is optional Android work and is not
  silently represented as covered by the Homebrew-port workflow.

## Distribution and pull request gates

`NOTICE` correctly identifies the optional Azahar revision and warns that it does not establish redistribution
compatibility. The current record identifies two unresolved review items: the pinned Azahar tree has mixed GPL notices,
including a GPL-only FAAD2 component, and the generated GLAD loader has no license file or confirmed attribution in
that tree. The Azahar submodule is not present in this checkout, so this audit could not inspect those source notices
or establish the terms for the exact linked objects. Do not describe an APK containing the optional Azahar core as
redistributable or PR-ready until the exact build inputs, notices, and applicable licenses have been reviewed and the
GLAD attribution is resolved.

The Android 3DS path also needs a reproducible CI/build check with `pokedaisy.n3ds=true`; the current default CI
intentionally skips Azahar. That check should only become required after it can fetch and validate the pinned source
and all required license texts without relying on a developer's local submodule state.

## Audit limits

This was a source and workflow review only. It did not run Gradle, compile either Android variant, run ROM-backed tests,
or inspect physical-device behavior. No claim about runtime stability follows from this document.
