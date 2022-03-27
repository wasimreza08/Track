package com.example.domain.domain.usecase

import app.cash.turbine.test
import com.example.core.dispatcher.BaseDispatcherProvider
import com.example.domain.domain.repository.Repository
import com.example.domain.utils.TestData
import com.example.domain.utils.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class GetVehiclesUseCaseImplTest {
    private val repository: Repository = mockk()
    private val testDispatcher: BaseDispatcherProvider = TestDispatcherProvider()
    private val useCase = GetVehiclesUseCaseImpl(repository, testDispatcher)

    @Test
    fun `test getVehicleUseCase with valid input return success output`() = runTest {
        val provided = listOf(TestData.vehicleInfo)
        val expected = GetVehiclesUseCase.Output.Success(listOf(TestData.vehicleInfo))

        coEvery {
            repository.getVehicles()
        } returns flow { emit(provided) }

        useCase.execute().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getVehicleUseCase with network error input return network error output`() = runTest {
        val provided = IOException("test")
        val expected = GetVehiclesUseCase.Output.NetworkError

        coEvery {
            repository.getVehicles()
        } returns flow { emit(throw provided) }

        useCase.execute().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getVehicleUseCase with unknown error input return unknown error output`() = runTest {
        val provided = IllegalStateException("test")
        val expected = GetVehiclesUseCase.Output.UnknownError("test")

        coEvery {
            repository.getVehicles()
        } returns flow { emit(throw provided) }

        useCase.execute().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }
}