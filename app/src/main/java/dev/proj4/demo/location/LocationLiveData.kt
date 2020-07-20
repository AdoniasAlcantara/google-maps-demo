package dev.proj4.demo.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import dev.proj4.demo.location.LocationUpdate.*

class LocationLiveData(
    private val client: FusedLocationProviderClient,
    private val settings: LocationRequest
) : LiveData<LocationUpdate>() {

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

            if (value !is Unavailable) {
                value = Available(location)
                Log.d(TAG, "Location: lat ${location.latitude}, lng ${location.longitude}")
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability?) {
            val isAvailable = availability?.isLocationAvailable ?: return

            when {
                !isAvailable -> {
                    value = Unavailable
                    Log.d(TAG, "Location: unavailable")
                }

                isAvailable && value is Unavailable -> {
                    value = Searching
                    Log.d(TAG, "Location: searching")
                }
            }
        }
    }

    private companion object {
        val TAG = LocationLiveData::class.qualifiedName
    }
}