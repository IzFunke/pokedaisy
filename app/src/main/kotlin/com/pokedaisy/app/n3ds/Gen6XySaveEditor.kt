package com.pokedaisy.app.n3ds

import com.pokedaisy.app.companion.data.save.*
import com.pokedaisy.app.companion.data.gen3ExperienceTables
import com.pokedaisy.app.companion.i18n.tr
import java.io.File

/**
 * The shared SAVE EDITOR adapter for decrypted Pokémon X/Y saves.
 *
 * The first exposed edits are deliberately limited to operations whose save representation is independently
 * validated here: swapping PC records and releasing a PC Pokémon. Party/PC moves and field edits remain refused until
 * the shared editor has the corresponding verified Gen 6 rules and capabilities.
 */
internal class Gen6XySaveEditor(
    val file: Gen6XySave,
    private val game: Gen6XyGameData,
) : MonSaveEditor {
    private var working = file

    constructor(file: Gen6XySave, cart: File, language: Int) : this(
        file,
        Gen6XyGameData(
            requireNotNull(Gen6Text.load(cart, language, N3dsLayout.XY)) { "X/Y localized game text couldn't be read from this ROM" },
            requireNotNull(Gen6Personal.loadRecords(cart, N3dsLayout.XY)) { "X/Y personal data couldn't be read from this ROM" },
        ),
    )

    override val data: MonSave get() = Gen6XySaveData(working, game)
    override val changed: Boolean get() = !working.data.contentEquals(file.data)
    override fun fresh(): MonSaveEditor = Gen6XySaveEditor(file, game)
    override fun build(): ByteArray = working.data
    override fun snapshot(): EditorSnapshot = EditorSnapshot.of(listOf(working.data))

    override fun restore(snapshot: EditorSnapshot) {
        val bytes = snapshot.copyPart(0)
        working = when (val parsed = Gen6XySave.parse(bytes)) {
            is Gen6XySave.Parsed.Ok -> parsed.save
            is Gen6XySave.Parsed.Failed -> throw IllegalArgumentException(parsed.reason)
        }
    }

    override fun monAt(at: MonRef): SaveMon? = data.mon(at)?.first

    override fun move(from: MonRef, to: MonRef) {
        if (from == to) return
        val a = (from as? MonRef.Pc)?.at ?: throw SaveEditRefused(tr("X/Y Pokémon can only move between PC boxes"))
        val b = (to as? MonRef.Pc)?.at ?: throw SaveEditRefused(tr("X/Y Pokémon can only move between PC boxes"))
        val first = record(a)
        val second = record(b)
        if (first.isBadEgg || first.isEmpty) throw SaveEditRefused(tr("Nothing to move there"))
        if (second.isBadEgg) throw SaveEditRefused(tr("That Pokémon record can't be changed"))
        working = replace(a, second.encrypted)
        working = replace(b, first.encrypted)
    }

    override fun release(at: BoxSlot) {
        requirePc(at)
        val mon = data.mon(MonRef.Pc(at))?.first ?: throw SaveEditRefused(tr("Nothing to release there"))
        if (mon.isEmpty || mon.isEgg || mon.isBadEgg) throw SaveEditRefused(tr("Eggs stay as they are until they hatch."))
        val empty = ByteArray(Gen6PokemonRecord.STORED_SIZE)
        working = replace(at, empty)
    }

    override fun sortBox(box: Int, by: BoxSort, name: (SaveMon) -> String): Nothing =
        throw SaveEditRefused("box sorting isn't enabled for X/Y yet")

    override val canWriteNames: Boolean get() = false
    override val canEditItems: Boolean get() = false
    override val canEditLevel: Boolean get() = false
    override val canEditTraits: Boolean get() = false
    override val canEditFriendship: Boolean get() = false
    override val canEditStats: Boolean get() = false
    override val canEditMoves: Boolean get() = false
    override val canSortBoxes: Boolean get() = false
    override fun canMoveFrom(at: MonRef): Boolean = at is MonRef.Pc && monAt(at)?.let { !it.isEmpty && !it.isBadEgg } == true

    override fun nicknameMax(at: MonRef): Int = 0
    override val boxNameMax: Int get() = 0
    override fun canWrite(text: String): Boolean = false
    override fun renameBox(box: Int, name: String): Nothing = unsupported()
    override fun isOwn(mon: SaveMon): Boolean = false
    override fun setNickname(at: MonRef, name: String): Nothing = unsupported()
    override fun levelAsRead(at: MonRef): Int = data.mon(at)?.second ?: 1
    override fun setLevel(at: MonRef, level: Int): Nothing = unsupported()
    override fun setIvs(at: MonRef, ivs: IntArray): Nothing = unsupported()
    override fun setEvs(at: MonRef, evs: IntArray): Nothing = unsupported()
    override fun setFriendship(at: MonRef, value: Int): Nothing = unsupported()
    override fun reroll(at: MonRef, nature: Int?, shiny: Boolean?, gender: Gen3Gender?, abilityNum: Int?): Nothing = unsupported()
    override fun learnable(at: MonRef): Set<Int> = emptySet()
    override fun setMoves(at: MonRef, moves: List<Int>): Nothing = unsupported()
    override fun holdable(): List<Int> = emptyList()
    override fun setHeldItem(at: MonRef, item: Int): Nothing = unsupported()

    private fun unsupported(): Nothing = throw SaveEditRefused(tr("This edit isn't available for X/Y yet"))

    private fun record(at: BoxSlot): SlotRecord {
        requirePc(at)
        return when (val parsed = working.boxRecord(at.box, at.slot)) {
            is Gen6PokemonRecord.Parsed.Ok -> SlotRecord(parsed.record.isEmpty, false, parsed.record.toEncrypted())
            is Gen6PokemonRecord.Parsed.Failed -> SlotRecord(false, true, ByteArray(0))
        }
    }

    private fun replace(at: BoxSlot, encrypted: ByteArray): Gen6XySave = when (val parsed = working.withBoxRecord(at.box, at.slot, encrypted)) {
        is Gen6XySave.Parsed.Ok -> parsed.save
        is Gen6XySave.Parsed.Failed -> throw SaveEditRefused(parsed.reason)
    }

    private fun requirePc(at: BoxSlot) {
        require(at.box in 0 until Gen6XySave.BOX_COUNT && at.slot in 0 until Gen6XySave.BOX_SLOTS) {
            "X/Y PC slot is out of range: $at"
        }
    }

    private data class SlotRecord(val isEmpty: Boolean, val isBadEgg: Boolean, val encrypted: ByteArray)
}

