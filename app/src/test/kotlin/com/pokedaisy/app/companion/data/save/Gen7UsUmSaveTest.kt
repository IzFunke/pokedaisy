package com.pokedaisy.app.companion.data.save

import org.junit.Assert.*
import org.junit.Test

class Gen7UsUmSaveTest {
    @Test fun acceptsOnlyExactSizeFooterAndAllThirtyNineBlockChecksums() {
        val fixture = Gen7UsUmSave.checksummedFixture(ByteArray(Gen7UsUmSave.SIZE))
        // PKHeX SaveUtil.HasSaveFooterBEEF reads a little-endian UInt32 and compares it to 0x42454546.
        // Therefore the serialized marker bytes are 46 45 45 42 (not the ASCII byte order 42 45 45 46).
        val footer = Gen7UsUmSave.SIZE - 0x1F0
        assertArrayEquals(byteArrayOf(0x46, 0x45, 0x45, 0x42), fixture.copyOfRange(footer, footer + 4))
        assertTrue(Gen7UsUmSave.parse(fixture) is Gen7UsUmSave.Parsed.Ok)
        assertEquals(Gen7UsUmSave.Failure.SIZE, failure(fixture.copyOf(fixture.size - 1)))
        val corrupt = fixture.copyOf().also { it[0x1400] = 1 }
        assertEquals(Gen7UsUmSave.Failure.BLOCK_CHECKSUMS, failure(corrupt))
        val badFooter = fixture.copyOf().also { it[Gen7UsUmSave.SIZE - 0x1F0] = 0 }
        assertEquals(Gen7UsUmSave.Failure.FOOTER, failure(badFooter))
        val asciiOrderFooter = fixture.copyOf().also { byteArrayOf(0x42, 0x45, 0x45, 0x46).copyInto(it, footer) }
        assertEquals(Gen7UsUmSave.Failure.FOOTER, failure(asciiOrderFooter))
        val invalidParty = fixture.copyOf().also { it[0x1C18] = 7 }
        assertEquals(Gen7UsUmSave.Failure.PARTY_COUNT, failure(Gen7UsUmSave.checksummedFixture(invalidParty)))
    }

    private fun failure(bytes: ByteArray): Gen7UsUmSave.Failure =
        (Gen7UsUmSave.parse(bytes) as Gen7UsUmSave.Parsed.Failed).reason

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
