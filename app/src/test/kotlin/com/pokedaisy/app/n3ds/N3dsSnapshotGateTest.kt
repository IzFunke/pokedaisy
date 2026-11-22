package com.pokedaisy.app.n3ds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class N3dsSnapshotGateTest {
    @Test
    fun queuedUpdateFromEndedSessionCannotReplaceCurrentGame() {
        val gate = N3dsSnapshotGate()
        val oldSession = gate.begin()
        gate.end()
        val currentSession = gate.begin()
        var oldPublished = false
        var currentPublished = false

        gate.publishIfCurrent(oldSession) { oldPublished = true }
        gate.publishIfCurrent(currentSession) { currentPublished = true }

        assertFalse(oldPublished)
        assertTrue(currentPublished)
    }

    @Test
    fun updateQueuedBeforeStopIsDroppedAfterStop() {
        val gate = N3dsSnapshotGate()
        val session = gate.begin()
        var published = 0

        gate.end()
        gate.publishIfCurrent(session) { published++ }

        assertEquals(0, published)
    }

    @Test
    fun currentSessionPublishesTerminalStateOnceAndClosesGate() {
        val gate = N3dsSnapshotGate()
        val session = gate.begin()
        var terminalPublishes = 0
        var laterPublishes = 0

        assertTrue(gate.finishIfCurrent(session) { terminalPublishes++ })
        assertFalse(gate.finishIfCurrent(session) { terminalPublishes++ })
        gate.publishIfCurrent(session) { laterPublishes++ }

        assertEquals(1, terminalPublishes)
        assertEquals(0, laterPublishes)
    }

    @Test
    fun oldSessionCannotPublishTerminalStateAfterReplacement() {
        val gate = N3dsSnapshotGate()
        val oldSession = gate.begin()
        gate.end()
        val currentSession = gate.begin()
        var oldTerminalPublishes = 0
        var currentPublishes = 0

        assertFalse(gate.finishIfCurrent(oldSession) { oldTerminalPublishes++ })
        gate.publishIfCurrent(currentSession) { currentPublishes++ }

        assertEquals(0, oldTerminalPublishes)
        assertEquals(1, currentPublishes)
    }
}
