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
        assertFalse(n3dsTouchVisibleForLayout(DsLayout.ONE_SCREEN, manuallyOpened = false, autoWanted = false))
        assertTrue(n3dsTouchVisibleForLayout(DsLayout.ONE_SCREEN, manuallyOpened = false, autoWanted = true))
        assertTrue(n3dsTouchVisibleForLayout(DsLayout.ONE_SCREEN, manuallyOpened = true, autoWanted = false))
        for (layout in DsLayout.entries.filter { it != DsLayout.ONE_SCREEN }) {
            assertTrue("$layout remains explicit", n3dsTouchScreenVisible(layout, manuallyOpened = false))
        }
    }

    @Test fun threeDsDualScreenLayoutsKeepBothScreensAndNeverUseTheFloatingOneScreenFit() {
        for (layout in listOf(DsLayout.SIDE_BY_SIDE, DsLayout.LARGE_SMALL, DsLayout.STACKED)) {
            val pair = com.pokedaisy.app.companion.DsLayouts.arrange(layout, 1080, 720, fill = false,
                topAspect = com.pokedaisy.app.companion.DsLayouts.N3DS_TOP_ASPECT)
            assertTrue("$layout includes the touch display", pair.touch != null)
            assertEquals(layout, n3dsStageLayout(layout))
        }
        val oneScreen = com.pokedaisy.app.companion.DsLayouts.arrange(DsLayout.ONE_SCREEN, 1080, 720, fill = true,
            topAspect = com.pokedaisy.app.companion.DsLayouts.N3DS_TOP_ASPECT).top
        assertEquals(1080, oneScreen.width)
        assertEquals(720, oneScreen.height)
    }

    @Test fun threeDsOneScreenFloatingWindowFitsPhonesAndHandheldAspectRatiosInsideItsFrame() {
        val frame = 30f
        val profiles = listOf(360 to 800, 800 to 360, 412 to 915, 915 to 412, 2340 to 1080)
        for ((width, height) in profiles) {
            val result = n3dsStageArrangement(DsLayout.ONE_SCREEN, width, height, fill = true, floating = false, framePx = frame)
            val touch = requireNotNull(result.touch)
            val outerWidth = touch.width + frame
            val outerHeight = touch.height + frame
            assertTrue("$width x $height stays within width share", outerWidth <= width * com.pokedaisy.app.companion.TOUCH_WINDOW_WIDTH_SHARE + 1)
            assertTrue("$width x $height stays within height share", outerHeight <= height * com.pokedaisy.app.companion.TOUCH_WINDOW_HEIGHT_SHARE + 1)
            assertTrue("$width x $height keeps 4:3 touch output", kotlin.math.abs(touch.width.toFloat() / touch.height - 4f / 3f) < 0.01f)
            assertTrue(kotlin.math.abs((touch.left + touch.right) - width) <= 1f)
            assertTrue(kotlin.math.abs((touch.top + touch.bottom) - height) <= 1f)
        }
    }

    @Test fun threeDsPipAutoOrManualOverrideFloatsWhileSideBySideStaysSelected() {
        val base = com.pokedaisy.app.companion.DsLayouts.arrange(DsLayout.PICTURE_IN_PICTURE, 1080, 720, fill = false,
            topAspect = com.pokedaisy.app.companion.DsLayouts.N3DS_TOP_ASPECT)
        val closedPip = n3dsStageArrangement(DsLayout.PICTURE_IN_PICTURE, 1080, 720, fill = false, floating = false, framePx = 30f)
        assertEquals(base, closedPip)

        for ((manual, auto) in listOf(false to true, true to false)) {
            val floating = manual || auto
            val window = n3dsStageArrangement(DsLayout.PICTURE_IN_PICTURE, 1080, 720, fill = false, floating = floating, framePx = 30f)
            assertEquals(1080f, requireNotNull(window.touch).let { (it.left + it.right).toFloat() }, 1f)
            assertEquals(720f, requireNotNull(window.touch).let { (it.top + it.bottom).toFloat() }, 1f)
        }

        val sideBySide = n3dsStageArrangement(DsLayout.SIDE_BY_SIDE, 1080, 720, fill = false, floating = true, framePx = 30f)
        assertEquals(DsLayout.SIDE_BY_SIDE, n3dsStageLayout(DsLayout.SIDE_BY_SIDE))
        assertEquals(com.pokedaisy.app.companion.DsLayouts.arrange(DsLayout.SIDE_BY_SIDE, 1080, 720, fill = false,
            topAspect = com.pokedaisy.app.companion.DsLayouts.N3DS_TOP_ASPECT), sideBySide)
    }

    @Test fun startupWaitUsesTheEffectiveSceneLayoutForOneScreenGames() {
        val setupLayout = n3dsStageLayout(DsLayout.ONE_SCREEN)
        assertEquals(DsLayout.ONE_SCREEN, setupLayout)
        assertFalse(n3dsTouchVisibleForLayout(setupLayout, manuallyOpened = false, autoWanted = false))
        assertTrue(n3dsTouchVisibleForLayout(setupLayout, manuallyOpened = false, autoWanted = true))
        assertTrue(n3dsTouchSurfaceRequired(stageOn = true, onSecondDisplay = false, touchVisible = true))

        val fieldLayout = n3dsStageLayout(DsLayout.ONE_SCREEN)
        assertEquals(DsLayout.ONE_SCREEN, fieldLayout)
        assertFalse(n3dsTouchVisibleForLayout(fieldLayout, manuallyOpened = false, autoWanted = false))
        assertTrue(n3dsTouchVisibleForLayout(fieldLayout, manuallyOpened = false, autoWanted = true))
    }

    @Test fun threeDsTouchWindowFrameTracksTheDsLikeOpenState() {
        assertFalse(n3dsTouchWindowFramed(DsLayout.ONE_SCREEN, windowOpen = false))
        assertTrue(n3dsTouchWindowFramed(DsLayout.ONE_SCREEN, windowOpen = true))
        assertTrue(n3dsTouchWindowFramed(DsLayout.PICTURE_IN_PICTURE, windowOpen = false))
        assertFalse(n3dsTouchWindowFramed(DsLayout.SIDE_BY_SIDE, windowOpen = false))
    }

    @Test fun threeDsFieldMenusAndBattleSubmenusRequestTheTouchDisplayLikeDs() {
        val bag = com.pokedaisy.app.companion.data.BATTLE_INPUT_BAG_OPEN
        val party = com.pokedaisy.app.companion.data.BATTLE_INPUT_PARTY_OPEN
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU, 0))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, 0))
        assertFalse(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, 0, fieldReaderAvailable = false))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN, 0))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE, bag))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE, party))
        assertTrue(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, bag))
        assertFalse(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.UNMAPPED, party))
        assertFalse(n3dsTouchWanted(com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE, 0))
    }

    @Test fun threeDsAutoTouchDismissClosesOnlyGameOpenedPages() {
        val menu = com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU
        val battle = com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE
        val bag = com.pokedaisy.app.companion.data.BATTLE_INPUT_BAG_OPEN
        val party = com.pokedaisy.app.companion.data.BATTLE_INPUT_PARTY_OPEN
        assertTrue(n3dsAutoTouchCanDismiss(menu, 0))
        assertTrue(n3dsAutoTouchCanDismiss(com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN, 0))
        assertTrue(n3dsAutoTouchCanDismiss(battle, bag))
        assertTrue(n3dsAutoTouchCanDismiss(battle, party))
        assertFalse(n3dsAutoTouchCanDismiss(battle, 0))
        assertFalse(n3dsAutoTouchCanDismiss(com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD, bag))
    }

    @Test fun threeDsScenesPreserveThePlayersChosenLayout() {
        for (layout in DsLayout.entries) assertEquals(layout, n3dsStageLayout(layout))
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
