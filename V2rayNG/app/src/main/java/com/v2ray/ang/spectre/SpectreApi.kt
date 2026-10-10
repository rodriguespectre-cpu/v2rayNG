package com.v2ray.ang.spectre

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Informations du compte SpectreVPN renvoyées par Bots.Business.
 */
data class SpectreClientInfo(
    val matricule: String,
    val nom: String,
    val codeAbonnement: String,
    val forfait: String,
    val dateExpiration: String,
    val quotaOctets: Long,
    val consommationOctets: Long,
    val donneesRestantesOctets: Long?
)

/**
 * Communication HTTP entre SpectreVPN et Bots.Business.
 *
 * L'URL du webhook doit être fournie à l'initialisation.
 * Ne pas placer le jeton du webhook directement dans le code public.
 */
class SpectreApi(
    private val webhookUrl: String
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType =
        "application/json; charset=utf-8".toMediaType()

    /**
     * Envoie une action au webhook Bots.Business.
     */
    private suspend fun post(
        data: JSONObject
    ): JSONObject = withContext(Dispatchers.IO) {

        if (webhookUrl.isBlank()) {
            throw IOException("URL du serveur non configurée.")
        }

        val body = data.toString()
            .toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(webhookUrl)
            .post(body)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->

            val responseText = response.body?.string()
                ?: throw IOException("Réponse vide du serveur.")

            if (!response.isSuccessful) {
                throw IOException(
                    "Erreur HTTP : ${response.code}"
                )
            }

            try {
                JSONObject(responseText)
            } catch (e: Exception) {
                throw IOException(
                    "Réponse JSON invalide du serveur."
                )
            }
        }
    }

    /**
     * Vérifie le token et récupère le compte du client.
     */
    suspend fun verifyToken(
        token: String
    ): SpectreClientInfo {

        val cleanedToken = token.trim()

        if (!cleanedToken.matches(Regex("^[A-Za-z0-9]{32}$"))) {
            throw IOException("Le format du token est invalide.")
        }

        val requestData = JSONObject().apply {
            put("action", "verify_token")
            put("token", cleanedToken)
        }

        val response = post(requestData)

        if (!response.optBoolean("success", false)) {
            val error = response.optString(
                "error",
                "verification_failed"
            )

            val message = when (error) {
                "client_not_found" ->
                    "Token inconnu."

                "invalid_token" ->
                    "Token invalide."

                "account_suspended" ->
                    "Compte suspendu."

                "subscription_expired" ->
                    "Abonnement expiré."

                "data_quota_exhausted" ->
                    "Quota de données épuisé."

                else ->
                    "Vérification impossible : $error"
            }

            throw IOException(message)
        }

        val account = response.optJSONObject("client")
            ?: throw IOException(
                "Les informations du compte sont absentes."
            )

        SpectreClientInfo(
            matricule = account.optString("matricule"),
            nom = account.optString("nom"),
            codeAbonnement = account.optString(
                "code_abonnement"
            ),
            forfait = account.optString("forfait"),
            dateExpiration = account.optString(
                "date_expiration"
            ),
            quotaOctets = account.optLong(
                "quota_octets",
                0L
            ),
            consommationOctets = account.optLong(
                "consommation_octets",
                0L
            ),
            donneesRestantesOctets =
                if (account.isNull("donnees_restantes_octets")) {
                    null
                } else {
                    account.optLong(
                        "donnees_restantes_octets",
                        0L
                    )
                }
        )
    }

    /**
     * Récupère le Host actuellement enregistré
     * dans Bots.Business.
     */
    suspend fun getHost(): String {

        val requestData = JSONObject().apply {
            put("action", "get_host")
        }

        val response = post(requestData)

        if (!response.optBoolean("success", false)) {
            val error = response.optString(
                "error",
                "host_unavailable"
            )

            throw IOException(
                "Impossible de récupérer le Host : $error"
            )
        }

        val host = response.optString("host").trim()

        if (host.isBlank()) {
            throw IOException("Le Host reçu est vide.")
        }

        return host
    }
}
