package com.example.domain.domain.usecase

import com.example.core.dispatcher.BaseDispatcherProvider
import com.example.core.ext.isNetworkException
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
    override fun execute(): Flow<GetVehiclesUseCase.Output> {
        return repository.getVehicles().map { vehicleList ->
            GetVehiclesUseCase.Output.Success(vehicleList) as GetVehiclesUseCase.Output
        }.catch { exception ->
            if (exception.isNetworkException()) {
                emit(GetVehiclesUseCase.Output.NetworkError)
            } else {
                emit(GetVehiclesUseCase.Output.UnknownError(exception.message.orEmpty()))
            }
        }.flowOn(mainDispatcherProvider.io())
    }
}
