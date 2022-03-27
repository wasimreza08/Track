package com.example.datatrack.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttributesDto(
    @SerialName("batteryLevel")
    val batteryLevel: Int,
    @SerialName("lat")
    val lat: Double,
    @SerialName("lng")
    val lng: Double,
    @SerialName("maxSpeed")
    val maxSpeed: Int,
    @SerialName("vehicleType")
    val vehicleType: String,
    @SerialName("hasHelmetBox")
    val hasHelmetBox: Boolean
)
