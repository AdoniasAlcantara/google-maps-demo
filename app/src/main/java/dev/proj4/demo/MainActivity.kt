package dev.proj4.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.findFragmentById(R.id.mapHolder)?.let { fragment ->
            fragment as SupportMapFragment
            fragment.getMapAsync(::onMapReady)
        }
    }

    private fun onMapReady(googleMap: GoogleMap) {
        println("Map is ready")
    }
}