package com.example.featuretrack.model

data class VehicleUiInfo(
    val id: String,
    val batteryLevel: Int,
    val clusterItem: VehicleClusterItem,
    val maxSpeed: Int,
    val vehicleType: String,
    val hasHelmetBox: Boolean,
    val distance: Double
)
