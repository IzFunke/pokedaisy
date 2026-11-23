package com.pokedaisy.app.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveTouchWindowTest {
    @Test fun fitsTheSameFourByThreeWindowInPortraitAndLandscapeStages() {
        listOf(360f to 800f, 800f to 360f, 412f to 915f, 915f to 412f).forEach { (width, height) ->
            val fit = fitTouchWindow(width, height)
            assertTrue(fit.width <= width)
            assertTrue(fit.height <= height)
            assertEquals(4f / 3f, fit.width / fit.height, 0.0001f)
        }
    }

    @Test fun adaptsToAnAspectRatioAndRespectsAvailableFrameBounds() {
        val availableWidth = 600f - 24f
        val availableHeight = 900f - 48f
        val fit = fitTouchWindow(availableWidth, availableHeight, DsLayouts.N3DS_TOP_ASPECT)
        assertEquals(576f, fit.width, 0.001f)
        assertEquals(345.6f, fit.height, 0.01f)
        assertTrue(fit.width + 24f <= 600f)
        assertTrue(fit.height + 48f <= 900f)
    }

    @Test fun usesHeightWhenLandscapeIsTheLimitingDimension() {
        val fit = fitTouchWindow(800f, 300f)
        assertEquals(400f, fit.width, 0.001f)
        assertEquals(300f, fit.height, 0.001f)
    }

    @Test fun dsWindowFitMatchesItsPreviousFourByThreeFormula() {
        listOf(390f to 844f, 844f to 390f, 720f to 720f).forEach { (stageWidth, stageHeight) ->
            val maxWidth = stageWidth * 0.92f - 6f
            val maxHeight = stageHeight * 0.88f - 6f
            val expectedWidth = minOf(maxWidth, maxHeight * 4f / 3f)
            val expectedHeight = expectedWidth * 3f / 4f
            val fit = fitTouchWindow(maxWidth, maxHeight)
            assertEquals(expectedWidth, fit.width, 0.001f)
            assertEquals(expectedHeight, fit.height, 0.001f)
        }
    }
}