/** Read-only view of the X/Y party and PC slots for the shared editor. */
private class Gen6XySaveData(
    private val save: Gen6XySave,
    override val game: Gen6XyGameData,
) : MonSave {
    override val trainerName: String = ""
    override val trainerId: Int = 0
    override val secretId: Int = 0
    override val partyCount: Int = save.partyCount.coerceIn(0, Gen6XySave.PARTY_SIZE)
    override val partyMons: List<PartyEntry> by lazy {
        (0 until partyCount).mapNotNull { index ->
            val mon = readParty(index) ?: return@mapNotNull null
            val level = mon.level.coerceIn(1, 100)
            PartyEntry(mon, level, mon.stat(0xF0), intArrayOf(
                mon.stat(0xF2), // max HP
                mon.stat(0xF4), // Attack
                mon.stat(0xF6), // Defense
                mon.stat(0xF8), // Speed
                mon.stat(0xFA), // Sp. Atk
                mon.stat(0xFC), // Sp. Def
            ))
        }
    }
    override val boxCount: Int get() = Gen6XySave.BOX_COUNT
    override val currentBox: Int get() = 0

    override fun boxName(box: Int): String {
        require(box in 0 until boxCount)
        return "BOX ${box + 1}"
    }

    override fun boxMons(box: Int): List<SaveMon> {
        require(box in 0 until boxCount)
        return (0 until Gen6XySave.BOX_SLOTS).map { slot -> readBox(box, slot) }
    }

    override fun mon(at: MonRef): Pair<SaveMon, Int>? = when (at) {
        is MonRef.Party -> partyMons.getOrNull(at.index)?.let { it.mon to it.level }
        is MonRef.Pc -> {
            val mon = readBox(at.at.box, at.at.slot)
            if (mon.isEmpty) null else mon to game.levelFor(mon.species, mon.exp)
        }
    }

    private fun readBox(box: Int, slot: Int): Gen6XyMon = when (val parsed = save.boxRecord(box, slot)) {
        is Gen6PokemonRecord.Parsed.Ok -> Gen6XyMon(parsed.record.data, false, game)
        is Gen6PokemonRecord.Parsed.Failed -> Gen6XyMon.empty(game)
    }

    private fun readParty(slot: Int): Gen6XyMon? = when (val parsed = save.partyRecord(slot)) {
        is Gen6PokemonRecord.Parsed.Ok -> parsed.record.takeUnless { it.isEmpty }?.let { Gen6XyMon(it.data, true, game) }
        is Gen6PokemonRecord.Parsed.Failed -> null
    }
}

