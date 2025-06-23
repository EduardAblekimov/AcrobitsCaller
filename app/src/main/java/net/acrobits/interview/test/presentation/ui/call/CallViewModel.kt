package net.acrobits.interview.test.presentation.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import net.acrobits.interview.test.core.utils.CallTimer
import net.acrobits.interview.test.domain.model.CallState
import net.acrobits.interview.test.domain.repository.SipRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallViewModel(
    private val repository: SipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CallSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private val timer = CallTimer()

    init {
        observeCallState()
        observeTimer()
    }

    private fun observeCallState() {
        repository.getCallState().onEach { state ->
            when (state) {
                is CallState.Active -> {
                    _uiState.update {
                        it.copy(
                            remoteParty = state.remoteParty,
                            status = "Active",
                            isMuted = state.isMuted,
                            isHeld = state.isHeld
                        )
                    }
                    // Call just started - start the timer
                    if (uiState.value.duration == 0L) timer.start()
                }
                is CallState.Connecting -> _uiState.update { it.copy(remoteParty = state.remoteParty, status = "Connecting...") }
                is CallState.Ringing -> _uiState.update { it.copy(remoteParty = state.remoteParty, status = "Ringing...") }
                is CallState.Disconnected -> {
                    _uiState.update { it.copy(status = "Disconnected: ${state.reason}") }
                    timer.stop()
                }
                is CallState.Idle -> {
                    _uiState.update { it.copy(status = "Idle") }
                    timer.stop()
                    viewModelScope.launch {
                        _sideEffect.emit(CallSideEffect.NavigateBack)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeTimer() {
        // collecting timer's flow to show the call duration
        timer.elapsedTime.onEach { time ->
            _uiState.update { it.copy(duration = time) }
        }.launchIn(viewModelScope)
    }

    // MVI approach - handling all screen's events in a single method
    fun onEvent(event: CallEvent) {
        when (event) {
            CallEvent.HangupClicked -> {
                repository.hangup()
            }

            CallEvent.ToggleHold -> {
                val isHeld = !_uiState.value.isHeld
                repository.setHold(isHeld)
                _uiState.update { it.copy(isHeld = isHeld) }
            }

            CallEvent.ToggleMute -> {
                val isMuted = !_uiState.value.isMuted
                repository.setMute(!_uiState.value.isMuted)
                _uiState.update { it.copy(isMuted = isMuted) }
            }
        }
    }

    override fun onCleared() {
        timer.reset()
        super.onCleared()
    }

    companion object {
        // need that as we use manual DI
        fun provideFactory(
            repository: SipRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CallViewModel(repository = repository) as T
            }
        }
    }
}

data class CallUiState(
    val remoteParty: String = "Unknown",
    val status: String = "Connecting...",
    val duration: Long = 0L,
    val isMuted: Boolean = false,
    val isHeld: Boolean = false
)

sealed class CallSideEffect {
    object NavigateBack : CallSideEffect()
}

sealed class CallEvent {
    object HangupClicked : CallEvent()
    object ToggleMute : CallEvent()
    object ToggleHold : CallEvent()
}