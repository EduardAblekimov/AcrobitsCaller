package net.acrobits.interview.test.domain.model

/**
 * Domain model representing the state of a call using a sealed class.
 * This provides a structured way to handle different call scenarios in the UI.
 */
sealed class CallState {
    object Idle : CallState()
    data class Connecting(val remoteParty: String, val callId: Long) : CallState()
    data class Ringing(val remoteParty: String, val callId: Long) : CallState()
    data class Active(val remoteParty: String, val callId: Long, val isMuted: Boolean, val isHeld: Boolean) : CallState()
    data class Disconnected(val reason: String) : CallState()
}