/** Field reads from an unencrypted PK6 record; offsets follow the Gen 6 format. */
private class Gen6XyMon(
    private val raw: ByteArray,
    private val party: Boolean,
    override val game: Gen6XyGameData,
) : SaveMon {
    override val species: Int get() = u16(0x08)
    override val isEmpty: Boolean get() = species == 0
    override val isEgg: Boolean get() = (u32(0x74) and (1L shl 30)) != 0L
    override val isBadEgg: Boolean get() = false
    override val personality: Long get() = u32(0x18)
    override val otTid: Int get() = u16(0x0C)
    override val otSid: Int get() = u16(0x0E)
    override val heldItem: Int get() = u16(0x0A)
    override val friendship: Int get() = raw[if ((raw.getOrNull(0x93)?.toInt() ?: 0) == 0) 0xCA else 0xA2].toInt() and 0xFF
    override val exp: Long get() = u32(0x10)
    override val nature: Int get() = raw[0x1C].toInt() and 0xFF
    /** PK6 stores ability slot numbers as 1/2/4; the shared model represents regular slots as 0/1. */
    override val abilityNum: Int get() = when (raw[0x15].toInt() and 0xFF) { 1 -> 0; 2 -> 1; 4 -> 2; else -> raw[0x15].toInt() and 0xFF }
    override val ability: Int get() = raw[0x14].toInt() and 0xFF
    override val gender: Gen3Gender get() = when ((raw[0x1D].toInt() ushr 1) and 3) {
        0 -> Gen3Gender.MALE
        1 -> Gen3Gender.FEMALE
        else -> Gen3Gender.GENDERLESS
    }
    override val isShiny: Boolean get() =
        ((otTid xor otSid xor (personality ushr 16).toInt() xor personality.toInt()) and 0xFFFF) < 16
    override val moves: IntArray get() = IntArray(4) { u16(0x5A + it * 2) }
    override val pp: IntArray get() = IntArray(4) { raw[0x62 + it].toInt() and 0xFF }
    override val ppBonuses: Int get() = raw[0x66].toInt() and 0xFF
    override val ivs: IntArray get() {
        val word = u32(0x74)
        return IntArray(6) { ((word ushr (it * 5)) and 31).toInt() }
    }
    override val evs: IntArray get() = IntArray(6) { raw[0x1E + it].toInt() and 0xFF }
    override val pokerus: Int get() = raw[0x2B].toInt() and 0xFF
    override val metLevel: Int get() = raw[0xDD].toInt() and 0x7F
    override val level: Int get() = if (party) raw.getOrNull(0xEC)?.toInt()?.and(0xFF) ?: 1 else game.levelFor(species, exp)
    override val nickname: String? get() = if ((u32(0x74) and (1L shl 31)) == 0L) null else stringAt(0x40, 26)
    override val otName: String get() = stringAt(0xB0, 26)
    override val metGameLabel: String get() = when (raw.getOrNull(0xDF)?.toInt()?.and(0xFF)) {
        24 -> "Pokémon X"
        25 -> "Pokémon Y"
        else -> "Pokémon X/Y"
    }

    fun stat(offset: Int): Int = u16(offset)
    private fun u16(offset: Int): Int = if (offset + 1 < raw.size) (raw[offset].toInt() and 0xFF) or ((raw[offset + 1].toInt() and 0xFF) shl 8) else 0
    private fun u32(offset: Int): Long = u16(offset).toLong() or (u16(offset + 2).toLong() shl 16)
    private fun stringAt(offset: Int, size: Int): String {
        val end = (offset + size).coerceAtMost(raw.size)
        val chars = ByteArray(end - offset)
        raw.copyInto(chars, 0, offset, end)
        val value = String(chars, Charsets.UTF_16LE)
        val terminators = listOf(value.indexOf('\u0000'), value.indexOf('\uFFFF')).filter { it >= 0 }
        return if (terminators.isEmpty()) value else value.substring(0, terminators.minOrNull()!!)
    }

    companion object {
        fun empty(game: Gen6XyGameData) = Gen6XyMon(ByteArray(Gen6PokemonRecord.STORED_SIZE), false, game)
    }
}

