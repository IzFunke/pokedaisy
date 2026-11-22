package com.pokedaisy.app.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class N3dsCoreTest {
    @Test
    fun autoSelectsTheFirstBundledBackend() {
        assertEquals(N3dsBackend.AZAHAR, N3dsCore.AUTO.resolve())
    }

    @Test
    fun explicitAzaharChoiceResolvesToAzahar() {
        assertEquals(N3dsBackend.AZAHAR, N3dsCore.AZAHAR.resolve())
    }

    @Test
    fun unavailableExplicitBackendDoesNotSilentlyFallBack() {
        assertNull(N3dsCore.AZAHAR.resolve(emptyList()))
    }

    @Test
    fun autoReportsNoBackendWhenBuildHasNone() {
        assertNull(N3dsCore.AUTO.resolve(emptyList()))
    }
}
