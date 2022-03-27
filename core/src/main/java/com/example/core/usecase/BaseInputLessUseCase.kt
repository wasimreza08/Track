package com.example.core.usecase

import kotlinx.coroutines.flow.Flow

interface BaseInputLessUseCase<Output : BaseInputLessUseCase.Output> {

    fun execute(): Flow<Output>

    interface Output
}
