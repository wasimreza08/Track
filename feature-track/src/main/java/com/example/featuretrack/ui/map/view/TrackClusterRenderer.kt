package com.example.featuretrack.ui.map.view

import android.content.Context
import com.example.featuretrack.common.Utils
import com.example.featuretrack.model.VehicleClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class TrackClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<VehicleClusterItem>
) : DefaultClusterRenderer<VehicleClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: VehicleClusterItem,
        markerOptions: MarkerOptions
    ) {
        val selectedDrawable = Utils.getVehicleDrawableId(item.snippetString)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(selectedDrawable))
    }
}
