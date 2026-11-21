package com.pokedaisy.app.n3ds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class N3dsSavesTest {
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

    private fun assertNotNullAndReturn(format: N3dsSaves.Format?): N3dsSaves.Format {
        assertNotNull(format)
        return format!!
    }
}
