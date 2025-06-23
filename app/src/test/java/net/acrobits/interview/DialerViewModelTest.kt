package net.acrobits.interview

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.acrobits.interview.test.domain.model.CallState
import net.acrobits.interview.test.domain.model.RegistrationState
import net.acrobits.interview.test.domain.model.SipAccount
import net.acrobits.interview.test.domain.repository.SipRepository
import net.acrobits.interview.test.presentation.ui.dialer.DialerEvent
import net.acrobits.interview.test.presentation.ui.dialer.DialerViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DialerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DialerViewModel
    private lateinit var sipRepository: SipRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sipRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        // Given
        coEvery { sipRepository.getRegistrationState() } returns flowOf(RegistrationState.NotRegistered)
        coEvery { sipRepository.getCallState() } returns flowOf(CallState.Idle)

        // When
        viewModel = DialerViewModel(sipRepository)

        // Then
        val state = viewModel.uiState.value
        assertEquals(RegistrationState.NotRegistered, state.registrationState)
        assertEquals("", state.numberToDial)
        assertFalse(state.isDialButtonEnabled)
        assertTrue(state.showAccountSelector)
    }

    @Test
    fun `when account selected, registration is triggered and UI updated`() = runTest {
        // Given
        val account = SipAccount("3100", "pass", "User 3100")
        coEvery { sipRepository.getRegistrationState() } returns flowOf(RegistrationState.Registering)
        coEvery { sipRepository.getCallState() } returns flowOf(CallState.Idle)
        coEvery { sipRepository.register(account) } returns Unit
        viewModel = DialerViewModel(sipRepository)

        // When
        viewModel.onEvent(DialerEvent.AccountSelected(account))
        testDispatcher.scheduler.advanceUntilIdle() // Let the coroutine run

        // Then
        coVerify { sipRepository.register(account) }
        val state = viewModel.uiState.value
        assertEquals(account, state.selectedAccount)
        assertFalse(state.showAccountSelector)
        assertEquals(RegistrationState.Registering, state.registrationState)
    }

    @Test
    fun `dial button is enabled only when registered and number is not blank`() = runTest {
        // Given
        coEvery { sipRepository.getRegistrationState() } returns flowOf(RegistrationState.NotRegistered)
        coEvery { sipRepository.getCallState() } returns flowOf(CallState.Idle)
        viewModel = DialerViewModel(sipRepository)

        // Initial state: not enabled
        assertFalse(viewModel.uiState.value.isDialButtonEnabled)

        // When number is entered but not registered
        viewModel.onEvent(DialerEvent.NumberChanged("12345"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isDialButtonEnabled)

        // When registered but number is blank
        coEvery { sipRepository.getRegistrationState() } returns flowOf(RegistrationState.Registered)
        viewModel = DialerViewModel(sipRepository) // re-create to pick up new flow
        viewModel.onEvent(DialerEvent.NumberChanged(""))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isDialButtonEnabled)

        // When registered AND number is entered
        viewModel.onEvent(DialerEvent.NumberChanged("12345"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isDialButtonEnabled)
    }

    @Test
    fun `when dial clicked with valid state, makeCallUseCase is called`() = runTest {
        // Given
        val number = "12345"
        coEvery { sipRepository.getRegistrationState() } returns flowOf(RegistrationState.Registered)
        coEvery { sipRepository.getCallState() } returns flowOf(CallState.Idle)
        coEvery { sipRepository.makeCall(number) } returns Unit
        viewModel = DialerViewModel(sipRepository)
        viewModel.onEvent(DialerEvent.NumberChanged(number))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onEvent(DialerEvent.DialClicked)

        // Then
        coVerify { sipRepository.makeCall(number) }
    }
}