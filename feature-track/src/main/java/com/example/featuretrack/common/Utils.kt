package com.example.featuretrack.common

import android.content.Context
import android.location.LocationManager
import androidx.fragment.app.FragmentActivity
import com.example.feature_track.R
import com.example.featuretrack.model.VehicleType

object Utils {

    fun getVehicleDrawableId(vehicleType: String): Int {
        return when (vehicleType) {
            VehicleType.EBICYCLE.type -> {
                R.drawable.ic_ebicycle
            }
            VehicleType.ESCOOTER.type -> {
                R.drawable.ic_scooter
            }
            VehicleType.EMOPED.type -> {
                R.drawable.ic_emoped
            }
            else -> {
                R.drawable.ic_vehicle
            }
        }
    }

    fun isGPSEnabled(activity: FragmentActivity?): Boolean {
        var locationManager: LocationManager? = null

        if (locationManager == null) {
            locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }
}