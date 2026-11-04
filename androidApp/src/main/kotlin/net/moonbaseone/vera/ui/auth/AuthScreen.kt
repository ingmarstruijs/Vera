package net.moonbaseone.vera.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.auth.AuthStep
import net.moonbaseone.vera.shared.auth.AuthUiState
import net.moonbaseone.vera.shared.auth.AuthViewModel
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start(autoUnlock = true)
    }

    if (state.showSelfDestructDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(UiCopy.AUTH_SELF_DESTRUCT_TITLE) },
            text = { Text(UiCopy.AUTH_SELF_DESTRUCT_DONE) },
            confirmButton = {
                TextButton(onClick = viewModel::acknowledgeSelfDestruct) {
                    Text(UiCopy.OK)
                }
            },
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (state.step) {
            AuthStep.Checking -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            AuthStep.NewVault -> NewVaultContent(
                isBusy = state.isBusy,
                onCreate = viewModel::createVault,
            )
            AuthStep.Unlock, AuthStep.Unlocked -> UnlockContent(
                state = state,
                onUnlock = viewModel::unlockBiometric,
            )
        }
    }
}

@Composable
private fun NewVaultContent(isBusy: Boolean, onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Box(modifier = Modifier.height(32.dp))
        Text(
            text = UiCopy.AUTH_CREATE_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.height(16.dp))
        Text(
            text = UiCopy.AUTH_CREATE_BODY,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.height(40.dp))
        Button(
            onClick = onCreate,
            enabled = !isBusy,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(imageVector = Icons.Outlined.Security, contentDescription = null)
                Box(modifier = Modifier.width(8.dp))
                Text(UiCopy.AUTH_CREATE_BUTTON)
            }
        }
    }
}

@Composable
private fun UnlockContent(state: AuthUiState, onUnlock: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Fingerprint,
            contentDescription = UiCopy.AUTH_BIOMETRIC_PROMPT,
            modifier = Modifier.size(96.dp),
            tint = if (state.isBusy) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Box(modifier = Modifier.height(32.dp))
        Text(
            text = UiCopy.AUTH_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.height(12.dp))
        Text(
            text = UiCopy.AUTH_BIOMETRIC_PROMPT,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        state.errorMessage?.let { message ->
            Box(modifier = Modifier.height(20.dp))
            AuthErrorBanner(message = message, critical = state.attemptsLeft <= 1)
        }
        Box(modifier = Modifier.height(40.dp))
        Button(
            onClick = onUnlock,
            enabled = !state.isBusy && state.attemptsLeft > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics { contentDescription = UiCopy.AUTH_UNLOCK_BUTTON },
        ) {
            if (state.isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Box(modifier = Modifier.width(8.dp))
                Text(UiCopy.AUTH_LOADING)
            } else {
                Icon(imageVector = Icons.Outlined.Fingerprint, contentDescription = null)
                Box(modifier = Modifier.width(8.dp))
                Text(UiCopy.AUTH_UNLOCK_BUTTON)
            }
        }
    }
}

@Composable
private fun AuthErrorBanner(message: String, critical: Boolean) {
    val border = if (critical) Color(DesignTokens.ERROR_RED) else Color(DesignTokens.AMBER)
    val bg = if (critical) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (critical) Icons.Outlined.WarningAmber else Icons.Outlined.Info,
                contentDescription = null,
                tint = border,
            )
            Box(modifier = Modifier.width(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = border)
        }
    }
}
