package com.pokedaisy.app.n3ds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class N3dsSceneTransitionTest {
    @Test fun ordinarySceneChangeCapturesOnceAndReleasesOnSecondMatchingRead() {
        val gate = N3dsSceneTransition(Gen6Reader.Scene.FIELD)

        val first = gate.observe(Gen6Reader.Scene.MENU)
        assertTrue(first.capturePair)
        assertFalse(first.releasePair)
        assertEquals(null, first.report)

        val stable = gate.observe(Gen6Reader.Scene.MENU)
        assertFalse(stable.capturePair)
        assertTrue(stable.releasePair)
        assertFalse(stable.cachePair)
        assertEquals(Gen6Reader.Scene.MENU, stable.report)
        assertEquals(Gen6Reader.Scene.MENU, gate.scene)
        assertTrue(gate.observe(Gen6Reader.Scene.MENU).cachePair)
        assertTrue(gate.observe(Gen6Reader.Scene.MENU).cachePair)
    }

    @Test fun screenToFieldWaitsForElevenMatchingObservationsBeforeRelease() {
        val gate = N3dsSceneTransition(Gen6Reader.Scene.SCREEN)
        assertTrue(gate.observe(Gen6Reader.Scene.FIELD).capturePair)
        repeat(9) {
            val pending = gate.observe(Gen6Reader.Scene.FIELD)
            assertFalse(pending.releasePair)
            assertEquals(null, pending.report)
        }
        val stable = gate.observe(Gen6Reader.Scene.FIELD)
        assertTrue(stable.releasePair)
        assertEquals(Gen6Reader.Scene.FIELD, stable.report)
    }

    @Test fun anUnmappedReadDoesNotReleaseAnInProgressTransition() {
        val gate = N3dsSceneTransition(Gen6Reader.Scene.FIELD)
        gate.observe(Gen6Reader.Scene.MENU)
        assertFalse(gate.observe(Gen6Reader.Scene.UNMAPPED).releasePair)
        assertFalse(gate.observe(Gen6Reader.Scene.MENU).releasePair)
        assertTrue(gate.observe(Gen6Reader.Scene.MENU).releasePair)
    }

    @Test fun missingSceneReadsEventuallyReleaseTheFrozenPairWithoutGuessingANewScene() {
        val gate = N3dsSceneTransition(Gen6Reader.Scene.FIELD)
        assertTrue(gate.observe(Gen6Reader.Scene.MENU).capturePair)

        repeat(98) { assertFalse(gate.observe(null).releasePair) }
        val timedOut = gate.observe(null)

        assertTrue(timedOut.releasePair)
        assertEquals(null, timedOut.report)
        assertEquals(Gen6Reader.Scene.FIELD, gate.scene)
        assertFalse(gate.observe(null).releasePair)
    }

    @Test fun changingUnstableCandidatesCannotHoldThePairForever() {
        val gate = N3dsSceneTransition(Gen6Reader.Scene.SCREEN)
        repeat(99) { index ->
            val candidate = if (index % 2 == 0) Gen6Reader.Scene.FIELD else Gen6Reader.Scene.MENU
            val event = gate.observe(candidate)
            assertFalse(event.releasePair)
        }

        val timedOut = gate.observe(Gen6Reader.Scene.FIELD)
        assertTrue(timedOut.releasePair)
        assertEquals(null, timedOut.report)
        assertEquals(Gen6Reader.Scene.SCREEN, gate.scene)
    }
}
