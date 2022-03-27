package com.example.featuretrack.ui.permission.viewmodel

import com.example.core.viewmodel.ViewEffect
import com.example.core.viewmodel.ViewEvent
import com.example.core.viewmodel.ViewState
import com.example.featuretrack.model.GeoLocation

class PermissionContract {
    object State : ViewState

    sealed class Effect : ViewEffect {
        data class NavigationEffect(val location: GeoLocation) : Effect()
        object OpenLocationSettingsEffect : Effect()
        object RequestPermissionEffect : Effect()
        object PermissionDeniedEffect : Effect()
        object FragmentStartEffect : Effect()
        object OpenApplicationSettingsEffect : Effect()
    }

    sealed class Event : ViewEvent {
        object OnFragmentStart : Event()
        object OnPermissionDenied : Event()
        object OnContinueClicked : Event()
        data class OnLocationAccessed(val location: GeoLocation) : Event()
        object OnNoGpsDialogClicked : Event()
        object OnUnableDialogClicked : Event()
    }
}