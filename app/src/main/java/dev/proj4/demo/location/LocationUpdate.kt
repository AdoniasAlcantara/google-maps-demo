package dev.proj4.demo.location

import android.location.Location

sealed class LocationUpdate {

    data class Available(val location: Location) : LocationUpdate()

    object Unavailable : LocationUpdate()
}