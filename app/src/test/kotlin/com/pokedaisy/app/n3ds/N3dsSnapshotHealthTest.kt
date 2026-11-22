package com.pokedaisy.app.n3ds

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class N3dsSnapshotHealthTest {
    @Test
    fun sustainedReadFailuresPublishUnavailableOnceUntilRecovery() {
        val health = N3dsSnapshotHealth(failureThreshold = 3)

        assertFalse(health.onReadFailure())
        assertFalse(health.onReadFailure())
        assertTrue(health.onReadFailure())
        assertFalse(health.onReadFailure())
        assertFalse(health.onReadFailure())
    }

    @Test
    fun successfulReadResetsTheFailureStreak() {
        val health = N3dsSnapshotHealth(failureThreshold = 3)

        assertFalse(health.onReadFailure())
        assertFalse(health.onReadFailure())
        health.onReadSuccess()
        assertFalse(health.onReadFailure())
        assertFalse(health.onReadFailure())
        assertTrue(health.onReadFailure())
    }

    @Test
    fun aNewFailureStreakCanPublishAfterRecovery() {
        val health = N3dsSnapshotHealth(failureThreshold = 1)

        assertTrue(health.onReadFailure())
        assertFalse(health.onReadFailure())
        health.onReadSuccess()
        assertTrue(health.onReadFailure())
    }
}
