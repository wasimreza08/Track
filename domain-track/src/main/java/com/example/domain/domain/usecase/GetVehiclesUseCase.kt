package com.example.domain.domain.usecase

import com.example.core.usecase.BaseInputLessUseCase
import com.example.domain.domain.model.VehicleInfo

interface GetVehiclesUseCase : BaseInputLessUseCase<GetVehiclesUseCase.Output> {
    sealed class Output : BaseInputLessUseCase.Output {
        data class Success(val vehicleList: List<VehicleInfo>) : Output()
        object NetworkError : Output()
        data class UnknownError(val message: String) : Output()
    }
}
