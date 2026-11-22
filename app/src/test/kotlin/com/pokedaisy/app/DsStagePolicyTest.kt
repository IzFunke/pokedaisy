package com.pokedaisy.app

import com.pokedaisy.app.companion.DsLayout
import org.junit.Assert.assertEquals
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
        assertTrue(enabled(is3ds = true, portrait = true, companionOverGame = true))
        assertFalse(enabled(is3ds = true, portrait = true, folded = true))
    }

    @Test fun threeDsOneScreenShowsTouchOnlyOnManualRequest() {
        assertFalse(n3dsTouchScreenVisible(DsLayout.ONE_SCREEN, manuallyOpened = false))
        assertTrue(n3dsTouchScreenVisible(DsLayout.ONE_SCREEN, manuallyOpened = true))
        for (layout in DsLayout.entries.filter { it != DsLayout.ONE_SCREEN }) {
            assertTrue("$layout remains explicit", n3dsTouchScreenVisible(layout, manuallyOpened = false))
        }
    }

    @Test fun threeDsTouchWindowFrameTracksTheDsLikeOpenState() {
        assertFalse(n3dsTouchWindowFramed(DsLayout.ONE_SCREEN, windowOpen = false))
        assertTrue(n3dsTouchWindowFramed(DsLayout.ONE_SCREEN, windowOpen = true))
        assertTrue(n3dsTouchWindowFramed(DsLayout.PICTURE_IN_PICTURE, windowOpen = false))
        assertFalse(n3dsTouchWindowFramed(DsLayout.SIDE_BY_SIDE, windowOpen = false))
    }

    @Test fun threeDsMenusAndBattleSubmenusRequestTheTouchDisplayLikeDs() {
        val bag = com.pokedaisy.app.companion.data.BATTLE_INPUT_BAG_OPEN
        val party = com.pokedaisy.app.companion.data.BATTLE_INPUT_PARTY_OPEN
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU, 0))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE, bag))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE, party))
        assertFalse(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, 0))
        assertFalse(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE, 0))
    }

    @Test fun threeDsAutoTouchDismissClosesOnlyGameOpenedPages() {
        val menu = com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU
        val battle = com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE
        val bag = com.pokedaisy.app.companion.data.BATTLE_INPUT_BAG_OPEN
        val party = com.pokedaisy.app.companion.data.BATTLE_INPUT_PARTY_OPEN
        assertTrue(n3dsAutoTouchCanDismiss(menu, 0))
        assertTrue(n3dsAutoTouchCanDismiss(battle, bag))
        assertTrue(n3dsAutoTouchCanDismiss(battle, party))
        assertFalse(n3dsAutoTouchCanDismiss(battle, 0))
        assertFalse(n3dsAutoTouchCanDismiss(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, bag))
    }

    @Test fun threeDsPagesThatNeedBothScreensTemporarilyUseTheDsPairedLayout() {
        assertEquals(DsLayout.SIDE_BY_SIDE, n3dsStageLayout(com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN, DsLayout.ONE_SCREEN))
        assertEquals(DsLayout.SIDE_BY_SIDE, n3dsStageLayout(com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN, DsLayout.PICTURE_IN_PICTURE))
        assertEquals(DsLayout.LARGE_SMALL, n3dsStageLayout(com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN, DsLayout.LARGE_SMALL))
        assertEquals(DsLayout.ONE_SCREEN, n3dsStageLayout(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, DsLayout.ONE_SCREEN))
        assertEquals(DsLayout.PICTURE_IN_PICTURE, n3dsStageLayout(com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU, DsLayout.PICTURE_IN_PICTURE))
    }

    @Test fun threeDsBootDoesNotWaitForHiddenTouchSurfaceButWaitsWhenVisible() {
        assertFalse(n3dsTouchSurfaceRequired(stageOn = true, onSecondDisplay = false, touchVisible = false))
        assertTrue(n3dsTouchSurfaceRequired(stageOn = true, onSecondDisplay = false, touchVisible = true))
        assertTrue(n3dsTouchSurfaceRequired(stageOn = true, onSecondDisplay = true, touchVisible = false))
        assertTrue(n3dsTouchSurfaceRequired(stageOn = false, onSecondDisplay = true, touchVisible = true))
        assertFalse(n3dsTouchSurfaceRequired(stageOn = false, onSecondDisplay = false, touchVisible = false))
    }

    @Test fun ndsMenuTrackingNeverTurnsAThreeDsLayoutIntoDualScreen() {
        for (layout in DsLayout.entries) {
            assertEquals("3DS keeps $layout when NDS menu tracking is active", layout, dsStageLayout(is3ds = true, dsMenuUp = true, chosen = layout))
        }
        assertEquals(DsLayout.SIDE_BY_SIDE, dsStageLayout(is3ds = false, dsMenuUp = true, chosen = DsLayout.ONE_SCREEN))
        assertEquals(DsLayout.ONE_SCREEN, dsStageLayout(is3ds = false, dsMenuUp = false, chosen = DsLayout.ONE_SCREEN))
    }

    @Test fun touchScreenQuickActionHotkeyAndHintAreAvailableForEitherSystem() {
        assertFalse(touchScreenToggleAvailable(dsFloats = false, n3dsFloats = false))
        assertTrue(touchScreenToggleAvailable(dsFloats = true, n3dsFloats = false))
        assertTrue(touchScreenToggleAvailable(dsFloats = false, n3dsFloats = true))
    }
}
