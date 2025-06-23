package net.acrobits.interview.test.data.sdk

import net.acrobits.interview.test.domain.model.CallState
import net.acrobits.interview.test.domain.model.SipAccount
import kotlinx.coroutines.flow.StateFlow
import net.acrobits.interview.test.domain.model.RegistrationState

/**
 * Interface abstracting the LibSoftphone SDK. This allows for easier testing
 * and isolates SDK-specific code from the rest of the application.
 */
interface SipSdk {
    val registrationState: StateFlow<RegistrationState>
    val callState: StateFlow<CallState>

    fun register(account: SipAccount)
    fun makeCall(number: String)
    fun hangup()
    fun setMute(isMuted: Boolean)
    fun setHold(isHeld: Boolean)
}