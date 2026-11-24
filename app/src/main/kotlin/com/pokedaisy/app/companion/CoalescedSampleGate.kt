package com.pokedaisy.app.companion

import kotlinx.atomicfu.atomic

/** Shared request lifecycle for game samplers with different capture and decode threading requirements. */
class CoalescedSampleGate {
    private val state = atomic(IDLE)

    val isClosed: Boolean get() = state.value == CLOSED

    /** Returns true only when the caller should begin a sample now. Requests during work become one follow-up. */
    fun request(): Boolean {
        while (true) {
            when (state.value) {
                IDLE -> if (state.compareAndSet(IDLE, READING)) return true
                READING -> if (state.compareAndSet(READING, READING_AGAIN)) return false
                READING_AGAIN, CLOSED -> return false
            }
        }
    }

    /** Called after delivery. Returns true when a coalesced request should begin immediately. */
    fun finish(allowFollowUp: Boolean): Boolean {
        while (true) {
            when (val current = state.value) {
                IDLE, CLOSED -> return false
                READING -> if (state.compareAndSet(READING, IDLE)) return false
                READING_AGAIN -> {
                    val next = if (allowFollowUp) READING else IDLE
                    if (state.compareAndSet(current, next)) return allowFollowUp
                }
            }
        }
    }

    fun close() {
        state.value = CLOSED
    }

    private companion object {
        const val IDLE = 0
        const val READING = 1
        const val READING_AGAIN = 2
        const val CLOSED = 3
    }
}
