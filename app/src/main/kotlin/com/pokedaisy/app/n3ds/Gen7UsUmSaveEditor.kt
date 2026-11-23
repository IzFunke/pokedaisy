package com.pokedaisy.app.n3ds

import com.pokedaisy.app.companion.data.save.*
import com.pokedaisy.app.companion.i18n.tr
import java.io.File

/** Read-only Gen 7 save surface. USUM's checksum table must be RSA-signed before any write. */
internal class Gen7UsUmSaveEditor(
    private val save: Gen7UsUmSave,
    private val game: Gen6XyGameData,
) : MonSaveEditor {
    constructor(save: Gen7UsUmSave, cart: File, language: Int) : this(save, Gen6XyGameData(
        requireNotNull(Gen6Text.load(cart, language, N3dsLayout.USUM)) { "USUM localized text couldn't be read from this ROM" },
        requireNotNull(Gen6Personal.loadRecords(cart, N3dsLayout.USUM)) { "USUM personal data couldn't be read from this ROM" }, 807,
    ))

    override val data: MonSave get() = View(save, game)
    override val changed: Boolean get() = false
    override fun fresh(): MonSaveEditor = Gen7UsUmSaveEditor(save, game)
    override fun build(): ByteArray = save.data
    override fun snapshot(): EditorSnapshot = EditorSnapshot.of(listOf(save.data))
    override fun restore(snapshot: EditorSnapshot) { require(snapshot.copyPart(0).contentEquals(save.data)) { "USUM save is read-only until its MemeCrypto signature can be regenerated" } }
    override fun monAt(at: MonRef): SaveMon? = data.mon(at)?.first
    override fun move(from: MonRef, to: MonRef): Nothing = unsupported()
    override fun release(at: BoxSlot): Nothing = unsupported()
    override fun sortBox(box: Int, by: BoxSort, name: (SaveMon) -> String): Nothing = unsupported()
    override val canWriteNames get() = false
    override val canEditItems get() = false
    override val canEditLevel get() = false
    override val canEditTraits get() = false
    override val canEditFriendship get() = false
    override val canEditStats get() = false
    override val canEditMoves get() = false
    override val canSortBoxes get() = false
    override fun canMoveFrom(at: MonRef) = false
    override fun nicknameMax(at: MonRef) = 0
    override val boxNameMax get() = 0
    override fun canWrite(text: String) = false
    override fun renameBox(box: Int, name: String): Nothing = unsupported()
    override fun isOwn(mon: SaveMon) = mon.otTid == data.trainerId && mon.otSid == data.secretId
    override fun setNickname(at: MonRef, name: String): Nothing = unsupported()
    override fun levelAsRead(at: MonRef) = data.mon(at)?.second ?: 1
    override fun setLevel(at: MonRef, level: Int): Nothing = unsupported()
    override fun setIvs(at: MonRef, ivs: IntArray): Nothing = unsupported()
    override fun setEvs(at: MonRef, evs: IntArray): Nothing = unsupported()
    override fun setFriendship(at: MonRef, value: Int): Nothing = unsupported()
    override fun reroll(at: MonRef, nature: Int?, shiny: Boolean?, gender: Gen3Gender?, abilityNum: Int?): Nothing = unsupported()
    override fun learnable(at: MonRef): Set<Int> = emptySet()
    override fun setMoves(at: MonRef, moves: List<Int>): Nothing = unsupported()
    override fun holdable(): List<Int> = emptyList()
    override fun setHeldItem(at: MonRef, item: Int): Nothing = unsupported()

    private fun unsupported(): Nothing = throw SaveEditRefused(tr("Ultra Sun/Ultra Moon saves are read-only until the game signature can be regenerated"))

    private class View(private val save: Gen7UsUmSave, override val game: Gen6XyGameData) : MonSave {
        override val trainerName get() = save.trainerName
        override val trainerId get() = save.trainerId
        override val secretId get() = save.secretId
        override val partyCount get() = save.partyCount.coerceIn(0, Gen7UsUmSave.PARTY_SIZE)
        override val partyMons by lazy { (0 until partyCount).mapNotNull { slot ->
            val mon = read(save.partyRecord(slot), true) ?: return@mapNotNull null
            PartyEntry(mon, mon.level.coerceIn(1, 100), mon.stat(0xF0), IntArray(6) { mon.stat(0xF2 + it * 2) })
        } }
        override val boxCount get() = Gen7UsUmSave.BOX_COUNT
        override val currentBox get() = 0
        override fun boxName(box: Int): String { require(box in 0 until boxCount); return "BOX ${box + 1}" }
        override fun boxMons(box: Int): List<SaveMon> { require(box in 0 until boxCount); return (0 until Gen7UsUmSave.BOX_SLOTS).map { read(save.boxRecord(box, it), false) ?: Mon(ByteArray(Gen6PokemonRecord.STORED_SIZE), false, game) } }
        override fun mon(at: MonRef): Pair<SaveMon, Int>? = when (at) {
            is MonRef.Party -> partyMons.getOrNull(at.index)?.let { it.mon to it.level }
            is MonRef.Pc -> boxMons(at.at.box)[at.at.slot].takeUnless { it.isEmpty }?.let { it to game.levelFor(it.species, it.exp) }
        }
        private fun read(parsed: Gen6PokemonRecord.Parsed, party: Boolean): Mon? = when (parsed) {
            is Gen6PokemonRecord.Parsed.Failed -> null
            is Gen6PokemonRecord.Parsed.Ok -> parsed.record.takeUnless { it.isEmpty }?.let { Mon(it.data, party, game) }
        }
    }

    private class Mon(private val b: ByteArray, private val party: Boolean, override val game: Gen6XyGameData) : SaveMon {
        private fun u16(o: Int) = (b[o].toInt() and 255) or ((b[o + 1].toInt() and 255) shl 8)
        private fun u32(o: Int) = u16(o).toLong() or (u16(o + 2).toLong() shl 16)
        private fun str(o: Int, n: Int): String { val s = String(b, o, n, Charsets.UTF_16LE); return s.substringBefore('\u0000').substringBefore('\uFFFF') }
        override val species get() = u16(8)
        override val isEmpty get() = species == 0
        override val isEgg get() = (u32(0x74) and (1L shl 30)) != 0L
        override val isBadEgg get() = false
        override val personality get() = u32(0x18)
        override val otTid get() = u16(0x0C)
        override val otSid get() = u16(0x0E)
        override val heldItem get() = u16(0x0A)
        override val friendship get() = b[0xCA].toInt() and 255
        override val exp get() = u32(0x10)
        override val nature get() = b[0x1C].toInt() and 255
        override val abilityNum get() = when (b[0x15].toInt() and 255) { 1 -> 0; 2 -> 1; 4 -> 2; else -> b[0x15].toInt() and 255 }
        override val ability get() = b[0x14].toInt() and 255
        override val gender get() = when ((b[0x1D].toInt() ushr 1) and 3) { 0 -> Gen3Gender.MALE; 1 -> Gen3Gender.FEMALE; else -> Gen3Gender.GENDERLESS }
        override val isShiny get() = ((otTid xor otSid xor (personality ushr 16).toInt() xor personality.toInt()) and 0xFFFF) < 16
        override val moves get() = IntArray(4) { u16(0x5A + 2 * it) }
        override val pp get() = IntArray(4) { b[0x62 + it].toInt() and 255 }
        override val ppBonuses get() = b[0x66].toInt() and 255
        override val ivs get() = IntArray(6) { ((u32(0x74) ushr (5 * it)) and 31).toInt() }
        override val evs get() = IntArray(6) { b[0x1E + it].toInt() and 255 }
        override val pokerus get() = b[0x2B].toInt() and 255
        override val metLevel get() = b[0xDD].toInt() and 0x7F
        override val level get() = if (party) b[0xEC].toInt() and 255 else game.levelFor(species, exp)
        override val nickname get() = if ((u32(0x74) and (1L shl 31)) == 0L) null else str(0x40, 26)
        override val otName get() = str(0xB0, 26)
        override val metGameLabel get() = "Pokémon Ultra Sun/Ultra Moon"
        fun stat(offset: Int) = u16(offset)
    }
}
