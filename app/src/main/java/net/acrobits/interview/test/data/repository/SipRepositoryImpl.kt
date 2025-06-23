package net.acrobits.interview.test.data.repository

import net.acrobits.interview.test.data.sdk.SipSdk
import net.acrobits.interview.test.domain.model.CallState
import net.acrobits.interview.test.domain.model.SipAccount
import net.acrobits.interview.test.domain.repository.SipRepository
import kotlinx.coroutines.flow.Flow
import net.acrobits.interview.test.domain.model.RegistrationState

/**
 * Implementation of the SipRepository. It uses the SipSdk wrapper to communicate
 * with the SDK and maps SDK-specific models to domain models.
 */
class SipRepositoryImpl(private val sipSdk: SipSdk) : SipRepository {
    override fun register(account: SipAccount) {
        sipSdk.register(account)
    }

    override fun getRegistrationState(): Flow<RegistrationState> {
        return sipSdk.registrationState
    }

    override fun getCallState(): Flow<CallState> {
        return sipSdk.callState
    }

    override fun makeCall(number: String) {
        sipSdk.makeCall(number)
    }

    override fun hangup() {
        sipSdk.hangup()
    }

    override fun setMute(isMuted: Boolean) {
        sipSdk.setMute(isMuted)
    }

    override fun setHold(isHeld: Boolean) {
        sipSdk.setHold(isHeld)
    }
}