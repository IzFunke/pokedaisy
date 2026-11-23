package com.pokedaisy.app.companion.ui

import com.pokedaisy.app.companion.NewFeatures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class N3dsSettingRowsTest {
    @Test
    fun n3dsCoreSearchFindsAzaharAndGameTerms() {
        val rows = listOf(
            SettingRow("EMULATOR", null, header = true, onClick = {}),
            SettingRow("3DS CORE", "AUTO", onClick = {}),
        )

        assertEquals(listOf("EMULATOR", "3DS CORE"), filterRows(rows, "azahar").map { it.label })
        assertEquals(listOf("EMULATOR", "3DS CORE"), filterRows(rows, "ultra moon").map { it.label })
    }

    @Test
    fun n3dsCoreFeatureIsRegisteredForItsNewBadge() {
        assertTrue(NewFeatures.N3DS_CORE in NewFeatures.ALL)
        assertEquals(NewFeatures.ALL.size, NewFeatures.ALL.distinct().size)
    }
}
