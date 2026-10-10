package com.v2ray.ang.spectre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

/**
 * Activité dédiée à l'espace client SpectreVPN.
 *
 * Elle reste séparée de MainActivity pour préserver
 * le fonctionnement existant du moteur VPN.
 */
class SpectreActivity : ComponentActivity() {

    companion object {
        const val EXTRA_WEBHOOK_URL = "spectre_webhook_url"
    }

    private val spectreViewModel: SpectreViewModel by viewModels {
        SpectreViewModelFactory(
            applicationContext,
            intent.getStringExtra(EXTRA_WEBHOOK_URL).orEmpty()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpectreScreen(
                viewModel = spectreViewModel
            )
        }
    }
}
