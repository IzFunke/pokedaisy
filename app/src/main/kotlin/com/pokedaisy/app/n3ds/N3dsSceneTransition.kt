package com.pokedaisy.app.n3ds

/** Debounces verified module-derived scenes and latches the last paired Azahar frame, bounded to 100 scene polls. */
internal class N3dsSceneTransition(initial: Gen6Reader.Scene) {
    internal data class Event(
        val report: Gen6Reader.Scene? = null,
        val capturePair: Boolean = false,
        val cachePair: Boolean = false,
        val releasePair: Boolean = false,
    )

    var scene: Gen6Reader.Scene = initial
        private set

    private var seen: Gen6Reader.Scene? = null
    private var streak = 0
    private var holding = false
    private var heldReads = 0
    private var reported = false

    fun observe(next: Gen6Reader.Scene?): Event {
        if (holding) heldReads++
        streak = if (next == seen) streak + 1 else 1
        seen = next
        if (next == null) return timeoutIfNeeded()

        if (next == scene) {
            val release = holding
            holding = false
            heldReads = 0
            val report = next.takeIf { !reported && streak >= STABLE_READS }
            if (report != null) reported = true
            return Event(report = report, cachePair = streak >= STABLE_READS, releasePair = release)
        }

        val capture = !holding
        if (capture) heldReads = 1
        holding = true
        val needed = when {
            scene == Gen6Reader.Scene.SCREEN && next == Gen6Reader.Scene.FIELD -> SCREEN_TO_FIELD_READS
            scene == Gen6Reader.Scene.MENU && next == Gen6Reader.Scene.FIELD -> MENU_TO_FIELD_READS
            else -> STABLE_READS
        }
        if (streak < needed) {
            val timeout = timeoutIfNeeded()
            return if (timeout.releasePair) timeout.copy(capturePair = capture) else Event(capturePair = capture)
        }

        scene = next
        reported = true
        holding = false
        heldReads = 0
        return Event(report = next, capturePair = capture, releasePair = true)
    }

    private fun timeoutIfNeeded(): Event {
        if (!holding || heldReads < MAX_HOLD_READS) return Event()
        holding = false
        heldReads = 0
        return Event(releasePair = true)
    }

    private companion object {
        const val STABLE_READS = 2
        const val MENU_TO_FIELD_READS = 3
        const val SCREEN_TO_FIELD_READS = 11
        /** Scene reads are 100 ms apart; bound a stale paired frame like the DS transition's 600-frame timeout. */
        const val MAX_HOLD_READS = 100
    }
}
