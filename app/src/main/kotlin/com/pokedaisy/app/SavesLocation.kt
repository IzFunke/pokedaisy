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
     * folder takes precedence over both a per-game SAVE FOLDER and the ROM directory while selected. DS games otherwise
     * use PokeDaisy's standard saves folder and import an older beside-ROM save there on first access.
     */
    fun saveFor(context: Context, prefs: Prefs, rom: File): File {
        // 3DS archives have their own configurable shared directory and are migrated from Azahar's
        // title-id / `main` names here. Keep every caller (library, save editor, backup flow) on
        // the same ROM-basename `.sav` path.
        if (rom.extension.lowercase() in com.pokedaisy.app.n3ds.N3dsCart.EXTENSIONS) {
            val titleId = com.pokedaisy.app.n3ds.N3dsCart.header(rom)?.titleId
                ?: return com.pokedaisy.app.n3ds.N3dsSaves.sharedSavePath(context, prefs, rom, 0L)
            return com.pokedaisy.app.n3ds.N3dsSaves.saveFile(context, prefs, rom, titleId)
                ?: com.pokedaisy.app.n3ds.N3dsSaves.sharedSavePath(context, prefs, rom, titleId)
        }
        val nds = RomIdentity.isNds(rom)
        val dirs = searchDirs(context, prefs)
        val dsFolder = if (nds) prefs.dsSavesDirOverride?.takeIf { it.isNotBlank() }?.let(::File) ?: dirs.first() else null
        val search = dirsFor(rom, nds, dirs, dsFolder)
        val configuredOwn = prefs.romSaveDir(rom)?.let(::File)
        val own = ownFolderFor(nds, dsFolder, configuredOwn)?.apply { mkdirs() }
        if (nds) {
            val target = own ?: dsFolder ?: dirs.first()
            migrateDsSave(rom, target, listOfNotNull(configuredOwn, rom.absoluteFile.parentFile) + dirs)
        }
        return saveIn(own, search, rom)
    }

    /** Import an old DS save into the all-games location without removing the original or overwriting a newer target. */
    private fun migrateDsSave(rom: File, targetDir: File, oldDirs: List<File>) {
        val target = resolve(targetDir, rom)
        if (target.isFile) return
        val oldFiles = oldDirs.asSequence().distinctBy { it.absolutePath }
            .map { resolve(it, rom) }
            .filter { it.isFile && it.absolutePath != target.absolutePath }.toList()
        migrateLegacyDsSave(target, oldFiles)
    }

    internal fun migrateLegacyDsSave(target: File, oldFiles: List<File>): Boolean {
        if (target.isFile) return false
        val old = oldFiles.firstOrNull { it.isFile && it.absolutePath != target.absolutePath } ?: return false
        target.parentFile?.mkdirs()
        return runCatching { old.copyTo(target, overwrite = false); true }.getOrDefault(false)
    }

    /** A 3DS save is present in the selected shared save folder, after importing Azahar's `main` archive if needed. */
    fun hasSave(context: Context, prefs: Prefs, rom: File): Boolean {
        if (rom.extension.lowercase() in com.pokedaisy.app.n3ds.N3dsCart.EXTENSIONS) {
            val titleId = com.pokedaisy.app.n3ds.N3dsCart.header(rom)?.titleId ?: return false
            return com.pokedaisy.app.n3ds.N3dsSaves.hasSave(context, prefs, rom, titleId)
        }
        if (!RomIdentity.isNds(rom)) return saveFor(context, prefs, rom).exists()
        val dirs = searchDirs(context, prefs)
        val shared = prefs.dsSavesDirOverride?.takeIf { it.isNotBlank() }?.let(::File) ?: dirs.first()
        if (resolve(shared, rom).isFile) return true
        val legacy = listOfNotNull(prefs.romSaveDir(rom)?.let(::File), rom.absoluteFile.parentFile) + dirs
        return legacy.any { resolve(it, rom).isFile }
    }

    /** DS defaults to PokeDaisy's save folder; an older beside-ROM save is imported there by [saveFor]. */
    fun dirsFor(rom: File, nds: Boolean, dirs: List<File>, sharedDsFolder: File? = null): List<File> {
        if (nds && sharedDsFolder != null) return (listOf(sharedDsFolder) + dirs).distinctBy { it.absolutePath }
        val beside = rom.absoluteFile.parentFile ?: return dirs
        if (!nds || !(beside.canWrite() || resolve(beside, rom).exists())) return dirs
        if (nds) return (dirs + beside).distinctBy { it.absolutePath }
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
