package com.example.datatrack.data.datasource

import app.cash.turbine.test
import com.example.datatrack.data.api.Api
import com.example.datatrack.data.dto.DataDto
import com.example.datatrack.data.dto.RootResponseDto
import com.example.datatrack.data.utils.TestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteDataSourceImplTest {
    private val service: Api = mockk()
    private val remoteDataSource = RemoteDataSourceImpl(service)

    @Test
    fun `test getVehicles with valid input return valid output`() = runTest {
        val provided = TestData.responseDto
        val expected = provided.data
        coEvery {
            service.getVehicles()
        } returns provided

        remoteDataSource.getRemoteVehicles().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `test getVehicles with empty input return empty output`() = runTest {
        val provided = RootResponseDto(arrayListOf())
        val expected = emptyList<DataDto>()
        coEvery {
            service.getVehicles()
        } returns provided

        remoteDataSource.getRemoteVehicles().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }
}
