package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NetworkConfigUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val config: System.NetworkConfigurationEntry? = null,
    val activityChoices: List<List<String>> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class NetworkConfigViewModel(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkConfigUiState())
    val uiState: StateFlow<NetworkConfigUiState> = _uiState.asStateFlow()

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val configResult = manager.system.networkConfigurationConfig()
                if (configResult is ApiResult.Error) {
                    _uiState.update { it.copy(isLoading = false, error = configResult.message) }
                    return@launch
                }
                val config = (configResult as ApiResult.Success).data

                val choicesResult = manager.system.networkConfigurationActivityChoices()
                val choices = if (choicesResult is ApiResult.Success) choicesResult.data else emptyList()

                _uiState.update {
                    it.copy(isLoading = false, config = config, activityChoices = choices)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unexpected error") }
            }
        }
    }

    fun updateConfig(data: System.NetworkConfigurationUpdateArgs) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false) }
            when (val result = manager.system.networkConfigurationUpdate(data)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, config = result.data, saveSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    class ViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NetworkConfigViewModel(manager) as T
        }
    }
}
