package dev.proj4.demo.ui

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import dev.proj4.demo.location.LocationLiveData
import dev.proj4.demo.utils.checkLocationSettings
import kotlinx.coroutines.launch

class MainViewModel(
    private val context: Context,
    val locationUpdates: LocationLiveData
) : ViewModel() {

    private var settingsResolver: ActivityResultLauncher<IntentSenderRequest>? = null

    fun ensureLocationSettingsMeet(registry: ActivityResultRegistry) = viewModelScope.launch {
        runCatching {
            checkLocationSettings(context, locationUpdates.request)
        }.onFailure { exception ->
            if (exception !is ResolvableApiException) return@launch

            settingsResolver ?: registerSettingsResolver(registry)

            val intentSender = exception.resolution.intentSender
            val intentRequest = IntentSenderRequest.Builder(intentSender).build()
            settingsResolver?.launch(intentRequest)
        }
    }

    private fun registerSettingsResolver(registry: ActivityResultRegistry) {
        settingsResolver = registry.register(
            REQUEST_LOCATION_SETTINGS,
            StartIntentSenderForResult()
        ) { /* Ignore result */ }
    }

    private fun unregisterSettingsResolver() {
        settingsResolver?.unregister()
        settingsResolver = null
    }

    override fun onCleared() {
        unregisterSettingsResolver()
    }

    private companion object {
        const val REQUEST_LOCATION_SETTINGS = "locationSettings"
    }
}