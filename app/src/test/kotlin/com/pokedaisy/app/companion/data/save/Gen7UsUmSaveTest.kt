package com.pokedaisy.app.companion.data.save

import org.junit.Assert.*
import org.junit.Test

class Gen7UsUmSaveTest {
    @Test fun acceptsOnlyExactSizeFooterAndAllThirtyNineBlockChecksums() {
        val fixture = Gen7UsUmSave.checksummedFixture(ByteArray(Gen7UsUmSave.SIZE))
        assertTrue(Gen7UsUmSave.parse(fixture) is Gen7UsUmSave.Parsed.Ok)
        assertTrue(Gen7UsUmSave.parse(fixture.copyOf(fixture.size - 1)) is Gen7UsUmSave.Parsed.Failed)
        val corrupt = fixture.copyOf().also { it[0x1400] = 1 }
        assertTrue(Gen7UsUmSave.parse(corrupt) is Gen7UsUmSave.Parsed.Failed)
        val badFooter = fixture.copyOf().also { it[Gen7UsUmSave.SIZE - 0x1F0] = 0 }
        assertTrue(Gen7UsUmSave.parse(badFooter) is Gen7UsUmSave.Parsed.Failed)
    }

    @Test fun exposesTrainerStatusAndPk7SlotOffsetsWithoutMutatingSource() {
        val source = ByteArray(Gen7UsUmSave.SIZE)
        source[0x1400] = 0x34
        source[0x1401] = 0x12
        source[0x1402] = 0x78
        source[0x1403] = 0x56
        source[0x1435] = 5
        val original = source.copyOf()
        val fixture = Gen7UsUmSave.checksummedFixture(source)
        assertArrayEquals(original, source)
        val save = (Gen7UsUmSave.parse(fixture) as Gen7UsUmSave.Parsed.Ok).save
        assertEquals(0x1234, save.trainerId)
        assertEquals(0x5678, save.secretId)
        assertEquals(5, save.language)
        assertTrue(save.partyRecord(0) is Gen6PokemonRecord.Parsed.Ok)
        assertTrue(save.boxRecord(31, 29) is Gen6PokemonRecord.Parsed.Ok)
        assertArrayEquals(fixture, save.data)
    }
}
