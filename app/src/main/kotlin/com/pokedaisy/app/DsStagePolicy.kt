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
    (is3ds || isNds && !portrait) && !hasSecondScreen && !debugMirror && !companionOverGame && !folded

/** ONE SCREEN keeps the lower 3DS render hidden until the player asks for it; other selected layouts show it. */
internal fun n3dsTouchScreenVisible(layout: DsLayout, manuallyOpened: Boolean): Boolean =
    layout != DsLayout.ONE_SCREEN || manuallyOpened
