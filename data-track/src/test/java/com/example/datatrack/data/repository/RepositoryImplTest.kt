package com.example.datatrack.data.repository

import app.cash.turbine.test
import com.example.datatrack.data.datasource.RemoteDataSource
import com.example.datatrack.data.dto.DataDto
import com.example.datatrack.data.utils.TestData
import com.example.domain.domain.model.VehicleInfo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class RepositoryImplTest {
    private val remoteSource: RemoteDataSource = mockk()
    private val repository = RepositoryImpl(remoteSource)

    @Test
    fun `test getVehicles return valid output`() = runTest {
        val provided = TestData.dataDto
        val expected = listOf(TestData.vehicleInfo)
        coEvery {
            remoteSource.getRemoteVehicles()
        } returns flow { emit(listOf(provided)) }

        repository.getVehicles().test {
            Assert.assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getVehicles return empty output`() = runTest {
        val provided = emptyList<DataDto>()
        val expected = emptyList<VehicleInfo>()
        coEvery {
            remoteSource.getRemoteVehicles()
        } returns flow { emit(provided) }

        repository.getVehicles().test {
            Assert.assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }
}
