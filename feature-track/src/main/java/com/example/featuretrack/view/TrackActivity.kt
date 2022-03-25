package com.example.featuretrack.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.feature_track.databinding.ActivityTrackBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.fragmentContainer.id) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    /*  override fun onMapReady(googleMap: GoogleMap) {
          mMap = googleMap

          // Add a marker in Sydney and move the camera
          val sydney = LatLng(-34.0, 151.0)
          mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
          mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
      }*/
}