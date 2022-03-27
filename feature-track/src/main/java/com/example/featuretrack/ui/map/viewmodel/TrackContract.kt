package com.example.featuretrack.ui.map.viewmodel

import android.location.Location
import com.example.core.viewmodel.ViewEffect
import com.example.core.viewmodel.ViewEvent
import com.example.core.viewmodel.ViewState
import com.example.featuretrack.model.VehicleUiInfo
import com.google.android.gms.maps.model.Marker

class TrackContract {
    data class State(
        val isLoading: Boolean = false,
        val vehicles: List<VehicleUiInfo> = emptyList(),
        val nearestVehicle: VehicleUiInfo? = null
    ) : ViewState

    sealed class Effect : ViewEffect {
        data class OnUnknownError(val message: String) : Effect()
        object OnNetworkError : Effect()
        object OnRetryLocationAccess : Effect()
    }

    sealed class Event : ViewEvent {
        data class OnLocationAccessed(val location: Location) : Event()
        object OnViewModelInit : Event()
        object OnRetry : Event()
        data class OnMarkerClickedClicked(val marker: Marker) : Event()
    }
}
