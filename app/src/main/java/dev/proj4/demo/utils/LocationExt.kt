package dev.proj4.demo.utils

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

fun Location.toLatLng() = LatLng(latitude, longitude)

suspend fun checkLocationSettings(
    context: Context,
    request: LocationRequest
) : LocationSettingsResponse {
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(request)
        .build()

    val task = LocationServices
        .getSettingsClient(context)
        .checkLocationSettings(settingsRequest)

    return task.await()
}