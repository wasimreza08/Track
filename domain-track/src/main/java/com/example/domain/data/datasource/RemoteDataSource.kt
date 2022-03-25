package com.example.domain.data.datasource

import com.example.domain.data.dto.DataDto
import kotlinx.coroutines.flow.Flow


interface RemoteDataSource {
    fun getRemoteVehicles(): Flow<List<DataDto>>
}
