package com.pokedaisy.app.n3ds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque
import java.util.concurrent.Executor

class N3dsSamplingPipelineTest {
    @Test
    fun requestsDuringCaptureAndDecodeCoalesceIntoOneFollowUp() {
        val captureExecutor = ManualExecutor()
        val decodeExecutor = ManualExecutor()
        var captures = 0
        val delivered = mutableListOf<Int>()
        val pipeline = pipeline(captureExecutor, decodeExecutor, capture = { ++captures }, decode = { it }) {
            if (it is N3dsSampleResult.Success) delivered += it.value
        }

        pipeline.request()
        pipeline.request()
        pipeline.request()
        assertEquals(1, captureExecutor.size)
        captureExecutor.runNext()
        pipeline.request()
        pipeline.request()
        assertEquals(1, decodeExecutor.size)

        decodeExecutor.runNext()
        assertEquals(listOf(1), delivered)
        assertEquals(1, captureExecutor.size)
        captureExecutor.runNext()
        decodeExecutor.runNext()

        assertEquals(2, captures)
        assertEquals(listOf(1, 2), delivered)
        assertEquals(0, captureExecutor.size)
    }

    @Test
    fun closeDropsCapturedWorkBeforeDecodeAndDelivery() {
        val captureExecutor = ManualExecutor()
        val decodeExecutor = ManualExecutor()
        var decoded = false
        var delivered = false
        val pipeline = pipeline(captureExecutor, decodeExecutor, capture = { 7 }, decode = {
            decoded = true
            it
        }) { delivered = true }

        pipeline.request()
        captureExecutor.runNext()
        pipeline.close()
        decodeExecutor.runNext()

        assertFalse(decoded)
        assertFalse(delivered)
    }

    @Test
    fun staleGenerationDropsCapturedWorkBeforeDecodeAndDelivery() {
        val captureExecutor = ManualExecutor()
        val decodeExecutor = ManualExecutor()
        var generation = 4L
        var decoded = false
        var delivered = false
        val pipeline = N3dsSamplingPipeline(
            sessionGeneration = 4L,
            currentGeneration = { generation },
            captureExecutor = captureExecutor,
            decodeExecutor = decodeExecutor,
            capture = { 7 },
            decode = { decoded = true; it },
            deliver = { delivered = true },
        )

        pipeline.request()
        captureExecutor.runNext()
        generation++
        decodeExecutor.runNext()

        assertFalse(decoded)
        assertFalse(delivered)
    }

    @Test
    fun captureFailureIsTypedAndDoesNotInvokeDecoder() {
        val captureExecutor = ManualExecutor()
        val decodeExecutor = ManualExecutor()
        val failure = IllegalStateException("Azahar memory read failed")
        var decoded = false
        val outcomes = mutableListOf<N3dsSampleResult<Int>>()
        val pipeline = N3dsSamplingPipeline(
            sessionGeneration = 1L,
            currentGeneration = { 1L },
            captureExecutor = captureExecutor,
            decodeExecutor = decodeExecutor,
            capture = { throw failure },
            decode = { decoded = true; it },
            deliver = { outcomes += it },
        )

        pipeline.request()
        captureExecutor.runNext()
        assertEquals(1, decodeExecutor.size)
        decodeExecutor.runNext()

        assertFalse(decoded)
        assertEquals(1, outcomes.size)
        assertTrue(outcomes.single() is N3dsSampleResult.CaptureFailure)
        assertEquals(failure, (outcomes.single() as N3dsSampleResult.CaptureFailure).cause)
    }

    @Test
    fun decodeFailureIsTypedAndDoesNotPublishSuccess() {
        val captureExecutor = ManualExecutor()
        val decodeExecutor = ManualExecutor()
        val failure = IllegalArgumentException("Gen 6 sample was incomplete")
        val outcomes = mutableListOf<N3dsSampleResult<Int>>()
        val pipeline = N3dsSamplingPipeline(
            sessionGeneration = 1L,
            currentGeneration = { 1L },
            captureExecutor = captureExecutor,
            decodeExecutor = decodeExecutor,
            capture = { 7 },
            decode = { throw failure },
            deliver = { outcomes += it },
        )

        pipeline.request()
        captureExecutor.runNext()
        decodeExecutor.runNext()

        assertEquals(1, outcomes.size)
        assertTrue(outcomes.single() is N3dsSampleResult.DecodeFailure)
        assertEquals(failure, (outcomes.single() as N3dsSampleResult.DecodeFailure).cause)
    }

    @Test
    fun decodeAndDeliveryStayOrderedAcrossCoalescedSamples() {
        val captureExecutor = ManualExecutor()
        val decodeExecutor = ManualExecutor()
        var next = 0
        val events = mutableListOf<String>()
        val pipeline = N3dsSamplingPipeline(
            sessionGeneration = 2L,
            currentGeneration = { 2L },
            captureExecutor = captureExecutor,
            decodeExecutor = decodeExecutor,
            capture = { (++next).also { events += "capture$it" } },
            decode = { "value$it".also { events += "decode$it" } },
            deliver = { result ->
                if (result is N3dsSampleResult.Success) events += "deliver${result.value.removePrefix("value")}"
            },
        )

        pipeline.request()
        captureExecutor.runNext()
        pipeline.request()
        decodeExecutor.runNext()
        assertEquals(listOf("capture1", "decodevalue1", "deliver1"), events)
        captureExecutor.runNext()
        decodeExecutor.runNext()

        assertEquals(
            listOf("capture1", "decodevalue1", "deliver1", "capture2", "decodevalue2", "deliver2"),
            events,
        )
    }

    private fun <T> pipeline(
        captureExecutor: Executor,
        decodeExecutor: Executor,
        capture: () -> Int,
        decode: (Int) -> T,
        deliver: (N3dsSampleResult<T>) -> Unit,
    ) = N3dsSamplingPipeline(
        sessionGeneration = 1L,
        currentGeneration = { 1L },
        captureExecutor = captureExecutor,
        decodeExecutor = decodeExecutor,
        capture = capture,
        decode = decode,
        deliver = deliver,
    )

    private class ManualExecutor : Executor {
        private val tasks = ArrayDeque<Runnable>()
        val size: Int get() = tasks.size

        override fun execute(command: Runnable) {
            tasks.addLast(command)
        }

        fun runNext() = tasks.removeFirst().run()
    }
}
