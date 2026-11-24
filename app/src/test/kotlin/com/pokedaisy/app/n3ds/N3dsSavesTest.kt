package com.pokedaisy.app.n3ds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class N3dsSavesTest {
    @Test fun sharedSaveNameUsesRomBasenameAndSavExtension() {
        assertEquals("Pokemon Y (Europe).sav", N3dsSaves.sharedSaveName(File("Pokemon Y (Europe).3ds")))
    }

    @Test fun archiveFileNameUsesFullTitleId() {
        assertEquals("0004000000055e00", N3dsSaves.archiveFileName(N3dsCart.POKEMON_Y_USA))
    }

    @Test fun ultraMoonMetadataFormatMatchesTheLiveAzaharArchive() {
        val format = assertNotNullAndReturn(N3dsSaves.formatFor(N3dsCart.POKEMON_ULTRA_MOON_USA))
        assertEquals(0x40000, format.totalSize)
        assertEquals(1, format.directories)
        assertEquals(1, format.files)
        assertEquals(true, format.duplicate)
    }

    @Test fun ultraSunFormatIsNotGuessedWithoutLiveEvidence() {
        assertNull(N3dsSaves.formatFor(N3dsCart.POKEMON_ULTRA_SUN_USA))
    }

    @Test fun pokemonYSaveIsFoundUnderAzaharSdProfileIds() {
        val root = Files.createTempDirectory("pokedaisy-y-save").toFile()
        try {
            val folder = File(root, "sdmc/Nintendo 3DS/${"a".repeat(32)}/${"b".repeat(32)}/title/00040000/00055e00/data/00000001")
            assertTrue(folder.mkdirs())
            File(folder, "main").writeBytes(byteArrayOf(1, 2, 3))

            assertEquals(folder, N3dsSaves.saveFolder(root, N3dsCart.POKEMON_Y_USA))
            assertTrue(N3dsSaves.saveFolder(root, N3dsCart.POKEMON_Y_USA).resolve("main").isFile)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun azaharSaveIsCopiedToPokeDaisysSharedArchivePath() {
        val root = Files.createTempDirectory("pokedaisy-y-azahar").toFile()
        try {
            val azaharSave = File(N3dsSaves.saveFolder(root, N3dsCart.POKEMON_Y_USA), "main")
            azaharSave.parentFile!!.mkdirs()
            azaharSave.writeBytes(byteArrayOf(4, 5, 6))
            val shared = File(root, "saves/Pokemon Y (Europe).sav")

            assertTrue(N3dsSaves.syncFromAzahar(root, N3dsCart.POKEMON_Y_USA, shared))

            assertEquals(azaharSave.readBytes().toList(), shared.readBytes().toList())
            assertTrue(azaharSave.isFile)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun checkingForASaveDoesNotCopyAzaharsWorkingArchiveIntoTheSharedFolder() {
        val root = Files.createTempDirectory("pokedaisy-y-library-save-check").toFile()
        try {
            val azaharSave = File(N3dsSaves.saveFolder(root, N3dsCart.POKEMON_Y_USA), "main")
            azaharSave.parentFile!!.mkdirs()
            val original = byteArrayOf(10, 20, 30)
            azaharSave.writeBytes(original)
            val sharedRoot = File(root, "shared-saves")
            val shared = File(sharedRoot, "Pokemon Y (Europe).sav")

            assertTrue(N3dsSaves.hasSave(root, sharedRoot, File("Pokemon Y (Europe).3ds"), N3dsCart.POKEMON_Y_USA))
            assertEquals(
                azaharSave,
                N3dsSaves.existingSaveFile(root, sharedRoot, File("Pokemon Y (Europe).3ds"), N3dsCart.POKEMON_Y_USA),
            )

            assertFalse(shared.exists())
            assertFalse(sharedRoot.exists())
            assertEquals(original.toList(), azaharSave.readBytes().toList())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun failedAtomicSyncKeepsTheExistingSharedPathUntouched() {
        val root = Files.createTempDirectory("pokedaisy-y-azahar-sync-fail").toFile()
        try {
            val azaharSave = File(N3dsSaves.saveFolder(root, N3dsCart.POKEMON_Y_USA), "main")
            azaharSave.parentFile!!.mkdirs()
            azaharSave.writeBytes(byteArrayOf(4, 5, 6))
            val shared = File(root, "saves/Pokemon Y (Europe).sav")
            assertTrue(shared.mkdirs())
            val sentinel = File(shared, "keep")
            sentinel.writeBytes(byteArrayOf(9, 8, 7))

            assertFalse(N3dsSaves.syncFromAzahar(root, N3dsCart.POKEMON_Y_USA, shared))

            assertEquals(listOf<Byte>(9, 8, 7), sentinel.readBytes().toList())
            assertTrue(shared.isDirectory)
            assertFalse(shared.parentFile!!.listFiles().orEmpty().any { it.name.startsWith("${shared.name}.") && it.extension == "tmp" })
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun azaharMainArchiveMigratesToRomNamedSavAndKeepsItsSource() {
        val root = Files.createTempDirectory("pokedaisy-y-named-save").toFile()
        try {
            val azaharSave = File(root, "azahar/main")
            azaharSave.parentFile!!.mkdirs()
            azaharSave.writeBytes(byteArrayOf(7, 8, 9))
            val shared = File(root, "saves/Pokemon Y (Europe).sav")

            val resolved = N3dsSaves.copyArchiveToShared(azaharSave, shared)

            assertEquals("Pokemon Y (Europe).sav", resolved.name)
            assertEquals(azaharSave.readBytes().toList(), resolved.readBytes().toList())
            assertTrue(azaharSave.isFile)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun assertNotNullAndReturn(format: N3dsSaves.Format?): N3dsSaves.Format {
        assertNotNull(format)
        return format!!
    }
}
