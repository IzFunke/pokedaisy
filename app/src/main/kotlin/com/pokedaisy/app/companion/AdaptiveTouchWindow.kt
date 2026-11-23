package com.pokedaisy.app.companion

import kotlin.math.min

/** The shared DS and 3DS floating window's share of the stage, leaving room around the screen and frame. */
const val TOUCH_WINDOW_WIDTH_SHARE = 0.92f
const val TOUCH_WINDOW_HEIGHT_SHARE = 0.88f

/** The largest screen of [aspect] that fits within a stage's available content bounds. */
data class TouchWindowSize(val width: Float, val height: Float)

/** Shared by the DS companion window and 3DS floating touch display. */
fun fitTouchWindow(maxWidth: Float, maxHeight: Float, aspect: Float = DsLayouts.ASPECT): TouchWindowSize {
    val width = min(maxWidth, maxHeight * aspect)
    return TouchWindowSize(width, min(maxHeight, width / aspect))
}
