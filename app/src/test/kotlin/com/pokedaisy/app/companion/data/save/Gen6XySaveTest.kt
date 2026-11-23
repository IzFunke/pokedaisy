package com.pokedaisy.app.companion.data.save

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class Gen6XySaveTest {
    @Test fun validatesIsolatedYSaveCopyWhenProvided() {
        val path = System.getProperty("pokedaisy.xyFixture") ?: return
        val copy = File(path).readBytes()
        assertTrue("isolated Y save copy must pass XY block checks", Gen6XySave.parse(copy) is Gen6XySave.Parsed.Ok)
    }

    @Test fun pk6CodecRoundTripsEveryRecordInIsolatedYSaveCopyWhenProvided() {
        val path = System.getProperty("pokedaisy.xyFixture") ?: return
        val bytes = File(path).readBytes()
        assertTrue(Gen6XySave.parse(bytes) is Gen6XySave.Parsed.Ok)
        val offsets = buildList {
            repeat(31 * 30) { add(Triple(0x22600 + it * Gen6PokemonRecord.STORED_SIZE, false, "box[$it]")) }
            repeat(6) { add(Triple(0x14200 + it * Gen6PokemonRecord.PARTY_SIZE, true, "party[$it]")) }
        }
        for ((offset, party, label) in offsets) {
            val length = if (party) Gen6PokemonRecord.PARTY_SIZE else Gen6PokemonRecord.STORED_SIZE
            val raw = bytes.copyOfRange(offset, offset + length)
            when (val parsed = Gen6PokemonRecord.parse(raw, party)) {
                is Gen6PokemonRecord.Parsed.Failed -> throw AssertionError("$label: ${parsed.reason}")
                is Gen6PokemonRecord.Parsed.Ok -> assertArrayEquals("$label re-encryption", raw, parsed.record.toEncrypted())
            }
        }
    }

    @Test fun pk6EditCanBeWrittenBackToAnIsolatedYSaveAndReopenedWhenProvided() {
        val path = System.getProperty("pokedaisy.xyFixture") ?: return
        val source = File(path).readBytes()
        val save = (Gen6XySave.parse(source) as? Gen6XySave.Parsed.Ok)?.save
            ?: throw AssertionError("isolated Y save did not parse")
        val before = (save.boxRecord(0, 0) as? Gen6PokemonRecord.Parsed.Ok)?.record
            ?: throw AssertionError("first XY box record did not parse")
        if (before.isEmpty) return

        val changedRecord = before.withData(before.data.also { putU16(it, 0x0A, 1) }).toEncrypted()
        val changedSave = when (val result = save.withBoxRecord(0, 0, changedRecord)) {
            is Gen6XySave.Parsed.Failed -> throw AssertionError("edited XY save rejected: ${result.reason}")
            is Gen6XySave.Parsed.Ok -> result.save
        }
        val reopened = (Gen6XySave.parse(changedSave.data) as? Gen6XySave.Parsed.Ok)?.save
            ?: throw AssertionError("edited XY save did not reopen")
        val after = (reopened.boxRecord(0, 0) as? Gen6PokemonRecord.Parsed.Ok)?.record
            ?: throw AssertionError("edited PK6 did not reopen")
        assertEquals(1, after.heldItem)
        assertEquals(before.species, after.species)
        assertArrayEquals("the fixture passed to the editor remains unchanged", source, File(path).readBytes())
    }

    @Test fun xySaveOffersValidatedBoxAndPartySlotAccess() {
        val save = (Gen6XySave.parse(validBlankXY()) as Gen6XySave.Parsed.Ok).save
        assertTrue(save.boxRecord(30, 29) is Gen6PokemonRecord.Parsed.Ok)
        assertTrue(save.partyRecord(5) is Gen6PokemonRecord.Parsed.Ok)
        assertTrue(save.withBoxRecord(0, 0, ByteArray(Gen6PokemonRecord.STORED_SIZE)) is Gen6XySave.Parsed.Ok)
        assertTrue(save.withPartyRecord(0, ByteArray(Gen6PokemonRecord.PARTY_SIZE)) is Gen6XySave.Parsed.Ok)
        assertTrue(save.withPartyRecord(0, ByteArray(Gen6PokemonRecord.STORED_SIZE)) is Gen6XySave.Parsed.Failed)
    }

    @Test fun xyPartySlotInsertAndRemovalKeepCountAndChecksumsValid() {
        val source = validBlankXY()
        val save = (Gen6XySave.parse(source) as Gen6XySave.Parsed.Ok).save
        val plain = ByteArray(Gen6PokemonRecord.PARTY_SIZE).also {
            putU32(it, 0, 0x12345678L)
            putU16(it, 8, 25)
            putU16(it, 0x0A, 1)
        }
        val encrypted = (Gen6PokemonRecord.create(plain, party = true) as Gen6PokemonRecord.Parsed.Ok).record.toEncrypted()
        val added = (save.withPartyRecord(0, encrypted) as Gen6XySave.Parsed.Ok).save
        assertEquals(1, added.partyCount)
        assertEquals(25, (added.partyRecord(0) as Gen6PokemonRecord.Parsed.Ok).record.species)

        val removed = (added.withPartyRecord(0, ByteArray(Gen6PokemonRecord.PARTY_SIZE)) as Gen6XySave.Parsed.Ok).save
        assertEquals(0, removed.partyCount)
        assertTrue(Gen6XySave.parse(removed.data) is Gen6XySave.Parsed.Ok)
        assertArrayEquals("synthetic source remains unchanged", validBlankXY(), source)
    }

    @Test fun acceptsXYBlockFooterAndRechecksumsModifiedCopy() {
        val fixture = validBlankXY()
        val parsed = Gen6XySave.parse(fixture) as Gen6XySave.Parsed.Ok

        val changed = parsed.save.data
        changed[0x14200] = 0x35
        val written = parsed.save.withData(changed)

        assertTrue(Gen6XySave.parse(written) is Gen6XySave.Parsed.Ok)
        assertFalse(fixture.contentEquals(written))
        assertArrayEquals(fixture, parsed.save.data)
    }

    @Test fun rejectsWrongSizeFooterAndBlockChecksum() {
        val fixture = validBlankXY()
        assertTrue(Gen6XySave.parse(fixture.copyOf(fixture.size - 1)) is Gen6XySave.Parsed.Failed)

        val badFooter = fixture.copyOf()
        badFooter[Gen6XySave.SIZE - 0x1F0] = 0
        assertTrue(Gen6XySave.parse(badFooter) is Gen6XySave.Parsed.Failed)

        val badBlock = fixture.copyOf()
        badBlock[0x14200] = 1
        assertTrue(Gen6XySave.parse(badBlock) is Gen6XySave.Parsed.Failed)
    }

    /** Synthetic decrypted XY-shaped fixture; tests operate on copies and never touch a user's save. */
    private fun validBlankXY(): ByteArray {
        val bytes = ByteArray(Gen6XySave.SIZE)
        val meta = Gen6XySave.SIZE - 0x200
        bytes[meta + 0x10] = 0x46 // little-endian 0x42454546, ASCII BEEF
        bytes[meta + 0x11] = 0x45
        bytes[meta + 0x12] = 0x45
        bytes[meta + 0x13] = 0x42
        val blocks = listOf(
            0x00000 to 0x002C8, 0x00400 to 0x00B88, 0x01000 to 0x0002C, 0x01200 to 0x00038,
            0x01400 to 0x00150, 0x01600 to 0x00004, 0x01800 to 0x00008, 0x01A00 to 0x001C0,
            0x01C00 to 0x000BE, 0x01E00 to 0x00024, 0x02000 to 0x02100, 0x04200 to 0x00140,
            0x04400 to 0x00440, 0x04A00 to 0x00574, 0x05000 to 0x04E28, 0x0A000 to 0x04E28,
            0x0F000 to 0x04E28, 0x14000 to 0x00170, 0x14200 to 0x0061C, 0x14A00 to 0x00504,
            0x15000 to 0x006A0, 0x15800 to 0x00644, 0x16000 to 0x00104, 0x16200 to 0x00004,
            0x16400 to 0x00420, 0x16A00 to 0x00064, 0x16C00 to 0x003F0, 0x17000 to 0x0070C,
            0x17800 to 0x00180, 0x17A00 to 0x00004, 0x17C00 to 0x0000C, 0x17E00 to 0x00048,
            0x18000 to 0x00054, 0x18200 to 0x00644, 0x18A00 to 0x005C8, 0x19000 to 0x002F8,
            0x19400 to 0x01B40, 0x1B000 to 0x001F4, 0x1B200 to 0x001F0, 0x1B400 to 0x00216,
            0x1B800 to 0x00390, 0x1BC00 to 0x01A90, 0x1D800 to 0x00308, 0x1DC00 to 0x00618,
            0x1E400 to 0x0025C, 0x1E800 to 0x00834, 0x1F200 to 0x00318, 0x1F600 to 0x007D0,
            0x1FE00 to 0x00C48, 0x20C00 to 0x00078, 0x20E00 to 0x00200, 0x21000 to 0x00C84,
            0x21E00 to 0x00628, 0x22600 to 0x34AD0, 0x57200 to 0x0E058,
        )
        blocks.forEachIndexed { id, (offset, length) ->
            val crc = crc16Ccitt(bytes, offset, length)
            val checksumOffset = meta + 0x14 + id * 8 + 6
            bytes[checksumOffset] = crc.toByte()
            bytes[checksumOffset + 1] = (crc ushr 8).toByte()
        }
        return bytes
    }

    private fun crc16Ccitt(bytes: ByteArray, offset: Int, length: Int): Int {
        var top = 0xFF
        var bottom = 0xFF
        for (i in offset until offset + length) {
            var x = (bytes[i].toInt() and 0xFF) xor top
            x = x xor (x ushr 4)
            top = (bottom xor (x ushr 3) xor (x shl 4)) and 0xFF
            bottom = (x xor (x shl 5)) and 0xFF
        }
        return (top shl 8) or bottom
    }

    private fun putU16(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = value.toByte()
        bytes[offset + 1] = (value ushr 8).toByte()
    }

    private fun putU32(bytes: ByteArray, offset: Int, value: Long) {
        for (i in 0..3) bytes[offset + i] = (value ushr (8 * i)).toByte()
    }
}
