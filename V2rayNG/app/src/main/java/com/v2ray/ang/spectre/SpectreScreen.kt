package com.v2ray.ang.spectre

import androidx.compose.foundation.background
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val SpectreBlack = Color(0xFF080B0A)
private val SpectreCard = Color(0xFF141A17)
private val SpectreGreen = Color(0xFF19E68C)
private val SpectreWhite = Color(0xFFF5F7F6)
private val SpectreMuted = Color(0xFF9AA7A0)
private val SpectreRed = Color(0xFFFF6B6B)

/**
 * Écran de connexion et espace client SpectreVPN.
 *
 * Cet écran reçoit son état du ViewModel.
 * Il ne modifie pas directement le moteur Xray.
 */
@Composable
fun SpectreScreen(
    viewModel: SpectreViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var token by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SpectreBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = SpectreCard,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    color = SpectreGreen,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "SPECTREVPN",
                color = SpectreWhite,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "TA CONNEXION. TON UNIVERS.",
                color = SpectreGreen,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(30.dp))

            if (!state.isConnected || state.client == null) {
                LoginCard(
                    token = token,
                    onTokenChange = { token = it },
                    isLoading = state.isLoading,
                    error = state.error,
                    onLogin = { viewModel.login(token) }
                )
            } else {
                ClientCard(
                    client = state.client!!,
                    host = state.host,
                    onRefresh = { viewModel.refreshHost() },
                    onLogout = {
                        token = ""
                        viewModel.logout()
                    }
                )

                state.message?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = SpectreGreen
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "SPECTRE TECH • CAMEROUN 🇨🇲",
                color = SpectreMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun LoginCard(
    token: String,
    onTokenChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SpectreCard,
                RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "Bienvenue 👋",
            color = SpectreWhite,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Entre ton token pour accéder à ton espace client.",
            color = SpectreMuted,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Token client") },
            placeholder = { Text("Colle ton token ici") },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SpectreWhite,
                unfocusedTextColor = SpectreWhite,
                focusedBorderColor = SpectreGreen,
                unfocusedBorderColor = Color(0xFF46524B),
                focusedLabelColor = SpectreGreen,
                unfocusedLabelColor = SpectreMuted,
                cursorColor = SpectreGreen
            )
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onLogin,
            enabled = !isLoading && token.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpectreGreen,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF35443B),
                disabledContentColor = SpectreMuted
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "VÉRIFIER MON COMPTE",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = error,
                color = SpectreRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ClientCard(
    client: SpectreClientInfo,
    host: String?,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    val quota = client.quotaOctets
    val used = client.consommationOctets.coerceAtLeast(0L)

    val progress = if (quota > 0L) {
        (used.toDouble() / quota.toDouble())
            .toFloat()
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                SpectreCard,
                RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "MON ESPACE",
            color = SpectreGreen,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = client.nom.ifBlank { "Client SpectreVPN" },
            color = SpectreWhite,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = client.matricule,
            color = SpectreMuted,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(20.dp))

        InfoRow("Forfait", client.forfait)
        InfoRow("Code", client.codeAbonnement)
        InfoRow(
            "Expiration",
            formatExpiration(client.dateExpiration)
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "CONSOMMATION",
            color = SpectreGreen,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        if (quota > 0L) {
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = SpectreGreen,
                trackColor = Color(0xFF344139)
            )

            Spacer(Modifier.height(10.dp))

            InfoRow("Utilisé", formatGo(used))
            InfoRow("Total", formatGo(quota))

            val remaining =
                (quota - used).coerceAtLeast(0L)

            InfoRow("Restant", formatGo(remaining))
        } else {
            Text(
                text = "Forfait illimité",
                color = SpectreWhite,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "La consommation réelle sera affichée lorsque les statistiques Xray seront intégrées.",
                color = SpectreMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "SERVEUR",
            color = SpectreGreen,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = host ?: "Host non récupéré",
            color = SpectreWhite,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpectreGreen,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "ACTUALISER LE SERVEUR",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF29332D),
                contentColor = SpectreWhite
            )
        ) {
            Text("SE DÉCONNECTER")
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = SpectreMuted,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            color = SpectreWhite,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatGo(bytes: Long): String {
    return String.format(
        Locale.FRANCE,
        "%.2f Go",
        bytes.toDouble() / 1_073_741_824.0
    )
}

private fun formatExpiration(value: String): String {
    return try {
        val parser = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val date: Date = parser.parse(value)
            ?: return value

        SimpleDateFormat(
            "dd/MM/yyyy à HH:mm",
            Locale.FRANCE
        ).format(date)
    } catch (_: Exception) {
        value
    }
}
