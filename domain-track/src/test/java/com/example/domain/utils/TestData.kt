package com.example.domain.utils

import com.example.domain.domain.model.VehicleInfo

object TestData {
    val vehicleInfo = VehicleInfo(
        id = "test_1",
        type = "vehicle",
        batteryLevel = 20,
        lat = 53.02,
        lng = 13.32,
        maxSpeed = 20,
        vehicleType = "escooter",
        hasHelmetBox = false
    )

    val vehicleListProvided = listOf(
        vehicleInfo.copy(id = "test_2", lat = 100.5, lng = 32.6),
        vehicleInfo.copy(id = "test_3", lat = 10.5, lng = 54.6),
        vehicleInfo,
    )
    val vehicleListExpected = listOf(
        vehicleInfo.copy(distance = 1.0f),
        vehicleInfo.copy(id = "test_2", lat = 100.5, lng = 32.6, distance = 2.0f),
        vehicleInfo.copy(id = "test_3", lat = 10.5, lng = 54.6, distance = 3.0f),
    )
}
