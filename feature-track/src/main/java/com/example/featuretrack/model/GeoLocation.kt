package com.example.featuretrack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoLocation(
    val lat: Double,
    val lng: Double
) : Parcelable
