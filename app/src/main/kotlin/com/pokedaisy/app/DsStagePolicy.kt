package com.pokedaisy.app

import com.pokedaisy.app.companion.DsLayout

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

/** NDS scene tracking may request both screens for its menus; it must never override a 3DS player's chosen layout. */
internal fun dsStageLayout(is3ds: Boolean, dsMenuUp: Boolean, chosen: DsLayout): DsLayout =
    if (!is3ds && dsMenuUp && chosen.floats) DsLayout.SIDE_BY_SIDE else chosen

/** ONE SCREEN keeps the lower 3DS render hidden until the player asks for it; other selected layouts show it. */
internal fun n3dsTouchScreenVisible(layout: DsLayout, manuallyOpened: Boolean): Boolean =
    layout != DsLayout.ONE_SCREEN || manuallyOpened

/** Whether this screen's 4:3 output is visible after both the selected layout and a game-requested presentation apply. */
internal fun n3dsTouchVisibleForLayout(layout: DsLayout, manuallyOpened: Boolean, autoWanted: Boolean): Boolean =
    n3dsTouchScreenVisible(layout, manuallyOpened) || autoWanted

/** 3DS scenes that need Azahar's lower screen, matching the DS auto-window triggers. */
internal fun n3dsTouchWanted(scene: com.pokedaisy.app.n3ds.Gen6Reader.Scene, battleInput: Int): Boolean =
    scene == com.pokedaisy.app.n3ds.Gen6Reader.Scene.FIELD ||
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

/** 3DS scenes never override the player's selected presentation; automatic touch-screen scenes float over ONE SCREEN. */
internal fun n3dsStageLayout(chosen: DsLayout): DsLayout = chosen

/** A closed ONE SCREEN has no frame; PIP keeps its always-visible inset framed, like the DS touch layout. */
internal fun n3dsTouchWindowFramed(layout: DsLayout, windowOpen: Boolean): Boolean =
    layout == DsLayout.PICTURE_IN_PICTURE || layout == DsLayout.ONE_SCREEN && windowOpen

/** Azahar waits for a lower surface only when that screen is actually visible; ONE SCREEN opens it on demand. */
internal fun n3dsTouchSurfaceRequired(stageOn: Boolean, onSecondDisplay: Boolean, touchVisible: Boolean): Boolean =
    onSecondDisplay || stageOn && touchVisible

/** The TOUCH SCREEN quick action, hotkey, and hint are available for either DS or 3DS floating layouts. */
internal fun touchScreenToggleAvailable(dsFloats: Boolean, n3dsFloats: Boolean): Boolean = dsFloats || n3dsFloats
