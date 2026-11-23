package com.pokedaisy.app.companion.data.save

/**
 * Validates and rechecksums a decrypted Pokémon X/Y `main` save.
 *
 * The 55 block ranges and footer checksum layout follow PKHeX's GPL-3.0-licensed
 * `SaveBlockAccessor6XY` / `BlockInfo3DS` implementation. Keep this limited to XY:
 * ORAS uses a different layout, and Gen 7 uses a different checksum and (for USUM)
 * an additional block transform.
 */
class Gen6XySave private constructor(private val original: ByteArray) {
    val data: ByteArray get() = original.copyOf()
    val partyCount: Int get() = readU32(original, PARTY_COUNT_OFFSET).toInt()

    /** A Pokémon's language is the best available hint when the trainer block language isn't needed by the editor. */
    fun languageHint(): Int? {
        fun language(parsed: Gen6PokemonRecord.Parsed): Int? =
            (parsed as? Gen6PokemonRecord.Parsed.Ok)?.record?.takeUnless { it.isEmpty }?.data
                ?.getOrNull(0xE3)?.toInt()?.and(0xFF)?.takeIf { it in setOf(1, 2, 3, 4, 5, 7, 8) }
        for (i in 0 until partyCount.coerceIn(0, PARTY_SIZE)) language(partyRecord(i))?.let { return it }
        for (box in 0 until BOX_COUNT) for (slot in 0 until BOX_SLOTS) language(boxRecord(box, slot))?.let { return it }
        return null
    }

    /** The encrypted PK6 in one PC slot. Indices are XY's 31 boxes of 30 slots. */
    fun boxRecord(box: Int, slot: Int): Gen6PokemonRecord.Parsed {
        require(box in 0 until BOX_COUNT && slot in 0 until BOX_SLOTS)
        val offset = PC_OFFSET + (box * BOX_SLOTS + slot) * Gen6PokemonRecord.STORED_SIZE
        return Gen6PokemonRecord.parse(original.copyOfRange(offset, offset + Gen6PokemonRecord.STORED_SIZE), party = false)
    }

    /** The encrypted PK6 in one party slot. */
    fun partyRecord(slot: Int): Gen6PokemonRecord.Parsed {
        require(slot in 0 until PARTY_SIZE)
        val offset = PARTY_OFFSET + slot * Gen6PokemonRecord.PARTY_SIZE
        return Gen6PokemonRecord.parse(original.copyOfRange(offset, offset + Gen6PokemonRecord.PARTY_SIZE), party = true)
    }

    /** Returns a checksummed save with one PC record replaced by a validated encrypted PK6. */
    fun withBoxRecord(box: Int, slot: Int, encrypted: ByteArray): Parsed =
        withRecord(PC_OFFSET + (checkedPcIndex(box, slot)) * Gen6PokemonRecord.STORED_SIZE, encrypted, party = false)

    /** Returns a checksummed save with one party record replaced by a validated encrypted PK6. */
    fun withPartyRecord(slot: Int, encrypted: ByteArray): Parsed {
        require(slot in 0 until PARTY_SIZE)
        val record = when (val parsed = Gen6PokemonRecord.parse(encrypted, party = true)) {
            is Gen6PokemonRecord.Parsed.Failed -> return Parsed.Failed(parsed.reason)
            is Gen6PokemonRecord.Parsed.Ok -> parsed.record
        }
        val countOffset = PARTY_COUNT_OFFSET
        val currentCount = partyCount
        if (currentCount !in 0..PARTY_SIZE) return Parsed.Failed("invalid XY party count")
        if (slot > currentCount || (slot == currentCount && record.isEmpty)) {
            return if (record.isEmpty) Parsed.Ok(this) else Parsed.Failed("party slots must stay contiguous")
        }

        val updated = original.copyOf()
        val slotOffset = PARTY_OFFSET + slot * Gen6PokemonRecord.PARTY_SIZE
        if (slot < currentCount && record.isEmpty) {
            // Removing a party member shifts the remaining records left; the game reads [count] contiguous entries.
            for (i in slot until currentCount - 1) {
                val from = PARTY_OFFSET + (i + 1) * Gen6PokemonRecord.PARTY_SIZE
                updated.copyInto(updated, PARTY_OFFSET + i * Gen6PokemonRecord.PARTY_SIZE, from, from + Gen6PokemonRecord.PARTY_SIZE)
            }
            updated.fill(0, PARTY_OFFSET + (currentCount - 1) * Gen6PokemonRecord.PARTY_SIZE,
                PARTY_OFFSET + currentCount * Gen6PokemonRecord.PARTY_SIZE)
            writeU32(updated, countOffset, (currentCount - 1).toLong())
        } else {
            record.toEncrypted().copyInto(updated, slotOffset)
            if (slot == currentCount) writeU32(updated, countOffset, (currentCount + 1).toLong())
        }
        return parse(withData(updated))
    }

    private fun withRecord(offset: Int, encrypted: ByteArray, party: Boolean): Parsed {
        val record = when (val parsed = Gen6PokemonRecord.parse(encrypted, party)) {
            is Gen6PokemonRecord.Parsed.Failed -> return Parsed.Failed(parsed.reason)
            is Gen6PokemonRecord.Parsed.Ok -> parsed.record
        }
        val updated = original.copyOf()
        record.toEncrypted().copyInto(updated, offset)
        return parse(withData(updated))
    }

