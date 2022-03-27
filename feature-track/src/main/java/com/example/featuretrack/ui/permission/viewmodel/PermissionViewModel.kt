package com.example.featuretrack.ui.permission.viewmodel

import com.example.core.ext.exhaustive
import com.example.core.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor() :
    BaseViewModel<PermissionContract.Event, PermissionContract.State, PermissionContract.Effect>() {
    override fun provideInitialState(): PermissionContract.State {
        return PermissionContract.State
    }

    override fun handleEvent(event: PermissionContract.Event) {
        when (event) {
            is PermissionContract.Event.OnContinueClicked -> {
                sendEffect { PermissionContract.Effect.RequestPermissionEffect }
            }
            is PermissionContract.Event.OnLocationAccessed -> {
                sendEffect { PermissionContract.Effect.NavigationEffect(event.location) }
            }
            is PermissionContract.Event.OnNoGpsDialogClicked -> {
                sendEffect { PermissionContract.Effect.OpenLocationSettingsEffect }
            }
            is PermissionContract.Event.OnPermissionDenied -> {
                sendEffect { PermissionContract.Effect.PermissionDeniedEffect }
            }
            is PermissionContract.Event.OnFragmentStart -> {
                sendEffect { PermissionContract.Effect.FragmentStartEffect }
            }
            is PermissionContract.Event.OnUnableDialogClicked -> {
                sendEffect { PermissionContract.Effect.OpenApplicationSettingsEffect }
            }
        }.exhaustive
    }
}