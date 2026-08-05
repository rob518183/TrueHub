package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SystemInformationUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val bootId: String? = null,
    val hostId: String? = null,
    val state: String? = null,
    val ready: Boolean? = null,
    val version: String? = null,
    val versionShort: String? = null,
    val productType: String? = null,
    val releaseNotesUrl: String? = null,
    val dedupEnabled: Boolean? = null,
    val fibreChannelEnabled: Boolean? = null,
    val vmEnabled: Boolean? = null,
    val error: String? = null
)

class SystemInformationViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemInformationUiState())
    val uiState: StateFlow<SystemInformationUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            val hostIdDeferred = async { manager.system.getHostId() }
            val bootIdDeferred = async { manager.system.getBootId() }
            val stateDeferred = async { manager.system.getSystemState() }
            val readyDeferred = async { manager.system.isSystemReady() }
            val versionDeferred = async { manager.system.getSystemVersion() }
            val versionShortDeferred = async { manager.system.getSystemVersionShort() }
            val productTypeDeferred = async { manager.system.getProductType() }
            val releaseNotesDeferred = async { manager.system.getReleaseNotesUrl(null) }
            val dedupDeferred = async { manager.system.isFeatureEnabled("DEDUP") }
            val fcDeferred = async { manager.system.isFeatureEnabled("FIBRECHANNEL") }
            val vmDeferred = async { manager.system.isFeatureEnabled("VM") }

            val hostId = (hostIdDeferred.await() as? ApiResult.Success)?.data
            val bootId = (bootIdDeferred.await() as? ApiResult.Success)?.data
            val state = (stateDeferred.await() as? ApiResult.Success)?.data
            val ready = (readyDeferred.await() as? ApiResult.Success)?.data
            val version = (versionDeferred.await() as? ApiResult.Success)?.data
            val versionShort = (versionShortDeferred.await() as? ApiResult.Success)?.data
            val productType = (productTypeDeferred.await() as? ApiResult.Success)?.data
            val releaseNotesUrl = (releaseNotesDeferred.await() as? ApiResult.Success)?.data
            val dedup = (dedupDeferred.await() as? ApiResult.Success)?.data
            val fc = (fcDeferred.await() as? ApiResult.Success)?.data
            val vm = (vmDeferred.await() as? ApiResult.Success)?.data

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    bootId = bootId ?: it.bootId,
                    hostId = hostId ?: it.hostId,
                    state = state ?: it.state,
                    ready = ready ?: it.ready,
                    version = version ?: it.version,
                    versionShort = versionShort ?: it.versionShort,
                    productType = productType ?: it.productType,
                    releaseNotesUrl = releaseNotesUrl ?: it.releaseNotesUrl,
                    dedupEnabled = dedup ?: it.dedupEnabled,
                    fibreChannelEnabled = fc ?: it.fibreChannelEnabled,
                    vmEnabled = vm ?: it.vmEnabled
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class ViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SystemInformationViewModel(manager) as T
        }
    }
}