    /** Replaces the decrypted save data and recomputes the block checksums. */
    fun withData(updated: ByteArray): ByteArray {
        require(updated.size == SIZE)
        val out = updated.copyOf()
        for (block in BLOCKS) {
            val checksum = crc16Ccitt(out, block.offset, block.length)
            putU16(out, METADATA + 0x14 + block.id * 8 + 6, checksum)
        }
        return out
    }

    sealed class Parsed {
        class Ok(val save: Gen6XySave) : Parsed()
        class Failed(val reason: String) : Parsed()
    }

    companion object {
        const val SIZE = 0x65600
        const val BOX_COUNT = 31
        const val BOX_SLOTS = 30
        const val PARTY_SIZE = 6
        private const val PC_OFFSET = 0x22600
        private const val PARTY_OFFSET = 0x14200
        private const val PARTY_COUNT_OFFSET = PARTY_OFFSET + PARTY_SIZE * Gen6PokemonRecord.PARTY_SIZE
        private const val METADATA = SIZE - 0x200
        private const val FOOTER_MAGIC = 0x42454546L // ASCII BEEF
        private data class Block(val id: Int, val offset: Int, val length: Int)

        private fun checkedPcIndex(box: Int, slot: Int): Int {
            require(box in 0 until BOX_COUNT && slot in 0 until BOX_SLOTS)
            return box * BOX_SLOTS + slot
        }

        private fun readU32(bytes: ByteArray, offset: Int): Long =
            (bytes[offset].toLong() and 0xFF) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 3].toLong() and 0xFF) shl 24)

        private fun writeU32(bytes: ByteArray, offset: Int, value: Long) {
            for (i in 0..3) bytes[offset + i] = (value ushr (8 * i)).toByte()
        }

        // XY block IDs and ranges from PKHeX SaveBlockAccessor6XY.
        private val BLOCKS = listOf(
            Block(0,0x00000,0x002C8), Block(1,0x00400,0x00B88), Block(2,0x01000,0x0002C),
            Block(3,0x01200,0x00038), Block(4,0x01400,0x00150), Block(5,0x01600,0x00004),
            Block(6,0x01800,0x00008), Block(7,0x01A00,0x001C0), Block(8,0x01C00,0x000BE),
            Block(9,0x01E00,0x00024), Block(10,0x02000,0x02100), Block(11,0x04200,0x00140),
            Block(12,0x04400,0x00440), Block(13,0x04A00,0x00574), Block(14,0x05000,0x04E28),
            Block(15,0x0A000,0x04E28), Block(16,0x0F000,0x04E28), Block(17,0x14000,0x00170),
            Block(18,0x14200,0x0061C), Block(19,0x14A00,0x00504), Block(20,0x15000,0x006A0),
            Block(21,0x15800,0x00644), Block(22,0x16000,0x00104), Block(23,0x16200,0x00004),
            Block(24,0x16400,0x00420), Block(25,0x16A00,0x00064), Block(26,0x16C00,0x003F0),
            Block(27,0x17000,0x0070C), Block(28,0x17800,0x00180), Block(29,0x17A00,0x00004),
            Block(30,0x17C00,0x0000C), Block(31,0x17E00,0x00048), Block(32,0x18000,0x00054),
            Block(33,0x18200,0x00644), Block(34,0x18A00,0x005C8), Block(35,0x19000,0x002F8),
            Block(36,0x19400,0x01B40), Block(37,0x1B000,0x001F4), Block(38,0x1B200,0x001F0),
            Block(39,0x1B400,0x00216), Block(40,0x1B800,0x00390), Block(41,0x1BC00,0x01A90),
            Block(42,0x1D800,0x00308), Block(43,0x1DC00,0x00618), Block(44,0x1E400,0x0025C),
            Block(45,0x1E800,0x00834), Block(46,0x1F200,0x00318), Block(47,0x1F600,0x007D0),
            Block(48,0x1FE00,0x00C48), Block(49,0x20C00,0x00078), Block(50,0x20E00,0x00200),
            Block(51,0x21000,0x00C84), Block(52,0x21E00,0x00628), Block(53,0x22600,0x34AD0),
            Block(54,0x57200,0x0E058),
        )

        fun parse(bytes: ByteArray): Parsed {
            if (bytes.size != SIZE) return Parsed.Failed("not a Pokémon X/Y save (${bytes.size} bytes)")
            if (u32(bytes, METADATA + 0x10) != FOOTER_MAGIC) return Parsed.Failed("its XY save footer is missing")
            if (BLOCKS.any { block ->
                    u16(bytes, METADATA + 0x14 + block.id * 8 + 6) != crc16Ccitt(bytes, block.offset, block.length)
                }) return Parsed.Failed("one or more XY save block checksums don't match")
            return Parsed.Ok(Gen6XySave(bytes.copyOf()))
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

        private fun u16(bytes: ByteArray, offset: Int) =
            (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)

        private fun u32(bytes: ByteArray, offset: Int) =
            (u16(bytes, offset).toLong() or (u16(bytes, offset + 2).toLong() shl 16)) and 0xFFFF_FFFFL

        private fun putU16(bytes: ByteArray, offset: Int, value: Int) {
            bytes[offset] = value.toByte()
            bytes[offset + 1] = (value ushr 8).toByte()
        }
    }
}
