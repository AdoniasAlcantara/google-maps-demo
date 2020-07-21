package dev.proj4.demo.location

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationSettingsResolver(
    private val context: Context,
    private val registry: ActivityResultRegistry
) : LifecycleObserver {

    private var resolveSettings: ActivityResultLauncher<IntentSenderRequest>? = null

    @OnLifecycleEvent(ON_CREATE)
    fun register() {
        val contract = StartIntentSenderForResult()
        resolveSettings = registry.register(REQUEST_KEY, contract) { /* Ignore result */ }
    }

    @OnLifecycleEvent(ON_DESTROY)
    fun unregister() {
        resolveSettings?.unregister()
    }

    fun ensureLocationSettingsMeet(request: LocationRequest) = MainScope().launch {
        runCatching {
            checkLocationSettings(context, request)
        }.onFailure { exception ->
            if (exception !is ResolvableApiException) return@launch

            val intentSender = exception.resolution.intentSender
            val intentRequest = IntentSenderRequest.Builder(intentSender).build()
            resolveSettings?.launch(intentRequest)
        }
    }

    private suspend fun checkLocationSettings(
        context: Context,
        request: LocationRequest
    ): LocationSettingsResponse {
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(request)
            .build()

        val task = LocationServices
            .getSettingsClient(context)
            .checkLocationSettings(settingsRequest)

        return task.await()
    }

    private companion object {
        const val REQUEST_KEY = "locationSettings"
    }
}