package net.acrobits.interview.test.presentation.ui.dialer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.acrobits.interview.test.domain.model.SipAccount
import kotlinx.coroutines.flow.collectLatest
import net.acrobits.interview.test.domain.model.RegistrationState

@Composable
fun DialerScreen(
    viewModel: DialerViewModel,
    onNavigateToCall: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is DialerSideEffect.NavigateToCallScreen -> onNavigateToCall()
            }
        }
    }

    if (uiState.showAccountSelector) {
        AccountSelectionDialog(onAccountSelected = { viewModel.onEvent(DialerEvent.AccountSelected(it)) })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RegistrationStatusIndicator(state = uiState.registrationState, account = uiState.selectedAccount)
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(
                value = uiState.numberToDial,
                onValueChange = { viewModel.onEvent(DialerEvent.NumberChanged(it)) },
                label = { Text("Enter number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.onEvent(DialerEvent.DialClicked) },
            enabled = uiState.isDialButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Dial", fontSize = 18.sp)
        }
    }
}

@Composable
fun RegistrationStatusIndicator(state: RegistrationState, account: SipAccount?) {
    val color = when (state) {
        RegistrationState.Registering -> Color.Yellow
        RegistrationState.Registered -> Color.Green
        else -> Color.Red
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = account?.displayName ?: "No Account",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Text(text = state.label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AccountSelectionDialog(onAccountSelected: (SipAccount) -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Cannot be dismissed */ },
        title = { Text("Select Account") },
        text = { Text("Please choose a SIP account to register.") },
        confirmButton = {
            Column {
                Button(
                    onClick = {
                        onAccountSelected(
                            SipAccount(
                                "3100",
                                "misscom",
                                "User 3100"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("User 3100") }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        onAccountSelected(
                            SipAccount(
                                "3101",
                                "misscom",
                                "User 3101"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("User 3101") }
            }
        }
    )
}