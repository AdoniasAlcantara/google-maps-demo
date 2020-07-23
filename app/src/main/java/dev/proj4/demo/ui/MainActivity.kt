package dev.proj4.demo.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.cameraPosition
import dev.proj4.demo.R
import dev.proj4.demo.databinding.ActivityMainBinding
import dev.proj4.demo.location.LocationUpdate.*
import dev.proj4.demo.utils.toLatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.withPermissionsCheck

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var isInitialState = true

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var map: GoogleMap
    private lateinit var locationMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isInitialState = savedInstanceState == null
        setUpWindow()
        setContentView(binding.root)

        lifecycleScope.launchWhenCreated {
            setUpMap()
            setUpControls()
            setUpLocationUpdates(isInitialState)

            if (isInitialState) moveCameraToCurrentLocation()
        }
    }

    private fun setUpWindow() = window.let {
        // Make status bar translucent
        val flags = SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        it.decorView.systemUiVisibility = flags

        val color = ContextCompat.getColor(this, R.color.translucent)
        it.statusBarColor = color
    }

    private suspend fun setUpMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapHolder)
        map = (mapFragment as SupportMapFragment).awaitMap()

        locationMarker = map.addMarker {
            val defaultPosition = LatLng(0.0, 0.0)
            position(defaultPosition)

            val pinpoint = BitmapDescriptorFactory.defaultMarker(HUE_VIOLET)
            icon(pinpoint)

            visible(false)
        }

        // prevent user from clicking the marker
        map.setOnMarkerClickListener { true }
    }

    private fun setUpControls() {
        binding.buttonLocation.setOnClickListener {
            if (viewModel.locationUpdates.value is Unavailable) {
                setUpLocationUpdates(true)
            }

            moveCameraToCurrentLocation()
        }
    }

    private fun setUpLocationUpdates(ensureSettings: Boolean) = withPermissionsCheck(
        ACCESS_FINE_LOCATION,
        onShowRationale = PermissionRequest::proceed,
        requiresPermission = {
            observeLocationUpdates()
            if (ensureSettings) checkSettings()
        }
    )

    private fun observeLocationUpdates() {
        viewModel.locationUpdates.observe(this) { update ->
            when (update) {
                is Available -> {
                    locationMarker.position = update.location.toLatLng()
                    locationMarker.isVisible = true
                    binding.buttonLocation.setImageResource(R.drawable.ic_location_enabled)

                    if (viewModel.isMoveToCurrentLocationPending) moveCameraToCurrentLocation()
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

    private fun moveCameraToCurrentLocation() {
        val lastUpdate = viewModel.locationUpdates.value

        if (lastUpdate is Available) {
            viewModel.isMoveToCurrentLocationPending = false

            val camera = cameraPosition {
                target(lastUpdate.location.toLatLng())
                zoom(DEFAULT_ZOOM)
            }

            val cameraUpdate = CameraUpdateFactory.newCameraPosition(camera)
            map.animateCamera(cameraUpdate)
        } else {
            viewModel.isMoveToCurrentLocationPending = true
        }
    }

    private fun checkSettings() = lifecycleScope.launch {
        val locationRequest = viewModel.locationUpdates.request

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val task = LocationServices
            .getSettingsClient(applicationContext)
            .checkLocationSettings(settingsRequest)

        try {
            task.await()
        } catch (exception: ResolvableApiException) {
            val intentSender = exception.resolution.intentSender
            val intentRequest = IntentSenderRequest.Builder(intentSender).build()

            resolveSettings.launch(intentRequest)
        }
    }

    private val resolveSettings = registerForActivityResult(
        StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            viewModel.isMoveToCurrentLocationPending = false
        }
    }

    private companion object {
        const val DEFAULT_ZOOM = 14f
    }
}