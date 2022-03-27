package com.example.domain.domain.model

data class VehicleInfo(
    val id: String,
    val type: String,
    val batteryLevel: Int,
    val lat: Double,
    val lng: Double,
    val maxSpeed: Int,
    val vehicleType: String,
    val hasHelmetBox: Boolean,
    var distance: Float? = null
)
