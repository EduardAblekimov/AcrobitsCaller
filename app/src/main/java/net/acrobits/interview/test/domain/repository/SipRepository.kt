package net.acrobits.interview.test.domain.repository

import net.acrobits.interview.test.domain.model.CallState
import net.acrobits.interview.test.domain.model.SipAccount
import kotlinx.coroutines.flow.Flow
import net.acrobits.interview.test.domain.model.RegistrationState

/**
 * The primary interface for the data layer. This contract defines how the domain layer
 * interacts with SIP-related data, abstracting the underlying data source.
 */
interface SipRepository {
    fun register(account: SipAccount)
    fun getRegistrationState(): Flow<RegistrationState>
    fun getCallState(): Flow<CallState>
    fun makeCall(number: String)
    fun hangup()
    fun setMute(isMuted: Boolean)
    fun setHold(isHeld: Boolean)
}