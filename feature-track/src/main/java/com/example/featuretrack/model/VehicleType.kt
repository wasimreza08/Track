package com.example.featuretrack.model

import androidx.annotation.Keep

@Keep
enum class VehicleType(val type: String) {
    EBICYCLE("ebicycle"),
    ESCOOTER("escooter"),
    EMOPED("emoped")
}
