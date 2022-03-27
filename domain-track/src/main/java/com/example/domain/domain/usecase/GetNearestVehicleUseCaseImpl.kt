package com.example.domain.domain.usecase

import android.location.Location
import com.example.core.dispatcher.BaseDispatcherProvider
import com.example.domain.domain.model.VehicleInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class GetNearestVehicleUseCaseImpl @Inject constructor(
    private val mainDispatcherProvider: BaseDispatcherProvider
) : GetNearestVehicleUseCase {
    override fun execute(input: GetNearestVehicleUseCase.Input): Flow<GetNearestVehicleUseCase.Output> {
        return flow<GetNearestVehicleUseCase.Output> {
            emit(
                GetNearestVehicleUseCase.Output.Success(
                    provideNearestVehicle(
                        input.userLocation,
                        input.vehicleList
                    )
                )
            )
        }.catch { exception ->
            emit(GetNearestVehicleUseCase.Output.UnknownError(exception.message.orEmpty()))
        }.flowOn(mainDispatcherProvider.compute())
    }

    private fun provideNearestVehicle(
        userLocation: Location,
        vehicleList: List<VehicleInfo>
    ): VehicleInfo {
        var nearestVehicleIndex: Int = -1
        var nearestDistance = Float.MAX_VALUE
        for (index in 0..vehicleList.lastIndex) {
            val location = Location("")
            location.latitude = vehicleList[index].lat
            location.longitude = vehicleList[index].lng
            val distance = userLocation.distanceTo(location)
            if (nearestDistance > distance) {
                nearestDistance = distance
                nearestVehicleIndex = index
            }
        }
        Timber.e("near distance $nearestDistance")
        return vehicleList[nearestVehicleIndex]
    }
}
