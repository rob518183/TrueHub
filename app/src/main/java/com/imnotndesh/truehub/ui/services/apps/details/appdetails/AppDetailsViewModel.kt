package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppDetailsViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _similarApps = MutableStateFlow<List<Apps.AppSimilarResponse>>(emptyList())
    val similarApps: StateFlow<List<Apps.AppSimilarResponse>> = _similarApps

    private val _isLoadingSimilar = MutableStateFlow(false)
    val isLoadingSimilar: StateFlow<Boolean> = _isLoadingSimilar

    fun loadSimilarApps(appName: String, train: String) {
        viewModelScope.launch {
            _isLoadingSimilar.value = true
            val result = manager.apps.getSimilarApps(appName, train)
            if (result is ApiResult.Success) {
                _similarApps.value = result.data
            }
            _isLoadingSimilar.value = false
        }
    }

    companion object {
        /**
         * Returns a Factory that injects the TrueNASApiManager.
         */
        fun provideFactory(manager: TrueNASApiManager): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppDetailsViewModel(manager)
            }
        }
    }
}