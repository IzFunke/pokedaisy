package com.pokedaisy.app.n3ds

/** Throttles repeated failed RAM reads so a sampler can replace stale Companion data once per failure streak. */
internal class N3dsSnapshotHealth(private val failureThreshold: Int = 5) {
    init {
        require(failureThreshold > 0)
    }

    private var consecutiveFailures = 0
    private var unavailablePublished = false

    /** Returns true once after [failureThreshold] consecutive failed reads. */
    fun onReadFailure(): Boolean {
        if (unavailablePublished) return false
        consecutiveFailures++
        if (consecutiveFailures < failureThreshold) return false
        unavailablePublished = true
        return true
    }

    /** A good sample ends the failure streak; later failures can publish a new unavailable state. */
    fun onReadSuccess() {
        consecutiveFailures = 0
        unavailablePublished = false
    }
}
