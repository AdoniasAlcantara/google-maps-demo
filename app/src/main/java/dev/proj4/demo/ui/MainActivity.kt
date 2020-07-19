package dev.proj4.demo.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dev.proj4.demo.R
import dev.proj4.demo.location.LocationLiveData
import dev.proj4.demo.location.LocationUpdate.Available
import dev.proj4.demo.location.LocationUpdate.Unavailable
import dev.proj4.demo.utils.toLatLng
import org.koin.android.ext.android.inject
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.withPermissionsCheck

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val locationLiveData: LocationLiveData by inject()
    private var isObservingLocationUpdates = false

    private var map: GoogleMap? = null
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.findFragmentById(R.id.mapHolder)?.let { fragment ->
            fragment as SupportMapFragment
            fragment.getMapAsync(::onMapReady)
        }

        setUpLocationUpdates()
    }

    private fun onMapReady(googleMap: GoogleMap) {
        if (map != null) return

        map = googleMap

        val makerOptions = MarkerOptions()
            .position(LatLng(0.0, 0.0))
            .icon(BitmapDescriptorFactory.defaultMarker(HUE_VIOLET))
            .visible(false)

        marker = googleMap.addMarker(makerOptions)
    }

    private fun setUpLocationUpdates() = withPermissionsCheck(
        ACCESS_FINE_LOCATION,
        onShowRationale = PermissionRequest::proceed
    ) {
        if (!isObservingLocationUpdates) {
            isObservingLocationUpdates = true

            locationLiveData.observe(this) { update ->
                when (update) {
                    is Available -> marker?.let {
                        it.position = update.location.toLatLng()
                        it.isVisible = true
                    }

                    is Unavailable -> marker?.isVisible = false
                }
            }
        }
    }
}