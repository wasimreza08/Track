package com.example.datatrack.data.api

import com.example.datatrack.data.dto.RootResponseDto
import retrofit2.http.GET

interface Api {
    @GET("9ec3a017-1c9d-47aa-8c38-ead2bfa9b339/c284fd9a-c94e-4bfa-8f26-3a04ddf15b47/")
    suspend fun getVehicles(): RootResponseDto
}
