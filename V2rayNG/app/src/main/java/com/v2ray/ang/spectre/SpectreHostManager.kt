package com.v2ray.ang.spectre

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Gestionnaire d'actualisation du Host de SpectreVPN.
 *
 * Il interroge Bots.Business toutes les trois heures
 * tant que le gestionnaire est actif.
 *
 * Attention : récupérer le Host ne modifie pas encore
 * la configuration du moteur Xray.
 */
class SpectreHostManager(
    private val api: SpectreApi,
    private val onHostUpdated: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    companion object {
        private const val REFRESH_INTERVAL_MS =
            3L * 60L * 60L * 1000L
    }

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private var refreshJob: Job? = null

    /**
     * Effectue immédiatement une récupération du Host,
     * puis recommence toutes les trois heures.
     */
    fun start() {
        if (refreshJob?.isActive == true) return

        refreshJob = scope.launch {
            while (isActive) {
                try {
                    val host = api.getHost()
                    onHostUpdated(host)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: IOException) {
                    onError(
                        e.message ?: "Impossible de récupérer le Host."
                    )
                } catch (e: Exception) {
                    onError("Une erreur inattendue est survenue.")
                }

                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    /**
     * Arrête l'actualisation périodique.
     */
    fun stop() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * Libère les ressources du gestionnaire.
     */
    fun release() {
        stop()
        scope.cancel()
    }
}
