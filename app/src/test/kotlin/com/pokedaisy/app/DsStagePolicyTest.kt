package com.pokedaisy.app

import com.pokedaisy.app.companion.DsLayout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DsStagePolicyTest {
    private fun enabled(
        isNds: Boolean = false,
        is3ds: Boolean = false,
        portrait: Boolean = false,
        secondScreen: Boolean = false,
        debugMirror: Boolean = false,
        companionOverGame: Boolean = false,
        folded: Boolean = false,
    ) = dsStageEnabled(isNds, is3ds, portrait, secondScreen, debugMirror, companionOverGame, folded)

    @Test fun threeDsUsesSingleScreenLayoutInPortraitAndLandscape() {
        assertTrue(enabled(is3ds = true, portrait = true))
        assertTrue(enabled(is3ds = true, portrait = false))
    }

    @Test fun portraitDsBehaviorIsUnchanged() {
        assertFalse(enabled(isNds = true, portrait = true))
        assertTrue(enabled(isNds = true, portrait = false))
    }

    @Test fun anotherScreenOrSpecialLayoutKeepsItsExistingPath() {
        assertFalse(enabled(is3ds = true, portrait = true, secondScreen = true))
        assertFalse(enabled(is3ds = true, portrait = true, debugMirror = true))
        assertFalse(enabled(is3ds = true, portrait = true, companionOverGame = true))
        assertFalse(enabled(is3ds = true, portrait = true, folded = true))
    }

    @Test fun threeDsOneScreenShowsTouchOnlyOnManualRequest() {
        assertFalse(n3dsTouchScreenVisible(DsLayout.ONE_SCREEN, manuallyOpened = false))
        assertTrue(n3dsTouchScreenVisible(DsLayout.ONE_SCREEN, manuallyOpened = true))
        for (layout in DsLayout.entries.filter { it != DsLayout.ONE_SCREEN }) {
            assertTrue("$layout remains explicit", n3dsTouchScreenVisible(layout, manuallyOpened = false))
        }
    }
}
