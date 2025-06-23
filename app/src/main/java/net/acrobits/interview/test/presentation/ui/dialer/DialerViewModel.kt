package net.acrobits.interview.test.presentation.ui.dialer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import net.acrobits.interview.test.domain.model.SipAccount
import net.acrobits.interview.test.domain.repository.SipRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.acrobits.interview.test.domain.model.RegistrationState

class DialerViewModel(
    private val repository: SipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DialerUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<DialerSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        observeRegistrationState()
    }

    private fun observeRegistrationState() {
        repository.getRegistrationState().onEach { state ->
            _uiState.update {
                it.copy(
                    registrationState = state,
                    isDialButtonEnabled = state == RegistrationState.Registered && it.numberToDial.isNotBlank()
                )
            }
        }.launchIn(viewModelScope)
    }

    // MVI approach - handling all screen's events in a single method
    fun onEvent(event: DialerEvent) {
        when (event) {
            is DialerEvent.NumberChanged -> {
                val number = event.number.filter { it.isDigit() || it in "+*#" } // simple number validation
                _uiState.update {
                    it.copy(
                        numberToDial = number,
                        isDialButtonEnabled = it.registrationState == RegistrationState.Registered && number.isNotBlank()
                    )
                }
            }
            is DialerEvent.AccountSelected -> {
                _uiState.update { it.copy(selectedAccount = event.account, showAccountSelector = false) }
                repository.register(event.account) // register SIP account when chosen
            }
            DialerEvent.DialClicked -> {
                if (_uiState.value.isDialButtonEnabled) {
                    repository.makeCall(_uiState.value.numberToDial)
                    viewModelScope.launch {
                        _sideEffect.emit(DialerSideEffect.NavigateToCallScreen)
                    }
                }
            }
        }
    }

    companion object {
        // need that as we use manual DI
        fun provideFactory(
            repository: SipRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DialerViewModel(repository = repository) as T
            }
        }
    }
}

data class DialerUiState(
    val registrationState: RegistrationState = RegistrationState.NotRegistered,
    val numberToDial: String = "",
    val selectedAccount: SipAccount? = null,
    val isDialButtonEnabled: Boolean = false,
    val showAccountSelector: Boolean = true
)

sealed class DialerEvent {
    data class NumberChanged(val number: String) : DialerEvent()
    data class AccountSelected(val account: SipAccount) : DialerEvent()
    object DialClicked : DialerEvent()
}

sealed class DialerSideEffect {
    object NavigateToCallScreen : DialerSideEffect()
}