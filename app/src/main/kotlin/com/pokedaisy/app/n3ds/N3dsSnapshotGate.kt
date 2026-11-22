package com.pokedaisy.app.n3ds

/** Prevents a queued Companion update from an ended Azahar session replacing a newer game's snapshot. */
internal class N3dsSnapshotGate {
    private var generation = 0L
    private var active = false

    @Synchronized
    fun begin(): Long {
        generation++
        active = true
        return generation
    }

    @Synchronized
    fun end() {
        active = false
        generation++
    }

    /** Publish a terminal snapshot only if this is still the active session, then close its gate. */
    @Synchronized
    fun finishIfCurrent(token: Long, publish: () -> Unit): Boolean {
        if (!active || generation != token) return false
        active = false
        generation++
        publish()
        return true
    }

    /** Run a UI-queued publication only while it still belongs to the current live session. */
    @Synchronized
    fun publishIfCurrent(token: Long, publish: () -> Unit) {
        if (active && generation == token) publish()
    }
}
