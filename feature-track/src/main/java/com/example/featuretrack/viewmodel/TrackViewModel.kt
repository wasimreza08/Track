package com.example.featuretrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repository.getVehicles().collect {
                Timber.e("data" + it.toString())
            }
        }
    }
}