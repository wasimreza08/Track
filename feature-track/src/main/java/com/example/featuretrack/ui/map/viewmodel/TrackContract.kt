package com.example.featuretrack.ui.map.viewmodel

import android.location.Location
import com.example.core.viewmodel.ViewEffect
import com.example.core.viewmodel.ViewEvent
import com.example.core.viewmodel.ViewState
import com.example.featuretrack.model.VehicleClusterItem
import com.example.featuretrack.model.VehicleUiInfo
import com.google.android.gms.maps.model.Marker

class TrackContract {
    data class State(
        val isLoading: Boolean = false,
        val vehicles: List<VehicleUiInfo> = emptyList(),
        val nearestVehicle: VehicleUiInfo? = null
    ) : ViewState

    sealed class Effect : ViewEffect {
        object FragmentStartEffect : Effect()
        data class UnknownErrorEffect(val message: String) : Effect()
        object NetworkErrorEffect : Effect()
        object RetryLocationAccessEffect : Effect()
        object OpenApplicationSettingsEffect : Effect()
        object OpenLocationSettingsEffect : Effect()
        object PermissionDeniedEffect : Effect()
        object PermissionRequestEffect : Effect()
    }

    sealed class Event : ViewEvent {
        data class OnLocationAccessed(val location: Location) : Event()
        object OnRetry : Event()
        object OnPermissionDenied : Event()
        data class OnMarkerClicked(val marker: Marker) : Event()
        data class OnClusterItemClicked(val clusterItem: VehicleClusterItem) : Event()
        object OnUnableDialogClicked : Event()
        object OnNoGpsDialogClicked : Event()
        object OnFragmentStart : Event()
        object OnPermissionRationaleDialogClicked : Event()
    }
}
