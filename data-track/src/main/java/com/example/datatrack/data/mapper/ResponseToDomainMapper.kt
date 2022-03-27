package com.example.datatrack.data.mapper

import com.example.core.mapper.Mapper
import com.example.datatrack.data.dto.DataDto
import com.example.domain.domain.model.VehicleInfo

class ResponseToDomainMapper : Mapper<DataDto, VehicleInfo> {
    override fun map(from: DataDto): VehicleInfo {
        return VehicleInfo(
            id = from.id,
            type = from.type,
            batteryLevel = from.attributes.batteryLevel,
            lat = from.attributes.lat,
            lng = from.attributes.lng,
            maxSpeed = from.attributes.maxSpeed,
            vehicleType = from.attributes.vehicleType,
            hasHelmetBox = from.attributes.hasHelmetBox
        )
    }
}
