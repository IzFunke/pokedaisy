package com.pokedaisy.app.companion

/** The 3DS emulator selected in Settings. Azahar is the only 3DS backend shipped in this build. */
enum class N3dsCore(val id: Int, val label: String) {
    AUTO(0, "AUTO"),
    AZAHAR(1, "AZAHAR");

    fun valueLabel(): String = if (this == AUTO) "AUTO: AZAHAR" else label

    companion object {
        fun of(id: Int) = entries.firstOrNull { it.id == id } ?: AUTO
    }
}
