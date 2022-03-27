package com.example.domain.domain.repository

import com.example.domain.domain.model.VehicleInfo
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getVehicles(): Flow<List<VehicleInfo>>
}
