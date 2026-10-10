package com.v2ray.ang.spectre

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * Stockage local des informations SpectreVPN.
 *
 * Utilise MMKV, déjà employé par v2rayNG.
 * Ce stockage conserve les données après la fermeture
 * de l'application, mais ne remplace pas la vérification
 * de l'abonnement auprès de Bots.Business.
 */
class SpectreClientStore(context: Context) {

    companion object {
        private const val KEY_TOKEN = "spectre_client_token"
        private const val KEY_MATRICULE = "spectre_client_matricule"
        private const val KEY_NOM = "spectre_client_nom"
        private const val KEY_FORFAIT = "spectre_client_forfait"
        private const val KEY_CODE = "spectre_client_code"
        private const val KEY_EXPIRATION = "spectre_client_expiration"
        private const val KEY_QUOTA = "spectre_client_quota"
        private const val KEY_CONSUMPTION = "spectre_client_consumption"
        private const val KEY_HOST = "spectre_current_host"
    }

    private val storage: MMKV = requireNotNull(MMKV.defaultMMKV()) {
        "Le stockage MMKV n'est pas initialisé."
    }

    // Conserver le token du client.
    fun saveToken(token: String) {
        storage.encode(KEY_TOKEN, token)
    }

    fun getToken(): String? {
        return storage.decodeString(KEY_TOKEN, null)
    }

    // Enregistrer les informations du compte.
    fun saveClient(client: SpectreClientInfo) {
        storage.encode(KEY_MATRICULE, client.matricule)
        storage.encode(KEY_NOM, client.nom)
        storage.encode(KEY_FORFAIT, client.forfait)
        storage.encode(KEY_CODE, client.codeAbonnement)
        storage.encode(KEY_EXPIRATION, client.dateExpiration)
        storage.encode(KEY_QUOTA, client.quotaOctets)
        storage.encode(KEY_CONSUMPTION, client.consommationOctets)
    }

    fun getMatricule(): String =
        storage.decodeString(KEY_MATRICULE, "") ?: ""

    fun getNom(): String =
        storage.decodeString(KEY_NOM, "") ?: ""

    fun getForfait(): String =
        storage.decodeString(KEY_FORFAIT, "") ?: ""

    fun getCodeAbonnement(): String =
        storage.decodeString(KEY_CODE, "") ?: ""

    fun getDateExpiration(): String =
        storage.decodeString(KEY_EXPIRATION, "") ?: ""

    fun getQuotaOctets(): Long =
        storage.decodeLong(KEY_QUOTA, 0L)

    fun getConsommationOctets(): Long =
        storage.decodeLong(KEY_CONSUMPTION, 0L)

    // Enregistrer le dernier Host récupéré.
    fun saveHost(host: String) {
        storage.encode(KEY_HOST, host)
    }

    fun getHost(): String? {
        return storage.decodeString(KEY_HOST, null)
    }

    // Effacer la session locale lors de la déconnexion.
    fun clearSession() {
        storage.removeValueForKey(KEY_TOKEN)
        storage.removeValueForKey(KEY_MATRICULE)
        storage.removeValueForKey(KEY_NOM)
        storage.removeValueForKey(KEY_FORFAIT)
        storage.removeValueForKey(KEY_CODE)
        storage.removeValueForKey(KEY_EXPIRATION)
        storage.removeValueForKey(KEY_QUOTA)
        storage.removeValueForKey(KEY_CONSUMPTION)
    }
}
