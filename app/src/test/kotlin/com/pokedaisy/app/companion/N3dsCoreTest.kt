package com.pokedaisy.app.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class N3dsCoreTest {
    @Test
    fun legacyAutoPreferenceStillSelectsAzahar() {
        assertEquals(N3dsBackend.AZAHAR, N3dsCore.AUTO.resolve())
        assertEquals("AZAHAR", N3dsCore.AUTO.valueLabel())
    }

    @Test
    fun explicitAzaharChoiceResolvesToAzahar() {
        assertEquals(N3dsBackend.AZAHAR, N3dsCore.AZAHAR.resolve())
        assertEquals("AZAHAR", N3dsCore.AZAHAR.valueLabel())
    }

    @Test
    fun unavailableExplicitBackendDoesNotSilentlyFallBack() {
        assertNull(N3dsCore.AZAHAR.resolve(emptyList()))
    }

    @Test
    fun autoReportsNoBackendWhenBuildHasNone() {
        assertNull(N3dsCore.AUTO.resolve(emptyList()))
    }

    @Test
    fun selectorOffersOnlyARealBackend() {
        assertEquals(listOf(N3dsCore.AZAHAR), N3dsCore.offered())
    }

    @Test
    fun persistedCoreIdsRemainCompatible() {
        // 0 was the default AUTO preference in released builds; 1 explicitly selected Azahar.
        assertEquals(N3dsCore.AUTO, N3dsCore.of(0))
        assertEquals(N3dsCore.AZAHAR, N3dsCore.of(1))
        assertEquals(N3dsCore.AUTO, N3dsCore.of(99))
        assertEquals(0, N3dsCore.AUTO.id)
        assertEquals(1, N3dsCore.AZAHAR.id)
    }
}
