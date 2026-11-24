package com.pokedaisy.app.n3ds

import com.pokedaisy.app.companion.CoalescedSampleGate
import java.util.ArrayDeque
import java.util.concurrent.Executor

/** A single sample's typed result. Capture and decode failures remain distinguishable to the host. */
internal sealed interface N3dsSampleResult<out T> {
    data class Success<T>(val value: T) : N3dsSampleResult<T>
    data class CaptureFailure(val cause: Throwable) : N3dsSampleResult<Nothing>
    data class DecodeFailure(val cause: Throwable) : N3dsSampleResult<Nothing>
}

/**
 * Separates a host's blocking capture from serialized decode, following [com.pokedaisy.app.companion.GameSampler]'s
 * coalescing and close behavior. Capture runs on [captureExecutor]; decode and result delivery run in order on
 * [decodeExecutor]. Executors may be shared, but each stage is serialized by this class.
 *
 * While a capture/decode/delivery is in flight, any number of [request] calls collapse into one follow-up sample.
 * The generation callback prevents queued work from an old host session being decoded or delivered after replacement.
 */
internal class N3dsSamplingPipeline<C, T>(
    private val sessionGeneration: Long,
    private val currentGeneration: () -> Long,
    captureExecutor: Executor,
    decodeExecutor: Executor,
    private val capture: () -> C,
    private val decode: (C) -> T,
    private val deliver: (N3dsSampleResult<T>) -> Unit,
) {
    private val captureQueue = SerialExecutor(captureExecutor)
    private val decodeQueue = SerialExecutor(decodeExecutor)
    private val lock = Any()
    private val requests = CoalescedSampleGate()

    fun request() {
        if (!isCurrent()) return
        if (requests.request()) enqueueCapture()
    }

    /** Invalidates queued or in-flight work; a blocking host capture is allowed to return, but cannot publish. */
    fun close() {
        synchronized(lock) { requests.close() }
    }

    private fun enqueueCapture() {
        captureQueue.execute {
            if (!isActive()) {
                finishSample()
                return@execute
            }
            val captured = try {
                capture()
            } catch (error: Throwable) {
                decodeQueue.execute { complete(N3dsSampleResult.CaptureFailure(error)) }
                return@execute
            }
            decodeQueue.execute {
                val result = if (!isActive()) null else try {
                    N3dsSampleResult.Success(decode(captured))
                } catch (error: Throwable) {
                    N3dsSampleResult.DecodeFailure(error)
                }
                if (result != null) complete(result) else finishSample()
            }
        }
    }

    private fun complete(result: N3dsSampleResult<T>) {
        try {
            synchronized(lock) {
                if (!requests.isClosed && isCurrent()) deliver(result)
            }
        } finally {
            finishSample()
        }
    }

    private fun finishSample() {
        val runAgain = requests.finish(allowFollowUp = isCurrent())
        if (runAgain) enqueueCapture()
    }

    private fun isCurrent(): Boolean = currentGeneration() == sessionGeneration

    private fun isActive(): Boolean = !requests.isClosed && isCurrent()
}

/** Serializes tasks even when a host supplies a shared or multi-threaded executor. */
private class SerialExecutor(private val delegate: Executor) : Executor {
    private val tasks = ArrayDeque<Runnable>()
    private var active = false

    override fun execute(command: Runnable) {
        synchronized(this) {
            tasks.addLast(Runnable {
                try {
                    command.run()
                } finally {
                    scheduleNext()
                }
            })
            if (!active) scheduleNextLocked()
        }
    }

    private fun scheduleNext() {
        synchronized(this) { scheduleNextLocked() }
    }

    private fun scheduleNextLocked() {
        val next = tasks.pollFirst()
        if (next == null) {
            active = false
            return
        }
        active = true
        try {
            delegate.execute(next)
        } catch (error: Throwable) {
            active = false
            tasks.clear()
            throw error
        }
    }
}
