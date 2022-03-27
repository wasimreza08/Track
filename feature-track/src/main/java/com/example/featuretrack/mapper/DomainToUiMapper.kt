package com.example.featuretrack.mapper

import com.example.core.mapper.Mapper
import com.example.domain.domain.model.VehicleInfo
import com.example.featuretrack.common.Utils
import com.example.featuretrack.model.VehicleClusterItem
import com.example.featuretrack.model.VehicleUiInfo

class DomainToUiMapper : Mapper<VehicleInfo, VehicleUiInfo> {
    override fun map(from: VehicleInfo): VehicleUiInfo {
        return VehicleUiInfo(
            id = from.id,
            batteryLevel = from.batteryLevel,
            clusterItem = VehicleClusterItem(
                from.lat,
                from.lng,
                titleString = from.id,
                snippetString = from.vehicleType
            ),
            maxSpeed = from.maxSpeed,
            vehicleType = from.vehicleType,
            hasHelmetBox = from.hasHelmetBox,
            distance = from.distance?.let { Utils.meterToKiloMeter(it) } ?: -1.0
        )
    }
}