/** Localized text and personal data extracted from the matching X/Y ROM, never guessed from English fallback tables. */
internal class Gen6XyGameData(
    private val speciesNames: List<String>,
    private val moveNames: List<String>,
    private val itemNames: List<String>,
    private val abilityNames: List<String>,
    private val personal: List<ByteArray>,
    private val movePp: IntArray = IntArray(0),
    private val nationalDexMaximum: Int = 721,
) : SaveGameData {
    constructor(text: Gen6Text, personal: List<ByteArray>, nationalDexMaximum: Int = 721) : this(
        text.species, text.moves, text.items, text.abilities, personal, nationalDexMaximum = nationalDexMaximum,
    )

    private fun p(species: Int): ByteArray? = personal.getOrNull(species)?.takeIf { it.size >= PERSONAL_SIZE }
    override fun speciesName(species: Int): String? = speciesNames.getOrNull(species)?.takeIf { it.isNotBlank() }
    override fun moveName(move: Int): String? = moveNames.getOrNull(move)?.takeIf { it.isNotBlank() }
    override fun itemName(item: Int): String? = itemNames.getOrNull(item)?.takeIf { it.isNotBlank() }
    override fun abilityName(ability: Int): String = abilityNames.getOrNull(ability).orEmpty()
    override fun nationalDex(species: Int): Int? = species.takeIf { it in 1..nationalDexMaximum }
    override fun abilities(species: Int): List<Int> = p(species)?.let { row ->
        listOf(row[0x18].toInt() and 0xFF, row[0x19].toInt() and 0xFF, row[0x1A].toInt() and 0xFF).filter { it != 0 }.distinct()
    }.orEmpty()
    override fun genderRatio(species: Int): Int = p(species)?.get(0x12)?.toInt()?.and(0xFF) ?: 255
    override fun baseStats(species: Int): IntArray = p(species)?.let { row -> IntArray(6) { row[it].toInt() and 0xFF } } ?: IntArray(6)
    override fun expForLevel(species: Int, level: Int): Long {
        val rate = p(species)?.get(0x15)?.toInt()?.and(0xFF)?.takeIf { it in gen3ExperienceTables.indices } ?: return 0
        return gen3ExperienceTables[rate][level.coerceIn(1, 100)].toLong()
    }
    override fun basePp(move: Int): Int = movePp.getOrElse(move) { 0 }
    override fun isMail(item: Int): Boolean = false
    override val unown: Int get() = 201
    override val wurmpleLine: IntRange get() = 265..269
    override val shedinja: Int get() = 292

    private companion object {
        const val PERSONAL_SIZE = 0x40
    }
}
