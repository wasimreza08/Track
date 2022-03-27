package com.example.datatrack.data.utils

import com.example.datatrack.data.dto.AttributesDto
import com.example.datatrack.data.dto.DataDto
import com.example.datatrack.data.dto.RootResponseDto
import com.example.domain.domain.model.VehicleInfo

object TestData {
    private val attributesDto = AttributesDto(
        batteryLevel = 20,
        lat = 53.02,
        lng = 13.32,
        maxSpeed = 20,
        vehicleType = "escooter",
        hasHelmetBox = false
    )
    val dataDto = DataDto(
        type = "vehicle",
        id = "test_1",
        attributes = attributesDto
    )

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

    val responseDto = RootResponseDto(arrayListOf(dataDto))
}
