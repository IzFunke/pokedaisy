package com.pokedaisy.app.companion

import com.pokedaisy.app.companion.data.SnapshotView
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.ArrayDeque
import kotlin.coroutines.CoroutineContext

class GameSamplerTest {
    @Test
    fun requestDuringGameDeliveryIsCoalescedUntilThePriorSnapshotIsDelivered() {
        val worker = ManualDispatcher()
        val game = ManualDispatcher()
        val delivered = mutableListOf<Long>()
        var prepared = 0
        val sampler = GameSampler(
            prepare = {
                val frame = ++prepared
                val snapshot: () -> SnapshotView = { SnapshotView(connected = true, frameCounter = frame.toLong()) }
                snapshot
            },
            current = { true },
            game = game,
            after = { delivered += it.frameCounter },
            worker = worker,
        )

        sampler.request()
        assertEquals(1, prepared)
        worker.runNext()
        assertEquals(1, game.size)

        // The decode is done, but finish() has not yet run on the game dispatcher.
        sampler.request()
        sampler.request()
        assertEquals("a second capture must wait until delivery completes", 1, prepared)

        game.runNext()
        assertEquals(listOf(1L), delivered)
        assertEquals("the two requests become one follow-up", 2, prepared)
        worker.runNext()
        game.runNext()
        assertEquals(listOf(1L, 2L), delivered)

        sampler.close()
    }

    @Test
    fun requestQueuedDuringFailedCaptureRunsOneFollowUp() {
        val worker = ManualDispatcher()
        val game = ManualDispatcher()
        val delivered = mutableListOf<Long>()
        var prepared = 0
        lateinit var sampler: GameSampler
        sampler = GameSampler(
            prepare = {
                if (++prepared == 1) {
                    sampler.request()
                    null
                } else {
                    { SnapshotView(connected = true, frameCounter = prepared.toLong()) }
                }
            },
            current = { true },
            game = game,
            after = { delivered += it.frameCounter },
            worker = worker,
        )

        sampler.request()
        assertEquals(2, prepared)
        worker.runNext()
        game.runNext()

        assertEquals(listOf(2L), delivered)
        sampler.close()
    }

    private class ManualDispatcher : CoroutineDispatcher() {
        private val tasks = ArrayDeque<Runnable>()
        val size: Int get() = synchronized(tasks) { tasks.size }

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            synchronized(tasks) { tasks.addLast(block) }
        }

        fun runNext() {
            val next = synchronized(tasks) { tasks.removeFirst() }
            next.run()
        }
    }
}
