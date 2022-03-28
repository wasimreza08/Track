package com.example.domain.domain.usecase

import android.location.Location
import com.example.core.dispatcher.BaseDispatcherProvider
import com.example.core.ext.isNetworkException
import com.example.domain.domain.model.VehicleInfo
import com.example.domain.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetVehiclesUseCaseImpl @Inject constructor(
    private val repository: Repository,
    private val mainDispatcherProvider: BaseDispatcherProvider
) : GetVehiclesUseCase {
    override fun execute(input: GetVehiclesUseCase.Input): Flow<GetVehiclesUseCase.Output> {
        return repository.getVehicles().map { vehicleList ->
            val finalList = provideNearestVehicle(input.userLocation, vehicleList)
            GetVehiclesUseCase.Output.Success(finalList) as GetVehiclesUseCase.Output
        }.catch { exception ->
            if (exception.isNetworkException()) {
                emit(GetVehiclesUseCase.Output.NetworkError)
            } else {
                emit(GetVehiclesUseCase.Output.UnknownError(exception.message.orEmpty()))
            }
        }.flowOn(mainDispatcherProvider.io())
    }

    private fun provideNearestVehicle(
        userLocation: Location,
        vehicleList: List<VehicleInfo>
    ): List<VehicleInfo> {
        if (vehicleList.isEmpty()) {
            return vehicleList
        }
        val mutableVehicleList = vehicleList.toMutableList()
        var nearestVehicleIndex: Int = -1
        var nearestDistance = Float.MAX_VALUE
        for (index in 0..vehicleList.lastIndex) {
            val location = Location("")
            location.latitude = vehicleList[index].lat
            location.longitude = vehicleList[index].lng
            val distance = userLocation.distanceTo(location)
            mutableVehicleList[index].distance = distance
            if (nearestDistance > distance) {
                nearestDistance = distance
                nearestVehicleIndex = index
            }
        }

        val nearestVehicle = vehicleList[nearestVehicleIndex]
        mutableVehicleList.removeAt(nearestVehicleIndex)
        mutableVehicleList.add(0, nearestVehicle)
        return mutableVehicleList
    }
}
