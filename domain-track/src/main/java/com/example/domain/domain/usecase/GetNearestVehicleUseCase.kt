package com.example.domain.domain.usecase

import android.location.Location
import com.example.core.usecase.BaseUseCase
import com.example.domain.domain.model.VehicleInfo

interface GetNearestVehicleUseCase :
    BaseUseCase<GetNearestVehicleUseCase.Input, GetNearestVehicleUseCase.Output> {
    data class Input(
        val userLocation: Location,
        val vehicleList: List<VehicleInfo>
    ) : BaseUseCase.Input

    sealed class Output : BaseUseCase.Output {
        data class Success(val vehicle: VehicleInfo) : Output()
        data class UnknownError(val message: String) : Output()
    }
}
