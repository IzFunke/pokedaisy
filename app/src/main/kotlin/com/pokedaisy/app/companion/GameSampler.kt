package com.pokedaisy.app.companion

import com.pokedaisy.app.companion.data.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*

/** Captures on the game thread, decodes one immutable RAM image at a time, then delivers back there.
 * A burst of requests is coalesced into one follow-up; closing discards pending delivery. */
class GameSampler(
    private val prepare: () -> (() -> SnapshotView?)?,
    private val current: () -> Boolean,
    private val game: CoroutineDispatcher,
    private val after: (SnapshotView) -> Unit,
    private val asynchronous: Boolean = true,
    worker: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + worker)
    private val closed = atomic(false)
    /** IDLE, READING, or READING_AGAIN. READING includes delivery on [game], not just worker decode. */
    private val readState = atomic(STATE_IDLE)
    private var timer: Job? = null

    fun request() {
        if (closed.value || !current()) return
        if (!claimRead()) return
        sample()
    }

    /** Claim one sample, or remember exactly one follow-up while capture, decode, or delivery is active. */
    private fun claimRead(): Boolean {
        while (true) {
            when (readState.value) {
                STATE_IDLE -> if (readState.compareAndSet(STATE_IDLE, STATE_READING)) return true
                STATE_READING -> if (readState.compareAndSet(STATE_READING, STATE_READING_AGAIN)) return false
                STATE_READING_AGAIN -> return false
            }
        }
    }

    private fun sample() {
        if (closed.value || !current()) return finish(null)
        val work = runCatching(prepare).onFailure { Logs.i("sample capture failed: $it") }.getOrNull()
        if (work == null) return finish(null)
        fun decode(): SnapshotView? = try {
            if (closed.value) null else work()
        } catch (t: Throwable) {
            Logs.i("sample failed: $t"); null
        }
        if (!asynchronous) finish(decode())
        else scope.launch { val value = decode(); withContext(game) { finish(value) } }
    }

    private fun finish(value: SnapshotView?) {
        try {
            if (!closed.value && current()) value?.let(after)
        } finally {
            finishRead()
        }
    }

    /** Called on the game dispatcher after delivery; a queued request becomes the next sample without an idle gap. */
    private fun finishRead() {
        while (true) {
            if (closed.value || !current()) {
                readState.value = STATE_IDLE
                return
            }
            when (readState.value) {
                STATE_READING_AGAIN -> if (readState.compareAndSet(STATE_READING_AGAIN, STATE_READING)) {
                    sample()
                    return
                }
                STATE_READING -> if (readState.compareAndSet(STATE_READING, STATE_IDLE)) return
                STATE_IDLE -> return
            }
        }
    }

    /** Hosts with a frame-based cadence call [request] themselves; iOS uses this foreground-safe timer. */
    fun startPeriodic(millis: Long) {
        require(millis > 0)
        timer?.cancel()
        timer = scope.launch {
            while (isActive) { delay(millis); withContext(game) { request() } }
        }
    }

    fun close() { closed.value = true; timer?.cancel(); scope.cancel() }

    companion object {
        private const val STATE_IDLE = 0
        private const val STATE_READING = 1
        private const val STATE_READING_AGAIN = 2

        fun forGame(telemetry: TelemetryStore, gameBoy: Boolean, nds: Boolean = false,
                    game: CoroutineDispatcher, current: () -> Boolean, after: (SnapshotView) -> Unit): GameSampler {
            val buffers = if (gameBoy) null else RamSnapshot.Buffers(nds)
            return GameSampler(prepare = {
                if (gameBoy) { { telemetry.refresh() } }
                else RamSnapshot.capture(buffers!!)?.let(telemetry::deferredRefresh)
            }, current, game, after, asynchronous = !gameBoy)
        }
    }
}
