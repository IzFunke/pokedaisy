package com.pokedaisy.app.n3ds

import com.pokedaisy.app.companion.data.save.BoxSlot
import com.pokedaisy.app.companion.data.save.Gen6PokemonRecord
import com.pokedaisy.app.companion.data.save.Gen6XySave
import com.pokedaisy.app.companion.data.save.MonRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gen6XySaveEditorTest {
    @Test fun sharedEditorSwapsAndReleasesPcPokemonWithoutChangingTheSource() {
        val source = blankXySave()
        val original = (Gen6XySave.parse(source) as Gen6XySave.Parsed.Ok).save
        val occupied = original.withBoxRecord(0, 0, pokemon(25)) as Gen6XySave.Parsed.Ok
        val editor = Gen6XySaveEditor(occupied.save, gameData())

        assertFalse(editor.canEditStats)
        assertFalse(editor.canEditMoves)
        assertFalse(editor.canEditTraits)
        assertFalse(editor.canMoveFrom(MonRef.Party(0)))
        assertTrue(editor.canMoveFrom(MonRef.Pc(BoxSlot(0, 0))))

        editor.move(MonRef.Pc(BoxSlot(0, 0)), MonRef.Pc(BoxSlot(0, 1)))
        assertTrue(editor.data.boxMons(0)[0].isEmpty)
        assertEquals(25, editor.data.mon(MonRef.Pc(BoxSlot(0, 1)))!!.first.species)
        assertFalse(editor.build().contentEquals(occupied.save.data))

        editor.release(BoxSlot(0, 1))
        assertTrue(editor.data.mon(MonRef.Pc(BoxSlot(0, 1))) == null)
        assertTrue(Gen6XySave.parse(editor.build()) is Gen6XySave.Parsed.Ok)
        assertTrue(Gen6XySave.parse(source) is Gen6XySave.Parsed.Ok)
        assertTrue(editor.build().contentEquals(source))
    }

    private fun gameData(): Gen6XyGameData {
        val names = MutableList(800) { "" }
        names[25] = "Pikachu"
        val personal = List(800) { ByteArray(0x40) }.map { it.copyOf() }
        val rows = personal.toMutableList()
        rows[25] = ByteArray(0x40).also {
            it[0] = 35; it[1] = 55; it[2] = 40; it[3] = 90; it[4] = 50; it[5] = 50
            it[0x12] = 127; it[0x15] = 0; it[0x18] = 9; it[0x19] = 9
        }
        return Gen6XyGameData(names, listOf("", "Tackle"), listOf("", "Potion"), listOf("", "Static"), rows)
    }

    private fun pokemon(species: Int): ByteArray {
        val plain = ByteArray(Gen6PokemonRecord.STORED_SIZE)
        putU32(plain, 0, 0x12345678L)
        putU16(plain, 8, species)
        putU32(plain, 0x10, 125_000L)
        putU16(plain, 0x0C, 1234)
        putU16(plain, 0x0E, 5678)
        plain[0x14] = 9
        plain[0x15] = 0
        plain[0x1C] = 3
        plain[0x1D] = 0
        plain[0xE3] = 2
        return (Gen6PokemonRecord.create(plain, party = false) as Gen6PokemonRecord.Parsed.Ok).record.toEncrypted()
    }

    private fun blankXySave(): ByteArray {
        val bytes = ByteArray(Gen6XySave.SIZE)
        val meta = bytes.size - 0x200
        bytes[meta + 0x10] = 0x46
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
            putU16(bytes, meta + 0x14 + id * 8 + 6, crc)
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
