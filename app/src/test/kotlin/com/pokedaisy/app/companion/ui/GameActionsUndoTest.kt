package com.pokedaisy.app.companion.ui

import com.pokedaisy.app.Hotkeys
import com.pokedaisy.app.companion.GameActions
import com.pokedaisy.app.companion.StateSlots
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameActionsUndoTest {
    private class Slots(override val undoSupported: Boolean) : StateSlots {
        var undoSaves = 0
        var undoLoads = 0
        override fun list() = emptyList<StateSlots.Slot>()
        override val currentIndex = 0
        override fun requestSave(index: Int) = Unit
        override fun requestLoad(index: Int) = Unit
        override fun requestDelete(index: Int) = Unit
        override fun requestUndoSave() { undoSaves++ }
        override fun requestUndoLoad() { undoLoads++ }
    }

    @Test fun unsupportedCoreDoesNotClaimUndoHotkeys() {
        val slots = Slots(undoSupported = false)
        val actions = GameActions(SampleCompanion.Settings(), slots, {}, { _, _ -> }, {})

        assertFalse(actions.hotkey(Hotkeys.Event(Hotkeys.Action.UNDO_SAVE, pressed = true)))
        assertFalse(actions.hotkey(Hotkeys.Event(Hotkeys.Action.UNDO_LOAD, pressed = true)))
        assertEquals(0, slots.undoSaves)
        assertEquals(0, slots.undoLoads)
    }

    @Test fun sharedSlotControllerStillRunsUndoForCoresThatSupportIt() {
        val slots = Slots(undoSupported = true)
        val actions = GameActions(SampleCompanion.Settings(), slots, {}, { _, _ -> }, {})

        assertTrue(actions.hotkey(Hotkeys.Event(Hotkeys.Action.UNDO_SAVE, pressed = true)))
        assertTrue(actions.hotkey(Hotkeys.Event(Hotkeys.Action.UNDO_LOAD, pressed = true)))
        assertEquals(1, slots.undoSaves)
        assertEquals(1, slots.undoLoads)
    }
}
