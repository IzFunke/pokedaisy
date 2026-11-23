# 3DS save editor integration

## Current XY groundwork

`Gen6XySave` validates decrypted Pokémon X/Y save size, BEEF footer, all 55 XY block CRCs, and can rebuild those CRCs after changed data. It exposes checked access to all 31×30 PC slots and six party slots. `Gen6XySaveEditor` connects this to PokeDaisy's shared `MonSaveEditor` and existing checked save-write flow. It reads localized game text and personal data from the selected ROM. The first enabled edits are PC-slot swaps and releasing PC Pokémon. A copied Pokémon Y save has been exercised across all 930 box records and six party slots; an edited PK6 was written into a copied save, reopened, and verified without changing the source fixture. Its block table follows the GPL-3.0-licensed PKHeX.Core `SaveBlockAccessor6XY` / `BlockInfo3DS` implementation; the app is GPL-3.0 as well.

The library, game-info panel, and save editor resolve an X/Y save through `SavesLocation.saveFor`, so the editor reads and commits the same ROM-basename `.sav` file as the game. When that file is first requested, Azahar's `main` archive (or the previous title-ID archive) is copied into that canonical path without deleting the source. `SaveFileEdits.commit` keeps the existing checked write, backup, and fresh-boot behavior for these saves.

## PKHeX.Core binding evaluation

PKHeX.Core is a .NET class library, while PokeDaisy's editor is Kotlin/Android. The current X/Y integration uses a focused Kotlin codec behind the shared editor interface; a .NET bridge has not been adopted. That keeps the Android runtime and packaging unchanged while the XY format surface remains scoped and fixture-tested.

If future save formats require broad PKHeX functionality, revisit bindings as a separate architecture decision with Android ABI, packaging, size, and license checks. Do not add a desktop-only dependency to the Kotlin app.

## Remaining work for XY and USUM

For XY, verify the connected PC swap/release UI, save, and reopen on isolated X and Y copies. Add party/PC transfer only after party-record conversion and stat recalculation pass fixture checks. Add name encoding and other Pokémon field edits with XY-specific legality rules. ORAS remains a separate save layout.

For Ultra Moon, implement a separate USUM block map and Gen 7 CRC scheme, preserve/reapply the USUM meme-crypto transform around block access, add PK7 record editing and USUM-specific personal/game tables, then validate against copied USUM saves. Do not route USUM through the XY parser.

### Gen 7 signing constraint

Writing an edited USUM save requires a fresh MemeCrypto signature. PKHeX clears this signature when it opens the save and signs the output only after updating the block checksums (`PKHeX.Core/Saves/SAV7.cs`, `GetFinalData` / `MemeCrypto.SignInPlace`). The reference signer contains private RSA key material (`PKHeX.Core/Saves/Encryption/MemeCrypto/MemeKey.cs`); the public implementation also exposes it in `FlagBrew/memecrypto` (`rsa.c`, `privkey[]`). The project rule forbids shipping keys or secrets, so PokeDaisy must not add that signing material or write modified USUM saves with a stale signature. Keep the XY editor title-ID-gated and preserve USUM files untouched until a compliant signing path exists. A read-only USUM save view can be considered separately, but it does not satisfy editing.
