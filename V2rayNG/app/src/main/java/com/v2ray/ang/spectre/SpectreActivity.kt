package com.v2ray.ang.spectre

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.v2ray.ang.BuildConfig

class SpectreActivity : ComponentActivity() {

    private val spectreViewModel: SpectreViewModel by viewModels {
        SpectreViewModelFactory(
            applicationContext,
            BuildConfig.SPECTRE_WEBHOOK_URL
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpectreScreen(viewModel = spectreViewModel)
        }

        spectreViewModel.restoreLocalSession()
    }
}
