package com.example.domain.data.datasource

import com.example.domain.data.api.Api
import com.example.domain.data.dto.DataDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteDataSourceImpl @Inject constructor(private val api: Api) : RemoteDataSource {
    override fun getRemoteVehicles(): Flow<List<DataDto>> = flow {
        emit(api.getVehicles().data)
    }
}
