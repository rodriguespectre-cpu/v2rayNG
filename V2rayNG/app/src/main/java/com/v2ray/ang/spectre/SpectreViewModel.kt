package com.v2ray.ang.spectre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

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
 * Coordonne les appels API et les informations du compte.
 *
 * Ce ViewModel ne modifie pas encore le moteur Xray.
 */
class SpectreViewModel(
    private val api: SpectreApi,
    private val store: SpectreClientStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpectreUiState())

    val uiState: StateFlow<SpectreUiState> =
        _uiState.asStateFlow()

    /**
     * Vérifie le token auprès de Bots.Business.
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

                // Sauvegarder uniquement après une vérification réussie.
                store.saveToken(cleanedToken)
                store.saveClient(client)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = true,
                    client = client,
                    error = null,
                    message = "Abonnement vérifié avec succès."
                )

                // Récupérer ensuite le Host.
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
     * Récupère le Host depuis Bots.Business.
     */
    fun refreshHost() {
        viewModelScope.launch {
            try {
                val host = api.getHost()

                store.saveHost(host)

                _uiState.value = _uiState.value.copy(
                    host = host,
                    error = null
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                        ?: "Impossible de récupérer le Host."
                )
            }
        }
    }

    /**
     * Recharge le compte enregistré localement.
     *
     * Cette méthode ne remplace pas la vérification
     * en ligne de l'abonnement.
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
            java.util.Locale.FRANCE,
            "%.2f Go",
            octets.toDouble() / 1_073_741_824.0
        )
    }
}
