package com.v2ray.ang.spectre

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * État de l'espace client SpectreVPN.
 */
data class SpectreUiState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val client: SpectreClientInfo? = null,
    val host: String? = null,
    val error: String? = null,
    val message: String? = null
)

/**
 * Coordonne le compte SpectreVPN et l'application du host.
 */
class SpectreViewModel(
    context: Context,
    private val api: SpectreApi,
    private val store: SpectreClientStore
) : ViewModel() {

    private val appContext = context.applicationContext

    private val _uiState = MutableStateFlow(SpectreUiState())

    val uiState: StateFlow<SpectreUiState> =
        _uiState.asStateFlow()

    /**
     * Vérifie le token auprès du serveur.
     */
    fun login(token: String) {
        val cleanedToken = token.trim()

        if (cleanedToken.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Entre ton token client.",
                message = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val client = api.verifyToken(cleanedToken)

                store.saveToken(cleanedToken)
                store.saveClient(client)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = true,
                    client = client,
                    error = null,
                    message = "Abonnement vérifié avec succès."
                )

                // Récupère le host et tente de l'appliquer.
                refreshHost()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = false,
                    client = null,
                    error = e.message
                        ?: "Impossible de vérifier le compte.",
                    message = null
                )
            }
        }
    }

    /**
     * Récupère le host et tente de l'appliquer au profil sélectionné.
     */
    fun refreshHost() {
        viewModelScope.launch {
            try {
                val receivedHost = api.getHost().trim()

                if (receivedHost.isBlank()) {
                    throw IllegalStateException(
                        "Le serveur n'a fourni aucun host."
                    )
                }

                // Applique le host au profil VLESS sélectionné.
                val result = SpectreHostApplier.applyHost(
                    context = appContext,
                    receivedHost = receivedHost
                )

                if (!result.success) {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        message = null
                    )
                    return@launch
                }

                store.saveHost(receivedHost)

                _uiState.value = _uiState.value.copy(
                    host = receivedHost,
                    error = null,
                    message = result.message
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                        ?: "Impossible de récupérer le host.",
                    message = null
                )
            }
        }
    }

    /**
     * Recharge la session locale puis vérifie le compte en ligne.
     */
    fun restoreLocalSession() {
        val token = store.getToken()

        if (token.isNullOrBlank()) {
            return
        }

        _uiState.value = _uiState.value.copy(
            host = store.getHost()
        )

        login(token)
    }

    /**
     * Déconnexion locale.
     */
    fun logout() {
        store.clearSession()

        _uiState.value = SpectreUiState(
            message = "Déconnexion effectuée."
        )
    }

    /**
     * Convertit une quantité d'octets en Go.
     */
    fun formatGo(octets: Long): String {
        return String.format(
            Locale.FRANCE,
            "%.2f Go",
            octets.toDouble() / 1_073_741_824.0
        )
    }
}
