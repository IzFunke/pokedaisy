package com.pokedaisy.app.companion.data.save

/**
 * Pokémon X/Y PK6 record codec (0xE8 stored, 0x104 party).
 *
 * The LCRNG stream, 4-block shuffle and shuffle tables are adapted from
 * PKHeX.Core's `PokeCrypto` (GPL-3.0, copyright retained by its authors).
 * This project's GPL-3.0 license is compatible. Keep this codec scoped to
 * Gen 6 record encoding; game legality and field semantics belong elsewhere.
 */
class Gen6PokemonRecord private constructor(
    private val decrypted: ByteArray,
    private val party: Boolean,
    private val originalEncrypted: ByteArray,
    private val dirty: Boolean = false,
) {
    val data: ByteArray get() = decrypted.copyOf()
    val species: Int get() = u16(decrypted, 8)
    val heldItem: Int get() = u16(decrypted, 0x0A)
    val isEmpty: Boolean get() = species == 0

    fun withData(updated: ByteArray): Gen6PokemonRecord {
        require(updated.size == decrypted.size)
        val copy = updated.copyOf()
        if (u16(copy, 8) != 0) putU16(copy, CHECKSUM_OFFSET, checksum(copy))
        return Gen6PokemonRecord(copy, party, originalEncrypted, dirty = true)
    }

    /** Returns the encrypted record in the game's exact stored or party size. */
    fun toEncrypted(): ByteArray {
        // Preserve untouched records byte-for-byte. Empty Gen 6 slots can contain encrypted
        // residue that decrypts to species 0, so normalizing them would alter a save needlessly.
        if (!dirty) return originalEncrypted.copyOf()
        val out = decrypted.copyOf()
        if (isEmpty && out.all { it == 0.toByte() }) return out
        if (u16(out, 8) != 0) putU16(out, CHECKSUM_OFFSET, checksum(out))
        val pv = u32(out, 0)
        val sv = ((pv ushr 13) and 31).toInt()
        val inverse = if (sv < INVERSE.size) INVERSE[sv] else return error("invalid PK6 shuffle value")
        shuffle(out, inverse)
        crypt(out, 8, STORED_SIZE, pv)
        if (party) crypt(out, STORED_SIZE, PARTY_SIZE, pv)
        return out
    }

    sealed class Parsed {
        class Ok(val record: Gen6PokemonRecord) : Parsed()
        class Failed(val reason: String) : Parsed()
    }

    companion object {
        const val STORED_SIZE = 0xE8
        const val PARTY_SIZE = 0x104
        private const val CHECKSUM_OFFSET = 6
        private const val BLOCK_SIZE = 56
        private val POSITION = intArrayOf(
            0,1,2,3, 0,1,3,2, 0,2,1,3, 0,3,1,2, 0,2,3,1, 0,3,2,1,
            1,0,2,3, 1,0,3,2, 2,0,1,3, 3,0,1,2, 2,0,3,1, 3,0,2,1,
            1,2,0,3, 1,3,0,2, 2,1,0,3, 3,1,0,2, 2,3,0,1, 3,2,0,1,
            1,2,3,0, 1,3,2,0, 2,1,3,0, 3,1,2,0, 2,3,1,0, 3,2,1,0,
            0,1,2,3, 0,1,3,2, 0,2,1,3, 0,3,1,2, 0,2,3,1, 0,3,2,1,
            1,0,2,3, 1,0,3,2,
        )
        private val INVERSE = intArrayOf(
            0,1,2,4, 3,5,6,7, 12,18,13,19, 8,10,14,20, 16,22,9,11, 15,21,17,23,
            0,1,2,4, 3,5,6,7,
        )

        fun parse(encrypted: ByteArray, party: Boolean): Parsed {
            val expected = if (party) PARTY_SIZE else STORED_SIZE
            if (encrypted.size != expected) return Parsed.Failed("invalid PK6 record size")
            if (encrypted.all { it == 0.toByte() }) return Parsed.Ok(Gen6PokemonRecord(encrypted.copyOf(), party, encrypted.copyOf()))
            val out = encrypted.copyOf()
            if (u16(out, 0xC8) != 0 || u16(out, 0x58) != 0) decrypt(out, party)
            if (u16(out, 8) == 0) return Parsed.Ok(Gen6PokemonRecord(out, party, encrypted.copyOf()))
            if (u16(out, CHECKSUM_OFFSET) != checksum(out)) return Parsed.Failed("PK6 checksum doesn't match")
            return Parsed.Ok(Gen6PokemonRecord(out, party, encrypted.copyOf()))
        }

        /** Builds an encrypted PK6 from editable decrypted data and immediately validates the encoded result. */
        fun create(decrypted: ByteArray, party: Boolean): Parsed {
            val expected = if (party) PARTY_SIZE else STORED_SIZE
            if (decrypted.size != expected) return Parsed.Failed("invalid PK6 record size")
            val draft = Gen6PokemonRecord(decrypted.copyOf(), party, ByteArray(expected), dirty = true)
            return parse(draft.toEncrypted(), party)
        }

        private fun decrypt(out: ByteArray, party: Boolean) {
            val pv = u32(out, 0)
            crypt(out, 8, STORED_SIZE, pv)
            if (party) crypt(out, STORED_SIZE, PARTY_SIZE, pv)
            val sv = ((pv ushr 13) and 31).toInt()
            shuffle(out, sv)
        }

        private fun crypt(out: ByteArray, from: Int, until: Int, initialSeed: Long) {
            var seed = initialSeed
            var offset = from
            while (offset < until) {
                seed = (0x41C64E6DL * seed + 0x6073L) and 0xFFFF_FFFFL
                val word = u16(out, offset) xor ((seed ushr 16).toInt() and 0xFFFF)
                putU16(out, offset, word)
                offset += 2
            }
        }

        /** Applies the PKHeX permutation procedure using the 24-entry shuffle table. */
        private fun shuffle(out: ByteArray, shuffleIndex: Int) {
            require(shuffleIndex in 0..23)
            val perm = intArrayOf(0, 1, 2, 3)
            val slotOf = intArrayOf(0, 1, 2, 3)
            val table = shuffleIndex * 4
            for (i in 0 until 3) {
                val desired = POSITION[table + i]
                val j = slotOf[desired]
                if (j == i) continue
                swapBlocks(out, i, j)
                val blockAtI = perm[i]
                perm[j] = blockAtI
                slotOf[blockAtI] = j
            }
        }

        private fun swapBlocks(bytes: ByteArray, a: Int, b: Int) {
            for (i in 0 until BLOCK_SIZE) {
                val ai = 8 + a * BLOCK_SIZE + i
                val bi = 8 + b * BLOCK_SIZE + i
                val t = bytes[ai]
                bytes[ai] = bytes[bi]
                bytes[bi] = t
            }
        }

        private fun checksum(bytes: ByteArray): Int {
            var sum = 0
            for (i in 8 until STORED_SIZE step 2) sum = (sum + u16(bytes, i)) and 0xFFFF
            return sum
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
