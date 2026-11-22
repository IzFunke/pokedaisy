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

/** The TOUCH SCREEN quick action, hotkey, and hint are available for either DS or 3DS floating layouts. */
internal fun touchScreenToggleAvailable(dsFloats: Boolean, n3dsFloats: Boolean): Boolean = dsFloats || n3dsFloats
