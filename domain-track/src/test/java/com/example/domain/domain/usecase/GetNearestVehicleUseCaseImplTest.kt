package com.example.domain.domain.usecase

import android.location.Location
import app.cash.turbine.test
import com.example.core.dispatcher.BaseDispatcherProvider
import com.example.domain.utils.TestData
import com.example.domain.utils.TestDispatcherProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class GetNearestVehicleUseCaseImplTest {
    private val testDispatcher: BaseDispatcherProvider = TestDispatcherProvider()
    private val useCase = GetNearestVehicleUseCaseImpl(testDispatcher)

    @Test
    fun `test getNearestVehicleUseCase with valid input return valid output`() = runTest {
        val userLocation = Location("user")
        userLocation.latitude = 54.02
        userLocation.longitude = 14.02
        val provided = TestData.vehicleList
        val expected = GetNearestVehicleUseCase.Output.Success(TestData.vehicleInfo)

        useCase.execute(GetNearestVehicleUseCase.Input(userLocation, provided)).test {
            Assert.assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getNearestVehicleUseCase with empty list input return error output`() = runTest {
        val userLocation = Location("user")
        userLocation.latitude = Double.MAX_VALUE
        userLocation.longitude = Double.MAX_VALUE

        useCase.execute(GetNearestVehicleUseCase.Input(userLocation, emptyList())).test {
            Assert.assertTrue(awaitItem() is GetNearestVehicleUseCase.Output.UnknownError)
            awaitComplete()
        }
    }
}