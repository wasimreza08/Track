package com.example.datatrack.data.datasource

import com.example.datatrack.data.dto.DataDto
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    fun getRemoteVehicles(): Flow<List<DataDto>>
}
