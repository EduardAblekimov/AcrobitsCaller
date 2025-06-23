package net.acrobits.interview.test.presentation.ui.call

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.acrobits.interview.test.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onCallEnded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is CallSideEffect.NavigateBack -> onCallEnded()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            CallInfo(
                remoteParty = uiState.remoteParty,
                status = uiState.status,
                duration = uiState.duration
            )

            CallControls(
                isMuted = uiState.isMuted,
                isHeld = uiState.isHeld,
                onMuteClick = { viewModel.onEvent(CallEvent.ToggleMute) },
                onHoldClick = { viewModel.onEvent(CallEvent.ToggleHold) },
                onHangupClick = { viewModel.onEvent(CallEvent.HangupClicked) }
            )
        }
    }
}

@Composable
fun CallInfo(remoteParty: String, status: String, duration: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = remoteParty,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = status,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatDuration(duration),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CallControls(
    isMuted: Boolean,
    isHeld: Boolean,
    onMuteClick: () -> Unit,
    onHoldClick: () -> Unit,
    onHangupClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlIconButton(
                onClick = onMuteClick,
                iconRes = R.drawable.ic_mic_off,
                label = "Mute",
                isToggled = isMuted
            )
            ControlIconButton(
                onClick = onHoldClick,
                iconRes = R.drawable.ic_pause,
                label = "Hold",
                isToggled = isHeld
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        FloatingActionButton(
            onClick = onHangupClick,
            containerColor = Color.Red,
            contentColor = Color.White,
            modifier = Modifier.size(72.dp),
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_call_end),
                contentDescription = "Hang up",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun ControlIconButton(
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    isToggled: Boolean
) {
    val backgroundColor = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isToggled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .background(backgroundColor, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = contentColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}