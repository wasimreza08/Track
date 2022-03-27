package com.example.featuretrack.common

import com.example.feature_track.R
import com.example.featuretrack.model.VehicleType
import kotlin.math.round

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

    fun meterToKiloMeter(meter: Float): Double {
        return (meter * 0.001)
    }


}