package dev.proj4.demo.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import dev.proj4.demo.location.LocationUpdate.Available
import dev.proj4.demo.location.LocationUpdate.Unavailable

class LocationLiveData(
    private val client: FusedLocationProviderClient,
    private val settings: LocationRequest
) : LiveData<LocationUpdate>() {

    private var isLocationAvailable = false

    @RequiresPermission(ACCESS_FINE_LOCATION)
    override fun onActive() {
        client.requestLocationUpdates(settings, callback, null)
        Log.i(TAG, "Location updates started")
    }

    override fun onInactive() {
        client.removeLocationUpdates(callback)
        Log.i(TAG, "Location updates stopped")
    }

    private val callback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult?) {
            val location = result?.lastLocation ?: return

            if (isLocationAvailable) {
                value = Available(location)
                Log.d(TAG, "Location: lat ${location.latitude}, lng ${location.longitude}")
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability?) {
            isLocationAvailable = availability?.isLocationAvailable ?: return

            if (!isLocationAvailable) {
                value = Unavailable
                Log.d(TAG, "Location: unavailable")
            }
        }
    }

    private companion object {
        val TAG = LocationLiveData::class.qualifiedName
    }
}