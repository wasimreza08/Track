package com.example.domain.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RootResponseDto(
    @SerialName("data")
    val data: ArrayList<DataDto>
)
