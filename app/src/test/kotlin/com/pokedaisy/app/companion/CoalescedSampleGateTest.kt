package com.pokedaisy.app.companion

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoalescedSampleGateTest {
    @Test fun requestsDuringWorkBecomeExactlyOneFollowUp() {
        val gate = CoalescedSampleGate()

        assertTrue(gate.request())
        assertFalse(gate.request())
        assertFalse(gate.request())
        assertTrue(gate.finish(allowFollowUp = true))
        assertFalse(gate.finish(allowFollowUp = true))
        assertTrue(gate.request())
    }

    @Test fun closeDiscardsPendingFollowUpAndPreventsNewWork() {
        val gate = CoalescedSampleGate()

        assertTrue(gate.request())
        assertFalse(gate.request())
        gate.close()

        assertTrue(gate.isClosed)
        assertFalse(gate.finish(allowFollowUp = true))
        assertFalse(gate.request())
    }

    @Test fun completionWithoutFollowUpDiscardsPendingRequest() {
        val gate = CoalescedSampleGate()

        assertTrue(gate.request())
        assertFalse(gate.request())
        assertFalse(gate.finish(allowFollowUp = false))
        assertTrue(gate.request())
    }
}
