package net.acrobits.interview.test.data.sdk

import android.os.Handler
import android.os.Looper
import cz.acrobits.ali.Xml
import cz.acrobits.libsoftphone.Instance
import cz.acrobits.libsoftphone.account.AccountXml
import cz.acrobits.libsoftphone.data.Account
import cz.acrobits.libsoftphone.data.Call
import cz.acrobits.libsoftphone.data.RegistrationState as SdkRegistrationState
import cz.acrobits.libsoftphone.event.CallEvent
import cz.acrobits.libsoftphone.mergeable.MergeableNodeAttributes
import cz.acrobits.libsoftphone.support.Listeners
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.acrobits.interview.test.domain.model.CallState
import net.acrobits.interview.test.domain.model.CallState.Active
import net.acrobits.interview.test.domain.model.CallState.Connecting
import net.acrobits.interview.test.domain.model.CallState.Disconnected
import net.acrobits.interview.test.domain.model.CallState.Idle
import net.acrobits.interview.test.domain.model.CallState.Ringing
import net.acrobits.interview.test.domain.model.RegistrationState
import net.acrobits.interview.test.domain.model.SipAccount

/**
 * Implementation of the SipSdk interface that works with SDK API.
 * This class directly communicates with the SDK and  implements Listeners to receive the SDK events back.
 */
class SipSdkImpl : SipSdk,
    Listeners.OnRegistrationStateChanged,
    Listeners.OnCallStateChanged,
    Listeners.OnCallHoldStateChanged {

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.NotRegistered)
    override val registrationState = _registrationState.asStateFlow()

    private val _callState = MutableStateFlow<CallState>(Idle)
    override val callState = _callState.asStateFlow()

    private val _activeAccountId = MutableStateFlow<String?>(null)

    private var activeCallEvent: CallEvent? = null
    private val listeners = object : Listeners() {
        override fun getRingtone(p0: CallEvent) = null
    }

    init {
        Instance.setObserver(listeners)
        listeners.register(this)
    }

    override fun register(account: SipAccount) {
        val accountXml = Xml("account").apply {
            setAttribute(Account.Attributes.ID, account.displayName)
            setChildValue(Account.USERNAME, account.username)
            setChildValue(Account.PASSWORD, account.password)
            setChildValue(Account.HOST, "pbx.acrobits.cz")
        }
        _activeAccountId.value = account.displayName
        Instance.Registration.saveAccount(AccountXml(accountXml, MergeableNodeAttributes.gui()))
    }

    override fun makeCall(number: String) {
        val accountId = _activeAccountId.value ?: return
        if (_registrationState.value == RegistrationState.Registered) {
            val callEvent = CallEvent(/* accountId = */ accountId, /* uri = */ number)
            Instance.Events.post(callEvent)
        }
    }

    override fun hangup() {
        activeCallEvent?.let {
            Instance.Calls.hangup(it, null)
        }
    }

    override fun setMute(isMuted: Boolean) {
        Instance.Audio.setMuted(isMuted)
        val currentCallState = _callState.value
        if (currentCallState is Active) {
            _callState.update { currentCallState.copy(isMuted = isMuted) }
        }
    }

    override fun setHold(isHeld: Boolean) {
        activeCallEvent?.let {
            Instance.Calls.setHeld(it, isHeld)
        }
    }

    // --- Listeners Implementation ---

    override fun onRegistrationStateChanged(accountId: String?, state: SdkRegistrationState) {
        if (accountId == _activeAccountId.value) {
            when (state) {
                SdkRegistrationState.None,
                SdkRegistrationState.NotRegistered -> _registrationState.value = RegistrationState.NotRegistered
                SdkRegistrationState.Discovering,
                SdkRegistrationState.PushHandshake,
                SdkRegistrationState.Registering -> _registrationState.value = RegistrationState.Registering
                SdkRegistrationState.Registered -> _registrationState.value = RegistrationState.Registered
                SdkRegistrationState.Unregistering -> _registrationState.value = RegistrationState.Unregistering
                SdkRegistrationState.Unauthorized -> _registrationState.value = RegistrationState.Failed
                SdkRegistrationState.Error -> _registrationState.value = RegistrationState.Failed
            }
        }
    }

    override fun onCallStateChanged(event: CallEvent, state: Call.State) {
        if (activeCallEvent != null && event.eventId != activeCallEvent!!.eventId) {
            return
        }

        activeCallEvent = event

        when (state) {
            Call.State.Trying, Call.State.IncomingTrying -> {
                _callState.value = Connecting(event.getPartyIdentifier(), event.eventId)
            }

            Call.State.Ringing, Call.State.IncomingRinging -> {
                _callState.value = Ringing(event.getPartyIdentifier(), event.eventId)
            }

            Call.State.Established -> {
                val holdStates = Instance.Calls.isHeld(event)
                val isMuted = Instance.Audio.isMuted()
                _callState.value = Active(
                    remoteParty = event.getPartyIdentifier(),
                    callId = event.eventId,
                    isMuted = isMuted,
                    isHeld = holdStates.isLocallyHeld
                )
            }

            Call.State.Terminated,
            Call.State.Busy,
            Call.State.Error,
            Call.State.Unauthorized,
            Call.State.IncomingMissed,
            Call.State.IncomingRejected -> {
                _callState.value = Disconnected(state.label)
                activeCallEvent = null
                Handler(Looper.getMainLooper()).postDelayed({
                    if (_callState.value is Disconnected) {
                        _callState.value = Idle
                    }
                }, 2000)
            }

            else -> Unit
        }
    }

    /**
     * Safely gets a displayable identifier for the remote party from a CallEvent.
     * It prioritizes the display name and falls back to the URI.
     */
    private fun CallEvent.getPartyIdentifier(): String {
        val remoteUser = this.remoteUser ?: return "Unknown"
        if (!remoteUser.displayName.isNullOrBlank()) {
            return remoteUser.displayName ?: "Unknown"
        }
        return remoteUser.genericUri ?: "Unknown"
    }

    override fun onCallHoldStateChanged(event: CallEvent, holdStates: Call.HoldStates) {
        val currentCallState = _callState.value
        if (event.eventId == (currentCallState as? Active)?.callId) {
            _callState.update {
                (it as Active).copy(isHeld = holdStates.isLocallyHeld)
            }
        }
    }
}