package com.pokedaisy.app.n3ds

import com.pokedaisy.app.companion.data.GameKind
import com.pokedaisy.app.companion.data.RETAIL_ROM_DIR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class N3dsCartTest {
    @get:Rule val tmp = TemporaryFolder()

    /** A cart image's first bytes: NCSD at 0x100 with [titleId], the first partition at 0x4000, its NCCH's flags. */
    private fun cart(titleId: Long, noCrypto: Boolean, name: String = "game.3ds"): File {
        val b = ByteArray(0x4200)
        "NCSD".toByteArray().copyInto(b, 0x100)
        for (i in 0 until 8) b[0x108 + i] = (titleId ushr (8 * i)).toByte()
        b[0x120] = 0x20 // partition 0 at 0x20 media units = 0x4000
        "NCCH".toByteArray().copyInto(b, 0x4100)
        if (noCrypto) b[0x4000 + 0x18F] = 0x04
        return tmp.newFile(name).apply { writeBytes(b) }
    }

    @Test fun readsTitleIdAndCrypto() {
        val h = N3dsCart.header(cart(N3dsCart.POKEMON_X_USA, noCrypto = true))!!
        assertEquals(N3dsCart.POKEMON_X_USA, h.titleId)
        assertTrue(h.decrypted)
        assertFalse(N3dsCart.header(cart(N3dsCart.POKEMON_X_USA, noCrypto = false, "enc.3ds"))!!.decrypted)
    }

    @Test fun supportsOnlyDecryptedKnownCarts() {
        assertTrue(N3dsCart.supported(cart(N3dsCart.POKEMON_X_USA, noCrypto = true)))
        assertFalse(N3dsCart.supported(cart(N3dsCart.POKEMON_X_USA, noCrypto = false, "enc.3ds")))
        assertFalse(N3dsCart.supported(cart(0x0004000000033500L, noCrypto = true, "other.3ds")))
    }

    @Test fun sunAndMoonUseTheSupportedGen7Route() {
        for ((titleId, kind) in listOf(
            N3dsCart.POKEMON_SUN_USA to GameKind.POKEMON_SUN,
            N3dsCart.POKEMON_MOON_USA to GameKind.POKEMON_MOON,
        )) {
            val rom = cart(titleId, noCrypto = true, name = "$titleId.3ds")
            assertTrue(N3dsCart.playable(rom))
            assertTrue(N3dsCart.supported(rom))
            assertEquals(kind, N3dsCart.kindOf(titleId))
            assertEquals(N3dsLayout.Family.SM, N3dsLayout.forTitle(titleId).family)
        }
    }

    @Test fun ultraGamesAreLaunchableButDoNotClaimCompanionMemorySupport() {
        val us = cart(N3dsCart.POKEMON_ULTRA_SUN_USA, noCrypto = true, "ultra-sun.3ds")
        val um = cart(N3dsCart.POKEMON_ULTRA_MOON_USA, noCrypto = true, "ultra-moon.3ds")
        assertTrue(N3dsCart.playable(us))
        assertTrue(N3dsCart.playable(um))
        assertFalse(N3dsCart.supported(us))
        assertFalse(N3dsCart.supported(um))
        assertEquals(GameKind.POKEMON_ULTRA_SUN, N3dsCart.kindOf(N3dsCart.POKEMON_ULTRA_SUN_USA))
        assertEquals(GameKind.POKEMON_ULTRA_MOON, N3dsCart.kindOf(N3dsCart.POKEMON_ULTRA_MOON_USA))
        assertEquals(N3dsLayout.Family.SM, N3dsLayout.forTitle(N3dsCart.POKEMON_ULTRA_SUN_USA).family)
        assertEquals(N3dsLayout.Family.SM, N3dsLayout.forTitle(N3dsCart.POKEMON_ULTRA_MOON_USA).family)
        assertEquals(N3dsLayout.USUM, N3dsLayout.forTitle(N3dsCart.POKEMON_ULTRA_SUN_USA))
        assertEquals(N3dsLayout.USUM, N3dsLayout.forTitle(N3dsCart.POKEMON_ULTRA_MOON_USA))
        assertFalse(N3dsCart.playable(cart(N3dsCart.POKEMON_ULTRA_SUN_USA, noCrypto = false, "ultra-enc.3ds")))
    }

    @Test fun refusesWhatIsNoCart() {
        assertNull(N3dsCart.header(tmp.newFile("x.3ds").apply { writeBytes(ByteArray(0x4200)) }))
        assertNull(N3dsCart.header(tmp.newFile("short.3ds").apply { writeBytes(ByteArray(0x10)) }))
    }

    /** The player's Pokemon X, where the ROMs sit (`3ds/` beside the GBA folder): skipped without it. */
    @Test fun pokemonX() {
        val f = File(File(RETAIL_ROM_DIR).parentFile, "3ds/Pokemon X (USA) (En,Ja,Fr,De,Es,It,Ko).3ds")
        assumeTrue(f.isFile)
        val h = N3dsCart.header(f)!!
        assertEquals(N3dsCart.POKEMON_X_USA, h.titleId)
        assertTrue(h.decrypted)
    }
}
