package com.pokedaisy.app.companion

/** An actual 3DS backend that can be selected at game launch. */
enum class N3dsBackend { AZAHAR }

/** The 3DS emulator selected in Settings. Azahar is the only 3DS backend shipped in this build. */
enum class N3dsCore(val id: Int, val label: String) {
    AUTO(0, "AUTO"),
    AZAHAR(1, "AZAHAR");

    /** AUTO is retained as id 0 for existing preferences, but resolves to the only shipped backend. */
    fun valueLabel(): String = when (this) {
        AUTO -> AZAHAR.label
        AZAHAR -> label
    }

    /** Resolve the saved choice against the backends this build can really start. */
    fun resolve(available: List<N3dsBackend> = BUNDLED_BACKENDS): N3dsBackend? = when (this) {
        AUTO -> available.firstOrNull()
        AZAHAR -> N3dsBackend.AZAHAR.takeIf { it in available }
    }

    companion object {
        val BUNDLED_BACKENDS = listOf(N3dsBackend.AZAHAR)

        /** Only backends that can actually be selected and started in this build. */
        fun offered(): List<N3dsCore> = listOf(AZAHAR)

        fun of(id: Int) = entries.firstOrNull { it.id == id } ?: AUTO
    }
}
