package net.acrobits.interview.test.domain.model

/**
 * Domain model representing the different states of SIP registration.
 * This decouples the presentation layer from the SDK's specific enum.
 */
sealed class RegistrationState(val label: String) {
    object NotRegistered : RegistrationState("Not Registered")
    object Registering : RegistrationState("Registering...")
    object Registered : RegistrationState("Registered")
    object Unregistering : RegistrationState("Unregistering...")
    object Failed : RegistrationState("Registration Failed")
}