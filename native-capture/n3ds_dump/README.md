# n3ds_dump: headless Azahar for 3DS fixtures

`nds_dump`'s twin for 3DS carts (docs/3DS_GAMES_PLAN.md). It links Azahar's core libraries from `third_party/azahar`
(our patch applied at configure time, the key blob forced off as in the app) with the software renderer, so it needs no
GPU, Android or Docker: it builds on the Mac with CMake 3.25+ (the Android SDK's `cmake/3.31.6` has Ninja with it).

```bash
git submodule update --init --recursive third_party/azahar
SDK=~/Library/Android/sdk/cmake/3.31.6/bin
$SDK/cmake -S native-capture/n3ds_dump -B native-capture/n3ds_dump/build -G Ninja -DCMAKE_MAKE_PROGRAM=$SDK/ninja
$SDK/ninja -C native-capture/n3ds_dump/build n3ds_dump
native-capture/n3ds_dump/build/n3ds_dump CART.3ds USERDIR [--save MAIN] [--new3ds] < script.txt
```

Only decrypted carts boot (no keys, ever). `USERDIR` is Azahar's user folder for the run (its emulated SD card, NAND and
config): give it a scratch folder. `--save MAIN` copies a save (a Checkpoint / JKSM backup's `main`) into the cart's save
folder there first; the game then writes its saves to that copy, never to `MAIN`. The clock and the start-up ticks are
fixed, so a script plays the same way each run.

## Script commands

One per line from stdin; `#` starts a comment. Numbers take `0x` hex. Keys are `A B X Y UP DOWN LEFT RIGHT L R START
SELECT ZL ZR HOME`, the circle pad pushed all the way `CUP CDOWN CLEFT CRIGHT` (Sun / Moon walk with it alone: their D-pad
only turns the player), joined with commas (`A,UP`, `CUP,CRIGHT`), or `NONE`. A frame is one the software renderer finished.

| Command | What it does |
| --- | --- |
| `wait N` | Runs N frames with the held keys. |
| `waittight N` | Runs N frames with the normal full CPU slice even when step mode is enabled. Useful for comparing scheduler behavior after loading a checkpoint. |
| `step N` | Runs N single-step scheduler iterations; reports elapsed time, calls and completed frames every two seconds. |
| `keys K` | Holds K from now on (`keys NONE` lets go). |
| `press K` / `hold K N` | K for one frame (or N), then one frame without. |
| `touch X Y [N]` | The stylus at (X, Y) on the bottom screen (320x240) for N frames (default 2), then up. |
| `touchmap X Y` | Prints the bottom-screen bounds and mapped framebuffer coordinate without input or frame advancement. |
| `shot FILE` / `shots PREFIX EVERY COUNT` | Top screen over the bottom one, as a 400x480 PNG. |
| `regions` / `dump DIR` | The game process's mapped memory (base, size, permissions); `dump` writes each as `<base>.bin`. |
| `peek8/16/32 ADDR`, `poke8/16/32 ADDR V` | The game process's memory. |
| `savestate FILE` / `loadstate FILE` | An Azahar state. |
| `bench N` | N frames flat out, then ms a frame. |
| `modules` | The loaded CRO modules, as the app reads them (`DllField DllUSC02 ...`). |
| `waitmod M [N]` / `waitnomod M [N]` | Frames until module M is loaded (or gone), at most N (3000). |
| `pressuntil K M [N] [EVERY]` | K for 2 frames every EVERY until module M is loaded, at most N frames; EVERY defaults to 60. |
| `watchw ADDR [N] [LEN]` | N frames (2) with ADDR's page watched: who wrote its LEN bytes (4), as PC / LR / value, counted (our Azahar patch's `Memory::pk_watch_write`). |
| `frames` | Frames run so far. |
| `findcrs` | Every page whose +0x80 reads "CRO0", with its chain: the static module's header for a new game. |
| `modroot ADDR` | Where `modules` starts (each known game's is set at boot: X / Y 0x012B6000, OR / AS 0x01307000, Sun / Moon 0x00869000, Ultra Moon 0x008B2000). |
| `echo TEXT` | Prints TEXT: a marker for a script fed live (stdin from `tail -f` on a file you append to). |
| `peekf ADDR` / `hex ADDR [LEN]` | A float; LEN bytes (0x40) as 16-byte lines. |
| `search32 V [MAX]` | Every aligned word equal to V in the game's memory (64 at most). |

`N3DS_STEP_RUNLOOP=1` makes `wait` use `RunLoop(false)` single-step iterations instead of the normal full CPU slice.
This can get past a full-slice wait during initial boot; it is much slower, so use `waittight N` to compare full-slice
execution at a loaded checkpoint. `N3DS_WAIT_PROGRESS=1` prints the active `wait`, `press`, or `hold` RunLoop call
count, rendered-frame count and elapsed time, including markers around the first two calls. This makes a slow key pulse
observable while it is waiting for its frame to render. Both variables are diagnostic opt-ins; default behavior is
unchanged.

`N3DS_TOUCH_TRACE=1` adds lines to each `touch` command with the supplied bottom-local coordinates, current
`bottom_screen` bounds, mapped framebuffer coordinate, and whether `TouchPressed` accepted that coordinate. It also
prints the registered `emu_window` touch-device's normalized position and pressed state after the press, after the
requested frames, and after release. This distinguishes an in-bounds pointer from the state delivered by the input
device. The trace does not change touch duration or release behavior. The frame-neutral `touchmap X Y` command prints
the bounds and mapping without injecting touch or advancing emulation; use it to verify a target point before issuing
`touch`.

On Windows, `scripts/run_n3ds_dump_bounded.ps1` runs the harness under an external wall-clock deadline, records tagged
stdout and stderr lines in a new log file, and kills the child process tree when the deadline expires (exit code 124).
For example:

```powershell
$userDir = (Resolve-Path '.\scratch\isolated-userdir').Path
$rom = (Resolve-Path '.\Pokemon Y (Europe).3ds').Path
$saveCopy = (Resolve-Path '.\scratch\Pokemon Y.sav').Path
$arguments = @($rom, $userDir, '--save', $saveCopy)
pwsh -NoProfile -File .\scripts\run_n3ds_dump_bounded.ps1 `
  -Executable (Resolve-Path '.\native-capture\n3ds_dump\build\n3ds_dump.exe').Path `
  -ArgumentListJson (ConvertTo-Json -Compress -InputObject $arguments) `
  -TimeoutSeconds 300 -LogFile '.\scratch\pokemon-y-headless.log' -IsolatedUserDir $userDir
```

The log path must not already exist. The explicit `-IsolatedUserDir` is a caller acknowledgement, not a safety check;
always use a disposable user directory and save copy because the game may write into that directory. This helper does
not inspect or alter ROMs, saves, or user data. It can stop Azahar only after the operating system terminates its
process; it does not add an interrupt into a currently executing core call. Run the smoke check with
`pwsh -NoProfile -File .\scripts\test_run_n3ds_dump_bounded.ps1`; it uses only temporary fake child processes.

`N3DS_INTERP=1` runs Azahar's interpreter instead of the JIT. Sun's own save, two of three Moon saves and Alpha Sapphire's
battles used to crash this tool as they loaded: the software renderer's worker threads shared Azahar's cached physical
memory region (one thread's half-written cache gave another a region's start with another region's memory). Our patch
drops that cache and skips a texture that runs past the console's memory; they all play now.

`N3DS_LOG` sets Azahar's log filter (default `*:Warning`, e.g. `N3DS_LOG='*:Info'`).

A module loads a few seconds before its screen shows (the field's with the start menu still up): wait ~500 frames after
`pressuntil A DllPssMessageWindow` before walking. `savestate` still fails on Pokemon X (a boost archive class isn't
registered), so every X run boots. Ultra Moon savestates work and can resume an isolated setup/intro checkpoint; never
use a personal save directory for diagnostic inputs.
