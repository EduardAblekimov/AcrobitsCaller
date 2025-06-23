package net.acrobits.interview.test.core.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple timer class to track call duration using coroutines.
 */
class CallTimer {

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private var timerJob: Job? = null

    fun start() {
        if (timerJob?.isActive == true) return // Already running

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                _elapsedTime.value = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }

    fun reset() {
        stop()
        _elapsedTime.value = 0L
    }
}