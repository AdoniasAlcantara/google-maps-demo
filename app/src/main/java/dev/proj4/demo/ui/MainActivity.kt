package dev.proj4.demo.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import dev.proj4.demo.R
import dev.proj4.demo.databinding.ActivityMainBinding
import dev.proj4.demo.location.LocationLiveData
import dev.proj4.demo.location.LocationUpdate.Available
import dev.proj4.demo.location.LocationUpdate.Unavailable
import dev.proj4.demo.ui.MainActivity.LocationState.*
import dev.proj4.demo.utils.toLatLng
import org.koin.android.ext.android.inject
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.withPermissionsCheck

class MainActivity : AppCompatActivity() {

    private val locationLiveData: LocationLiveData by inject()
    private var isObservingLocationUpdates = false

    private lateinit var binding: ActivityMainBinding
    private lateinit var map: GoogleMap
    private lateinit var locationMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launchWhenCreated {
            setUpMap()
            setUpLocationUpdates()
        }
    }

    private suspend fun setUpMap() {
        if (::map.isInitialized) return

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapHolder)
        val googleMap = (mapFragment as SupportMapFragment).awaitMap()

        locationMarker = googleMap.addMarker {
            val defaultPosition = LatLng(0.0, 0.0)
            position(defaultPosition)

            val pinpoint = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
            icon(pinpoint)

            visible(false)
        }

        map = googleMap
    }

    private fun setUpLocationUpdates() = withPermissionsCheck(
        ACCESS_FINE_LOCATION,
        onShowRationale = PermissionRequest::proceed,
        requiresPermission = ::observeLocationUpdates
    )

    private fun observeLocationUpdates() {
        if (isObservingLocationUpdates) return

        isObservingLocationUpdates = true

        locationLiveData.observe(this) { update ->
            locationState = when (update) {
                is Available -> Enabled(update.location)
                is Unavailable -> Disabled
            }
        }
    }

    private var locationState: LocationState = Searching
        set(state) {
            when (state) {
                is Enabled -> {
                    locationMarker.position = state.location.toLatLng()
                    locationMarker.isVisible = true
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_enabled)
                }

                is Searching -> {
                    locationMarker.isVisible = false
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_searching)
                }

                is Disabled -> {
                    locationMarker.isVisible = false
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_disabled)
                }
            }

            field = state
        }

    private sealed class LocationState {

        data class Enabled(val location: Location) : LocationState()

        object Searching : LocationState()

        object Disabled : LocationState()
    }
}