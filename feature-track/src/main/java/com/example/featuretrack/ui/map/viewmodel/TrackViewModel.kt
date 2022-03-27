package com.example.featuretrack.ui.map.viewmodel

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.core.ext.exhaustive
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.domain.usecase.GetVehiclesUseCase
import com.example.featuretrack.mapper.DomainToUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(
    private val getVehiclesUseCase: GetVehiclesUseCase
) : BaseViewModel<TrackContract.Event, TrackContract.State, TrackContract.Effect>() {

    private val mapper = DomainToUiMapper()

    private fun loadData(location: Location) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            getVehiclesUseCase.execute(GetVehiclesUseCase.Input(location)).collect { output ->
                when (output) {
                    is GetVehiclesUseCase.Output.Success -> {
                        val uiVehicleList = output.vehicleList.map {
                            mapper.map(it)
                        }
                        updateState {
                            copy(
                                vehicles = uiVehicleList,
                                nearestVehicle = uiVehicleList.first()
                            )
                        }
                    }
                    is GetVehiclesUseCase.Output.NetworkError -> {
                        sendEffect { TrackContract.Effect.NetworkErrorEffect }
                    }
                    is GetVehiclesUseCase.Output.UnknownError -> {
                        sendEffect { TrackContract.Effect.UnknownErrorEffect(output.message) }
                    }
                }
                updateState { copy(isLoading = false) }
            }
        }
    }

    override fun provideInitialState(): TrackContract.State {
        return TrackContract.State()
    }

    override fun handleEvent(event: TrackContract.Event) {
        when (event) {
            is TrackContract.Event.OnLocationAccessed -> {
                loadData(event.location)
            }
            is TrackContract.Event.OnMarkerClicked -> {
                event.marker.title?.let {
                    val items = viewState.value.vehicles.filter { vehicleInfo ->
                        vehicleInfo.id == event.marker.title
                    }
                    updateState {
                        copy(
                            nearestVehicle = items.first()
                        )
                    }
                }
            }
            is TrackContract.Event.OnFragmentStart, TrackContract.Event.OnRetry -> {
                sendEffect { TrackContract.Effect.InitLocationAccessEffect }
            }
            is TrackContract.Event.OnNoGpsDialogClicked -> {
                sendEffect { TrackContract.Effect.OpenLocationSettingsEffect }
            }
            is TrackContract.Event.OnUnableDialogClicked -> {
                sendEffect { TrackContract.Effect.OpenApplicationSettingsEffect }
            }
            is TrackContract.Event.OnPermissionDenied -> {
                sendEffect { TrackContract.Effect.PermissionDeniedEffect }
            }
            is TrackContract.Event.OnPermissionRationaleDialogClicked -> {
                sendEffect { TrackContract.Effect.PermissionRequestEffect }
            }
        }.exhaustive
    }
}
