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

The pinned Azahar source has been inspected at commit `219d743a1e698101fe1ab8bef43e6563126c3c27`. The concrete
cpp-httplib notice gap was fixed: its MIT text is now in `NOTICE` and the optional Android license asset, and the
packaged APK was checked for both the component heading and full license text on 2026-11-24. The separate Khronos
`khrplatform.h` permission notice is also included in the optional asset and verified in the rebuilt APK; GLAD's
generated-loader terms remain unresolved. Other questions include generated system archive data, mixed GPL headers,
and runtime changes in PokeDaisy's patch. The detailed source paths, build references, and unresolved obligations are recorded in
[the Azahar Android license audit](3DS_AZAHAR_LICENSE_AUDIT.md). This work does not establish that an APK containing
the optional Azahar core is redistributable or PR-ready.

The default Android CI job intentionally skips Azahar. A separate `android-3ds` job now fetches the pinned Azahar
submodule recursively and runs `:app:testDevUnitTest` plus `:app:assembleDev -Ppokedaisy.n3ds=true` on CI, so Kotlin,
JNI, native configuration, and packaging drift can be caught without changing the normal fast Android job. The job is
limited to CI pull requests and pushes; it is skipped when this workflow is called by a release workflow, so it does
not enable Azahar redistribution in signed public releases. Treat it as advisory until the native build is observed
passing consistently on GitHub's runners and the license/provenance questions above are resolved.

## Audit limits

This remains an incomplete upstream/readiness review. On 2026-11-24, the focused `DsStagePolicyTest` passed 19 tests;
`Gen6Test.standaloneTouchOnlyPagesRequestTheSecondScreen` and
`DsStagePolicyTest.standaloneTouchPagesStayVisibleAcrossEveryChosenLayout` passed; the manual/docsearch suite passed
9 tests; and `:app:assembleDev` succeeded with `pokedaisy.n3ds=true`. The generated `assets/licenses.txt` in that APK
contains the cpp-httplib MIT text and the separate Khronos khrplatform permission notice. The installed Dev APK started
into `LibraryActivity` on an S25 via the existing installation; no 3DS ROM was launched on-device in that check. Y gameplay testing continues in a disposable
headless profile. Ultra Moon gameplay mapping and direct S25 game validation remain incomplete, and the unresolved
license/provenance questions above prevent a claim of PR or distribution readiness.
