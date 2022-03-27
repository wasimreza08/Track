package com.example.domain.domain.usecase

import android.location.Location
import com.example.core.usecase.BaseUseCase
import com.example.domain.domain.model.VehicleInfo

interface GetVehiclesUseCase : BaseUseCase<GetVehiclesUseCase.Input, GetVehiclesUseCase.Output> {
    data class Input(val userLocation: Location) : BaseUseCase.Input
    sealed class Output : BaseUseCase.Output {
        data class Success(val vehicleList: List<VehicleInfo>) : Output()
        object NetworkError : Output()
        data class UnknownError(val message: String) : Output()
    }
}
