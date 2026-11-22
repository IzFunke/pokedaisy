package com.pokedaisy.app.companion

/** An actual 3DS backend that can be selected at game launch. */
enum class N3dsBackend { AZAHAR }

/** The 3DS emulator selected in Settings. Azahar is the only 3DS backend shipped in this build. */
enum class N3dsCore(val id: Int, val label: String) {
    AUTO(0, "AUTO"),
    AZAHAR(1, "AZAHAR");

    fun valueLabel(): String = if (this == AUTO) "AUTO: AZAHAR" else label

    /** Resolve the saved choice against the backends this build can really start. */
    fun resolve(available: List<N3dsBackend> = BUNDLED_BACKENDS): N3dsBackend? = when (this) {
        AUTO -> available.firstOrNull()
        AZAHAR -> N3dsBackend.AZAHAR.takeIf { it in available }
    }

    companion object {
        val BUNDLED_BACKENDS = listOf(N3dsBackend.AZAHAR)
        fun of(id: Int) = entries.firstOrNull { it.id == id } ?: AUTO
    }
}
