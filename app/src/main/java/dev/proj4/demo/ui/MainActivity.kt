package dev.proj4.demo.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import dev.proj4.demo.R
import dev.proj4.demo.databinding.ActivityMainBinding
import dev.proj4.demo.location.LocationSettingsResolver
import dev.proj4.demo.location.LocationUpdate.*
import dev.proj4.demo.utils.toLatLng
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.withPermissionsCheck

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val settingsResolver by lazy { LocationSettingsResolver(this, activityResultRegistry) }
    private var isObservingLocationUpdates = false

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var map: GoogleMap
    private lateinit var locationMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycle.addObserver(settingsResolver)

        lifecycleScope.launchWhenCreated {
            setUpMap()
            setUpMapControls()
            setUpLocationUpdates()
        }
    }

    private fun setUpMapControls() {
        binding.buttonLocation.setOnClickListener { setUpLocationUpdates(true) }
    }

    private suspend fun setUpMap() {
        if (::map.isInitialized) return

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapHolder)
        val map = (mapFragment as SupportMapFragment).awaitMap()

        locationMarker = map.addMarker {
            val defaultPosition = LatLng(0.0, 0.0)
            position(defaultPosition)

            val pinpoint = BitmapDescriptorFactory.defaultMarker(HUE_VIOLET)
            icon(pinpoint)

            visible(false)
        }
    }

    private fun setUpLocationUpdates(ensureSettings: Boolean = false) = withPermissionsCheck(
        ACCESS_FINE_LOCATION,
        onShowRationale = PermissionRequest::proceed,
        requiresPermission = {
            observeLocationUpdates()

            if (ensureSettings) {
                val locationRequest = viewModel.locationUpdates.request
                settingsResolver.ensureLocationSettingsMeet(locationRequest)
            }
        }
    )

    private fun observeLocationUpdates() {
        if (isObservingLocationUpdates) return

        isObservingLocationUpdates = true

        viewModel.locationUpdates.observe(this) { update ->
            when (update) {
                is Available -> {
                    locationMarker.position = update.location.toLatLng()
                    locationMarker.isVisible = true
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_enabled)
                }

                is Searching -> {
                    locationMarker.isVisible = false
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_searching)
                }

                is Unavailable -> {
                    locationMarker.isVisible = false
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_disabled)
                }
            }
        }
    }
}