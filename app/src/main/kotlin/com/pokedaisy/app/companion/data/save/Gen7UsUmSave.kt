package com.pokedaisy.app.companion.data.save

/**
 * Validates a decrypted Ultra Sun/Ultra Moon `main` save and exposes its PK7 party and PC records.
 * Layout and block CRCs follow PKHeX.Core `SaveBlockAccessor7USUM` / `BlockInfo7` (GPL-3.0).
 * Editing is deliberately not exposed: USUM also signs the checksum table with MemeCrypto RSA,
 * and this app has no supported signing key. Rewriting checksums alone would produce an untrusted save.
 */
class Gen7UsUmSave private constructor(private val original: ByteArray) {
    val data: ByteArray get() = original.copyOf()
    val partyCount: Int get() = readU32(original, PARTY_COUNT_OFFSET).toInt()
    val trainerId: Int get() = u16(original, STATUS_OFFSET)
    val secretId: Int get() = u16(original, STATUS_OFFSET + 2)
    val trainerGender: Int get() = original[STATUS_OFFSET + 5].toInt() and 0xFF
    val language: Int get() = original[STATUS_OFFSET + 0x35].toInt() and 0xFF
    val trainerName: String get() = readString(original, STATUS_OFFSET + 0x38, 0x1A)

    fun boxRecord(box: Int, slot: Int): Gen6PokemonRecord.Parsed {
        require(box in 0 until BOX_COUNT && slot in 0 until BOX_SLOTS)
        val offset = PC_OFFSET + (box * BOX_SLOTS + slot) * Gen6PokemonRecord.STORED_SIZE
        return Gen6PokemonRecord.parse(original.copyOfRange(offset, offset + Gen6PokemonRecord.STORED_SIZE), party = false)
    }

    fun partyRecord(slot: Int): Gen6PokemonRecord.Parsed {
        require(slot in 0 until PARTY_SIZE)
        val offset = PARTY_OFFSET + slot * Gen6PokemonRecord.PARTY_SIZE
        return Gen6PokemonRecord.parse(original.copyOfRange(offset, offset + Gen6PokemonRecord.PARTY_SIZE), party = true)
    }

    sealed class Parsed {
        class Ok(val save: Gen7UsUmSave) : Parsed()
        class Failed(val reason: String) : Parsed()
    }

    companion object {
        const val SIZE = 0x6CC00
        const val BOX_COUNT = 32
        const val BOX_SLOTS = 30
        const val PARTY_SIZE = 6
        private const val PARTY_OFFSET = 0x01600
        private const val PARTY_COUNT_OFFSET = PARTY_OFFSET + PARTY_SIZE * Gen6PokemonRecord.PARTY_SIZE
        private const val STATUS_OFFSET = 0x01400
        private const val PC_OFFSET = 0x05200
        private const val METADATA = SIZE - 0x200
        private const val FOOTER_MAGIC = 0x42454546L
        private data class Block(val id: Int, val offset: Int, val length: Int)

        // Exact IDs, ranges, and lengths from PKHeX.Core SaveBlockAccessor7USUM.
        private val BLOCKS = listOf(
            Block(0,0x00000,0x00E28), Block(1,0x01000,0x0007C), Block(2,0x01200,0x00014),
            Block(3,0x01400,0x000C0), Block(4,0x01600,0x0061C), Block(5,0x01E00,0x00E00),
            Block(6,0x02C00,0x00F78), Block(7,0x03C00,0x00228), Block(8,0x04000,0x0030C),
            Block(9,0x04400,0x001FC), Block(10,0x04600,0x0004C), Block(11,0x04800,0x00004),
            Block(12,0x04A00,0x00058), Block(13,0x04C00,0x005E6), Block(14,0x05200,0x36600),
            Block(15,0x3B800,0x0572C), Block(16,0x41000,0x00008), Block(17,0x41200,0x01218),
            Block(18,0x42600,0x01A08), Block(19,0x44200,0x06408), Block(20,0x4A800,0x06408),
            Block(21,0x50E00,0x03998), Block(22,0x54800,0x00100), Block(23,0x54A00,0x00100),
            Block(24,0x54C00,0x10528), Block(25,0x65200,0x00204), Block(26,0x65600,0x00B60),
            Block(27,0x66200,0x03F50), Block(28,0x6A200,0x00358), Block(29,0x6A600,0x00728),
            Block(30,0x6AE00,0x00200), Block(31,0x6B000,0x00718), Block(32,0x6B800,0x001FC),
            Block(33,0x6BA00,0x00200), Block(34,0x6BC00,0x00120), Block(35,0x6BE00,0x001C8),
            Block(36,0x6C000,0x00200), Block(37,0x6C200,0x0039C), Block(38,0x6C600,0x00400),
        )

        fun parse(bytes: ByteArray): Parsed {
            if (bytes.size != SIZE) return Parsed.Failed("not an Ultra Sun/Ultra Moon save (${bytes.size} bytes)")
            if (u32(bytes, METADATA + 0x10) != FOOTER_MAGIC) return Parsed.Failed("its USUM save footer is missing")
            if (BLOCKS.any { block -> u16(bytes, METADATA + 0x14 + block.id * 8 + 6) != crc16Invert(bytes, block.offset, block.length) }) {
                return Parsed.Failed("one or more USUM save block checksums don't match")
            }
            val count = readU32(bytes, PARTY_COUNT_OFFSET)
            if (count !in 0..PARTY_SIZE.toLong()) return Parsed.Failed("invalid USUM party count")
            return Parsed.Ok(Gen7UsUmSave(bytes.copyOf()))
        }

        /** Synthetic fixture helper; never signs or alters the signature block. */
        internal fun checksummedFixture(bytes: ByteArray): ByteArray {
            require(bytes.size == SIZE)
            val out = bytes.copyOf()
            putU32(out, METADATA + 0x10, FOOTER_MAGIC)
            BLOCKS.forEach { putU16(out, METADATA + 0x14 + it.id * 8 + 6, crc16Invert(out, it.offset, it.length)) }
            return out
        }

        private fun crc16Invert(bytes: ByteArray, offset: Int, length: Int): Int {
            var crc = 0xFFFF
            for (i in offset until offset + length) {
                crc = (crc ushr 8) xor CRC_TABLE[(crc xor (bytes[i].toInt() and 0xFF)) and 0xFF]
            }
            return crc xor 0xFFFF
        }

        private val CRC_TABLE = IntArray(256) { value ->
            var crc = value
            repeat(8) { crc = if ((crc and 1) != 0) (crc ushr 1) xor 0xA001 else crc ushr 1 }
            crc
        }

        private fun readString(bytes: ByteArray, offset: Int, length: Int): String {
            val text = String(bytes, offset, length, Charsets.UTF_16LE)
            val end = listOf(text.indexOf('\u0000'), text.indexOf('\uFFFF')).filter { it >= 0 }.minOrNull() ?: text.length
            return text.substring(0, end)
        }
        private fun u16(bytes: ByteArray, offset: Int) = (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
        private fun u32(bytes: ByteArray, offset: Int) = (u16(bytes, offset).toLong() or (u16(bytes, offset + 2).toLong() shl 16)) and 0xFFFF_FFFFL
        private fun readU32(bytes: ByteArray, offset: Int) = u32(bytes, offset)
        private fun putU16(bytes: ByteArray, offset: Int, value: Int) { bytes[offset] = value.toByte(); bytes[offset + 1] = (value ushr 8).toByte() }
        private fun putU32(bytes: ByteArray, offset: Int, value: Long) { repeat(4) { bytes[offset + it] = (value ushr (8 * it)).toByte() } }
    }
}
