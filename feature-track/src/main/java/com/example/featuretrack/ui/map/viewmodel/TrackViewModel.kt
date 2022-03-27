package com.example.featuretrack.ui.map.viewmodel

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.core.ext.exhaustive
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.domain.model.VehicleInfo
import com.example.domain.domain.usecase.GetNearestVehicleUseCase
import com.example.domain.domain.usecase.GetVehiclesUseCase
import com.example.featuretrack.mapper.DomainToUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getNearestVehicleUseCase: GetNearestVehicleUseCase
) : BaseViewModel<TrackContract.Event, TrackContract.State, TrackContract.Effect>() {

    init {
        onEvent(TrackContract.Event.OnViewModelInit)
    }

    private val mapper = DomainToUiMapper()
    private val domainList: MutableList<VehicleInfo> = mutableListOf()
    private var userLocation: Location? = null

    private fun loadData() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            getVehiclesUseCase.execute().collect { output ->
                Timber.e("data" + output.toString())
                when (output) {
                    is GetVehiclesUseCase.Output.Success -> {
                        domainList.addAll(output.vehicleList)
                        val uiVehicleList = output.vehicleList.map {
                            mapper.map(it)
                        }
                        updateState { copy(vehicles = uiVehicleList) }
                        getNearestVehicle()
                    }
                    is GetVehiclesUseCase.Output.NetworkError -> {
                        sendEffect { TrackContract.Effect.OnNetworkError }
                    }
                    is GetVehiclesUseCase.Output.UnknownError -> {
                        sendEffect { TrackContract.Effect.OnUnknownError(output.message) }
                    }
                }
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun getNearestVehicle() {
        if (domainList.isEmpty()) {
            return
        }
        userLocation?.let { location ->
            viewModelScope.launch {
                getNearestVehicleUseCase.execute(
                    GetNearestVehicleUseCase.Input(location, domainList)
                ).collect { output ->
                    when (output) {
                        is GetNearestVehicleUseCase.Output.Success -> {
                            val nearestVehicle = mapper.map(output.vehicle)
                            updateState {
                                copy(
                                    nearestVehicle = nearestVehicle
                                )
                            }
                        }
                        is GetNearestVehicleUseCase.Output.UnknownError -> {
                            sendEffect { TrackContract.Effect.OnUnknownError(output.message) }
                        }
                    }.exhaustive
                }
            }
        }
    }

    override fun provideInitialState(): TrackContract.State {
        return TrackContract.State()
    }

    override fun handleEvent(event: TrackContract.Event) {
        when (event) {
            is TrackContract.Event.OnLocationAccessed -> {
                userLocation = event.location
                getNearestVehicle()
            }
            is TrackContract.Event.OnViewModelInit -> {
                loadData()
            }
            is TrackContract.Event.OnRetry -> {
                loadData()
                sendEffect { TrackContract.Effect.OnRetryLocationAccess }
            }
            is TrackContract.Event.OnMarkerClickedClicked -> {
                event.marker.title?.let {
                    val items = domainList.filter { vehicleInfo ->
                        vehicleInfo.id == event.marker.title
                    }
                    val nearestVehicle = mapper.map(items.first())
                    updateState {
                        copy(
                            nearestVehicle = nearestVehicle
                        )
                    }
                }
            }
        }.exhaustive
    }
}
