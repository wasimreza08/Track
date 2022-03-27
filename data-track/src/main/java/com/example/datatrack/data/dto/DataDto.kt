package com.example.datatrack.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataDto(
    @SerialName("type")
    val type: String,
    @SerialName("id")
    val id: String,
    @SerialName("attributes")
    val attributes: AttributesDto
)
