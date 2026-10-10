package com.v2ray.ang.spectre

import android.content.Context
import com.v2ray.ang.core.LauncherManager
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.Utils

data class SpectreHostApplyResult(
    val success: Boolean,
    val changed: Boolean,
    val message: String
)

object SpectreHostApplier {

    fun applyHost(
        context: Context,
        receivedHost: String
    ): SpectreHostApplyResult {

        val appContext = context.applicationContext
        val newHost = receivedHost.trim().trimEnd('.')

        // Refuser les valeurs vides et les URL complètes.
        if (
            newHost.isBlank() ||
            newHost.contains("://") ||
            newHost.contains("/") ||
            newHost.contains(" ") ||
            newHost.contains(":")
        ) {
            return SpectreHostApplyResult(
                success = false,
                changed = false,
                message = "Le host reçu est invalide."
            )
        }

        // Accepter un nom d'hôte valide ou une adresse IP.
        val validHost =
            Utils.isValidUrl(newHost) ||
            Utils.isPureIpAddress(newHost)

        if (!validHost) {
            return SpectreHostApplyResult(
                success = false,
                changed = false,
                message = "Le host reçu n'est pas reconnu."
            )
        }

        // Identifier le profil actuellement sélectionné.
        val guid = MmkvManager.getSelectServer()

        if (guid.isNullOrBlank()) {
            return SpectreHostApplyResult(
                success = false,
                changed = false,
                message = "Aucun profil n'est sélectionné."
            )
        }

        val profile = MmkvManager.decodeServerConfig(guid)

        if (profile == null) {
            return SpectreHostApplyResult(
                success = false,
                changed = false,
                message = "Impossible de charger le profil sélectionné."
            )
        }

        // Première version : modification du profil VLESS uniquement.
        if (profile.configType != EConfigType.VLESS) {
            return SpectreHostApplyResult(
                success = false,
                changed = false,
                message = "Sélectionne un profil VLESS pour actualiser le host."
            )
        }

        // Ne rien faire si le host est déjà à jour.
        if (profile.server.equals(newHost, ignoreCase = true)) {
            return SpectreHostApplyResult(
                success = true,
                changed = false,
                message = "Le host est déjà à jour."
            )
        }

        // Modifier uniquement l'adresse principale du serveur.
        // Le port, l'UUID, le TLS/SNI, le WS et les autres champs restent intacts.
        profile.server = newHost

        return try {
            MmkvManager.encodeServerConfig(guid, profile)

            // Demande le redémarrage au service existant.
            // Si le VPN est arrêté, cette commande ne le démarre pas.
            LauncherManager.restartService(appContext)

            SpectreHostApplyResult(
                success = true,
                changed = true,
                message = "Host enregistré. Redémarrage du VPN demandé."
            )
        } catch (e: Exception) {
            SpectreHostApplyResult(
                success = false,
                changed = false,
                message = "Échec de la sauvegarde : ${
                    e.message ?: e.javaClass.simpleName
                }"
            )
        }
    }
}
