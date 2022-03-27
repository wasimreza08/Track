package com.example.domain.domain.usecase

import android.location.Location
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
    private val userLocation: Location = mockk()

    private fun userLocation(): Location {
        val userLocation = Location("user")
        userLocation.run {
            latitude = 54.02
            longitude = 14.02
        }
        return userLocation
    }

    @Test
    fun `test getVehicleUseCase with valid input return success output`() = runTest {
        val provided = TestData.vehicleListProvided
        val expected = GetVehiclesUseCase.Output.Success(TestData.vehicleListExpected)
        val input = GetVehiclesUseCase.Input(userLocation)

        coEvery {
            repository.getVehicles()
        } returns flow { emit(provided) }

        coEvery {
            userLocation.distanceTo(any())
        } returns 2.0f andThen 3.0f andThen 1.0f

        useCase.execute(input).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getVehicleUseCase with network error input return network error output`() = runTest {
        val provided = IOException("test")
        val expected = GetVehiclesUseCase.Output.NetworkError
        val input = GetVehiclesUseCase.Input(userLocation())
        coEvery {
            repository.getVehicles()
        } returns flow { emit(throw provided) }

        useCase.execute(input).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getVehicleUseCase with unknown error input return unknown error output`() = runTest {
        val provided = IllegalStateException("test")
        val expected = GetVehiclesUseCase.Output.UnknownError("test")
        val input = GetVehiclesUseCase.Input(userLocation())
        coEvery {
            repository.getVehicles()
        } returns flow { emit(throw provided) }

        useCase.execute(input).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }
}
