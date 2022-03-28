package com.example.featuretrack.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class VehicleClusterItem(
    val lat: Double,
    val lng: Double,
    val titleString: String,
    val snippetString: String
) : ClusterItem {
    private val position: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return titleString
    }

    override fun getSnippet(): String {
        return snippetString
    }
}
