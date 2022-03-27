package com.example.datatrack.data.repository

import com.example.datatrack.data.datasource.RemoteDataSource
import com.example.datatrack.data.mapper.ResponseToDomainMapper
import com.example.domain.domain.model.VehicleInfo
import com.example.domain.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val remoteDataSource: RemoteDataSource) :
    Repository {
    override fun getVehicles(): Flow<List<VehicleInfo>> {
        val mapper = ResponseToDomainMapper()
        return remoteDataSource.getRemoteVehicles().map { data ->
            data.map { mapper.map(it) }
        }
    }
}
