package com.pokedaisy.app.companion.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionLoadingStatusTest {
    @Test fun onlyStartupStatusKeepsTheLoadingAnimation() {
        assertTrue(companionDataStillStarting(null))
        assertTrue(companionDataStillStarting("starting…"))
        assertFalse(companionDataStillStarting("3DS companion data is not mapped yet"))
        assertFalse(companionDataStillStarting("no telemetry source"))
    }
}
