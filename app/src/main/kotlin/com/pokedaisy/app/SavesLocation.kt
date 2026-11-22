package com.pokedaisy.app

import android.content.Context
import java.io.File

/**
 * Where game-save (SRAM/flash) files live, and how a ROM's save file is found
 * inside that folder. The format is identical whether the file is named
 * `.sav` (mGBA/standalone convention) or `.srm` (RetroArch/libretro convention)
 * — it's the same raw save-RAM dump either way, so both are recognized.
 */
object SavesLocation {

    /** The effective saves folder: [Prefs.savesDirOverride] if the user picked
     * one (see Settings > Folders), else this app's own private files/saves/. */
    fun dir(context: Context, prefs: Prefs = Prefs(context)): File {
        val override = prefs.savesDirOverride
        val dir = if (!override.isNullOrBlank()) File(override) else File(context.getExternalFilesDir(null), "saves")
        dir.mkdirs()
        return dir
    }

    /**
     * [rom]'s save file. Its own folder if the player set one (the library menu's SAVE FOLDER,
     * [Prefs.romSaveDir]); else the first of the saves folder and the ALSO LOOK IN folders
     * ([Prefs.extraSaveDirs], Settings > FOLDERS) that holds a save for it - so a save another
     * emulator keeps (RetroArch's per-core folders) is played and written where it is; else a
     * new save in the saves folder. Only the folders themselves are looked in, never below. A configured shared DS
     * folder takes precedence over both a per-game SAVE FOLDER and the ROM directory while selected; without it, a
     * per-game folder wins, then a writable DS ROM directory comes first ([dirsFor]).
     */
    fun saveFor(context: Context, prefs: Prefs, rom: File): File {
        val nds = RomIdentity.isNds(rom)
        val dirs = searchDirs(context, prefs)
        val dsFolder = prefs.dsSavesDirOverride?.takeIf { nds && it.isNotBlank() }?.let(::File)
        val search = dirsFor(rom, nds, dirs, dsFolder)
        val configuredOwn = prefs.romSaveDir(rom)?.let(::File)
        val own = ownFolderFor(nds, dsFolder, configuredOwn)?.apply { mkdirs() }
        return saveIn(own, search, rom)
    }

    /** [dirs] for [rom]: a configured shared DS folder first, then the ordinary save/search folders. This deliberately
     * overrides melonDS's legacy beside-ROM save location. Without that override, a writable DS ROM directory comes
     * first, then the ordinary folders so a save made elsewhere before still loads. */
    fun dirsFor(rom: File, nds: Boolean, dirs: List<File>, sharedDsFolder: File? = null): List<File> {
        if (nds && sharedDsFolder != null) return (listOf(sharedDsFolder) + dirs).distinctBy { it.absolutePath }
        val beside = rom.absoluteFile.parentFile ?: return dirs
        if (!nds || !(beside.canWrite() || resolve(beside, rom).exists())) return dirs
        return (listOf(beside) + dirs).distinctBy { it.absolutePath }
    }

    /** A shared DS folder is an explicit all-games choice, so a stored per-ROM folder is dormant until it is reset. */
    internal fun ownFolderFor(nds: Boolean, sharedDsFolder: File?, own: File?): File? =
        if (nds && sharedDsFolder != null) null else own

    /** [saveFor]'s rule: [own] if set and not overridden, else the first of [dirs] holding a save, else a new one. */
    fun saveIn(own: File?, dirs: List<File>, rom: File): File {
        own?.let { return resolve(it, rom) }
        return dirs.asSequence().map { resolve(it, rom) }.firstOrNull { it.exists() } ?: resolve(dirs.first(), rom)
    }

    /** The saves folder, then the ALSO LOOK IN folders, in [saveFor]'s order. */
    fun searchDirs(context: Context, prefs: Prefs): List<File> =
        (listOf(dir(context, prefs)) + prefs.extraSaveDirs.map(::File)).distinctBy { it.absolutePath }

    /** A folder that already holds saves, offered by first-time setup. */
    class Suggestion(val dir: File, val saves: Int)

    /**
     * Folders likely to hold the player's existing saves, with at least one
     * `.sav` / `.srm` in them: the linked ROMs folders (saves kept next to the
     * games) and its `saves/`, then RetroArch's save folders on every volume.
     * Blocking (lists folders) and needs All files access to see anything.
     */
    fun suggestions(prefs: Prefs): List<Suggestion> {
        val candidates = buildList {
            prefs.romsFolders.forEach { add(File(it)); add(File(it, "saves")) }
            for (root in StorageAccess.volumeRoots()) {
                for (core in RETROARCH_SAVE_DIRS) add(File(root, "RetroArch/saves/$core"))
                add(File(root, "RetroArch/saves"))
            }
        }
        return candidates.distinctBy { it.absolutePath }.mapNotNull { dir ->
            val n = dir.listFiles { f -> f.isFile && f.extension.lowercase() in SAVE_EXTENSIONS }?.size ?: 0
            if (n > 0) Suggestion(dir, n) else null
        }
    }

    private val SAVE_EXTENSIONS = setOf("sav", "srm")
    /** RetroArch's GBA cores, for "sort saves by core" setups. */
    private val RETROARCH_SAVE_DIRS = listOf("mGBA", "gpSP", "VBA-M", "VBA Next", "Beetle GBA")

    /**
     * Resolves [romBaseName]'s save file inside [dir]. Prefers an existing
     * `.sav`, falls back to an existing `.srm` (e.g. a save copied in from
     * RetroArch), and defaults to `.sav` for a brand-new save.
     */
    fun resolve(dir: File, romBaseName: String): File {
        val sav = File(dir, "$romBaseName.sav")
        if (sav.exists()) return sav
        val srm = File(dir, "$romBaseName.srm")
        if (srm.exists()) return srm
        return sav
    }

    /** [resolve] for [rom]: the first of its [RomArchive.saveNames] with a save in [dir],
     * else a new `.sav` under the first (an archive's save follows RetroArch's name). */
    fun resolve(dir: File, rom: File): File {
        val names = RomArchive.saveNames(rom)
        return names.asSequence().map { resolve(dir, it) }.firstOrNull { it.exists() } ?: resolve(dir, names.first())
    }
}
