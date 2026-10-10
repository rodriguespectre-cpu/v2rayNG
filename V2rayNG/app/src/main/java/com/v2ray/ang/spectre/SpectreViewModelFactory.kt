package com.v2ray.ang.spectre

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SpectreViewModelFactory(
    context: Context,
    webhookUrl: String
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext
    private val api = SpectreApi(webhookUrl)
    private val store = SpectreClientStore(appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SpectreViewModel::class.java)) {
            return SpectreViewModel(api, store) as T
        }

        throw IllegalArgumentException(
            "ViewModel inconnu : ${modelClass.name}"
        )
    }
}
