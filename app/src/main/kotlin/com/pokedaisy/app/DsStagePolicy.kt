package com.pokedaisy.app

import com.pokedaisy.app.companion.DsArrangement
import com.pokedaisy.app.companion.DsBox
import com.pokedaisy.app.companion.DsLayout
import com.pokedaisy.app.companion.DsLayouts
import com.pokedaisy.app.companion.TOUCH_WINDOW_HEIGHT_SHARE
import com.pokedaisy.app.companion.TOUCH_WINDOW_WIDTH_SHARE
import com.pokedaisy.app.companion.fitTouchWindow

/** Whether the app should arrange a Nintendo game's screens on this single display. */
internal fun dsStageEnabled(
    isNds: Boolean,
    is3ds: Boolean,
    portrait: Boolean,
    hasSecondScreen: Boolean,
    debugMirror: Boolean,
    companionOverGame: Boolean,
    folded: Boolean,
): Boolean =
    (is3ds || isNds && !portrait) && !hasSecondScreen && !debugMirror && (!companionOverGame || is3ds) && !folded

/** A page that uses both displays temporarily puts a floating layout side by side, preserving [chosen]. */
internal fun dsStageLayout(twoScreenPage: Boolean, chosen: DsLayout): DsLayout =
    if (twoScreenPage && chosen.floats) DsLayout.SIDE_BY_SIDE else chosen

/** 3DS full-screen pages use the same two-display layout transition as DS menu pages. */
internal fun n3dsStageLayout(
    chosen: DsLayout,
    scene: com.pokedaisy.app.n3ds.Gen6Reader.Scene? = null,
): DsLayout = dsStageLayout(
    twoScreenPage = scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN,
    chosen = chosen,
)

/** ONE SCREEN keeps the lower 3DS render hidden until the player asks for it; other selected layouts show it. */
internal fun n3dsTouchScreenVisible(layout: DsLayout, manuallyOpened: Boolean): Boolean =
    layout != DsLayout.ONE_SCREEN || manuallyOpened

/** Whether this screen's 4:3 output is visible after both the selected layout and a game-requested presentation apply. */
internal fun n3dsTouchVisibleForLayout(layout: DsLayout, manuallyOpened: Boolean, autoWanted: Boolean): Boolean =
    n3dsTouchScreenVisible(layout, manuallyOpened) || autoWanted

/** 3DS lower-screen scenes; module-only field observations are not touch requests. */
internal fun n3dsTouchWanted(
    scene: com.pokedaisy.app.n3ds.Gen6Reader.Scene,
    battleInput: Int,
    fieldReaderAvailable: Boolean = true,
): Boolean =
    (scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD && fieldReaderAvailable) ||
        scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU ||
        // Pages rendered by the game on both screens (party, bag, main menu) also need the lower display in ONE SCREEN.
        scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN ||
        scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE &&
        battleInput in setOf(
            com.pokedaisy.app.companion.data.BATTLE_INPUT_BAG_OPEN,
            com.pokedaisy.app.companion.data.BATTLE_INPUT_PARTY_OPEN,
        )

/** The dimmed-area close gesture exits a field menu; a hand-opened or other automatically presented window closes locally. */
internal fun n3dsAutoTouchCanDismiss(scene: com.pokedaisy.app.n3ds.Gen6Reader.Scene, battleInput: Int): Boolean =
    scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.MENU ||
        scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.SCREEN ||
        scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.BATTLE &&
        battleInput in setOf(
            com.pokedaisy.app.companion.data.BATTLE_INPUT_BAG_OPEN,
            com.pokedaisy.app.companion.data.BATTLE_INPUT_PARTY_OPEN,
        )

/** Pure stage geometry for a 3DS game's screens; [framePx] is the total frame width/height around its touch surface. */
internal fun n3dsStageArrangement(
    layout: DsLayout,
    width: Int,
    height: Int,
    fill: Boolean,
    floating: Boolean,
    framePx: Float,
): DsArrangement {
    val topAspect = DsLayouts.N3DS_TOP_ASPECT
    val touchAspect = DsLayouts.ASPECT
    if (layout == DsLayout.ONE_SCREEN || layout == DsLayout.PICTURE_IN_PICTURE && floating) {
        val alone = DsLayouts.arrange(layout, width, height, fill, topAspect, touchAspect).top
        val fit = fitTouchWindow(
            width * TOUCH_WINDOW_WIDTH_SHARE - framePx,
            height * TOUCH_WINDOW_HEIGHT_SHARE - framePx,
            touchAspect,
        )
        val touchWidth = fit.width.toInt()
        val touchHeight = fit.height.toInt()
        val x = (width - touchWidth) / 2
        val y = (height - touchHeight) / 2
        return DsArrangement(
            alone,
            DsBox(x, y, x + touchWidth, y + touchHeight),
        )
    }
    return DsLayouts.arrange(layout, width, height, fill, topAspect, touchAspect)
}

/** A closed ONE SCREEN has no frame; PIP keeps its always-visible inset framed, like the DS touch layout. */
internal fun n3dsTouchWindowFramed(layout: DsLayout, windowOpen: Boolean): Boolean =
    layout == DsLayout.PICTURE_IN_PICTURE || layout == DsLayout.ONE_SCREEN && windowOpen

/** Azahar waits for a lower surface only when that screen is actually visible; ONE SCREEN opens it on demand. */
internal fun n3dsTouchSurfaceRequired(stageOn: Boolean, onSecondDisplay: Boolean, touchVisible: Boolean): Boolean =
    onSecondDisplay || stageOn && touchVisible

/** The TOUCH SCREEN quick action, hotkey, and hint are available for either DS or 3DS floating layouts. */
internal fun touchScreenToggleAvailable(dsFloats: Boolean, n3dsFloats: Boolean): Boolean = dsFloats || n3dsFloats
