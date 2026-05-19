package com.imnotndesh.truehub.ui.services.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.helpers.GlobalJobTracker
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.utils.AppCache
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

enum class AppCategory(val label: String) {
    ALL("All Apps"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    UPDATES("Has Updates")
}

data class AppsScreenUiState(
    val isLoading: Boolean = false,
    val apps: List<Apps.AppQueryResponse> = emptyList(),
    val marketplaceApps : List<Apps.AppAvailableItem> = emptyList(),
    val error: String? = null,
    val upgradeSummaryResult: Apps.AppUpgradeSummaryResult? = null,
    val isRefreshing: Boolean = false,
    val upgradeJobs: Map<String, System.UpgradeJobState> = emptyMap(),
    val isLoadingUpgradeSummaryForApp: String? = null,
    val rollbackVersions: List<String> = emptyList(),
    val isLoadingRollbackVersions: Boolean = false,
    val rollbackJobs: Map<String, System.UpgradeJobState> = emptyMap(),
    val selectedCategory: AppCategory = AppCategory.ALL
)

class AppsScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsScreenUiState())
    val uiState: StateFlow<AppsScreenUiState> = _uiState.asStateFlow()

    init {
        val cachedData = AppCache.cachedApps.value
        if (cachedData.isNotEmpty()) {
            _uiState.update { it.copy(apps = cachedData, isLoading = false) }
        }
        loadApps()
        startPeriodicRefresh()
    }


    fun updateCategory(category: AppCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(30000)
                if (_uiState.value.apps.isNotEmpty()) {
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
            }
        }
    }

    fun loadMarketplaceApps() {
        viewModelScope.launch {
            if (_uiState.value.marketplaceApps.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isRefreshing = true) }
            }
            try {
                when (val result = manager.apps.queryMarketplaceAvailableItems()) {
                    is ApiResult.Success -> {
                        AppCache.updateMarketplaceApps(result.data)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                marketplaceApps = result.data,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = if (it.marketplaceApps.isEmpty()) result.message else null
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (it.marketplaceApps.isEmpty()) e.message ?: "Failed to load apps" else null
                    )
                }
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            if (_uiState.value.apps.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isRefreshing = true) }
            }
            try {
                when (val result = manager.apps.getInstalledAppsWithResult()) {
                    is ApiResult.Success -> {
                        AppCache.updateApps(result.data)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                apps = result.data,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = if (it.apps.isEmpty()) result.message else null
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (it.apps.isEmpty()) e.message ?: "Failed to load apps" else null
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            loadApps()
        }
    }

    fun startApp(appName: String) {
        viewModelScope.launch {
            when (val result = manager.apps.startAppWithResult(appName)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Started Container")
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = if (_uiState.value.apps.isEmpty()) result.message else null) }
                }
                is ApiResult.Loading -> {
                    ToastManager.showInfo("Starting Container")
                }
            }
        }
    }

    fun stopApp(appName: String) {
        viewModelScope.launch {
            when (val result = manager.apps.stopAppWithResult(appName)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Stopped Container")
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = if (_uiState.value.apps.isEmpty()) result.message else null) }
                }
                is ApiResult.Loading -> {
                    ToastManager.showInfo("Stopping Container")
                }
            }
        }
    }

    fun upgradeApp(appName: String,context: Context ) {
        viewModelScope.launch {
            when (val result = manager.apps.upgradeAppWithResult(appName)) {
                is ApiResult.Success -> {
                    val jobId = result.data

                    GlobalJobTracker.startTracking(
                        context = context.applicationContext,
                        manager = manager,
                        jobId = jobId,
                        appName = appName,
                        showNotif = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
    fun loadUpgradeSummary(appName: String, appVersion: String? = "latest") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingUpgradeSummaryForApp = appName,
                error = null
            )
            try {
                when (val result = manager.apps.getUpgradeSummaryWithResult(appName, appVersion)) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpgradeSummaryForApp = appName,
                            upgradeSummaryResult = result.data,
                            error = null
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpgradeSummaryForApp = appName,
                            upgradeSummaryResult = null,
                            error = result.message
                        )
                    }
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpgradeSummaryForApp = appName,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUpgradeSummaryForApp = appName,
                    upgradeSummaryResult = null,
                    error = e.message ?: "Failed to load upgrade summary"
                )
            }
        }
    }

    fun clearUpgradeSummary() {
        _uiState.value = _uiState.value.copy(
            upgradeSummaryResult = null,
            isLoadingUpgradeSummaryForApp = null
        )
    }

    fun loadRollbackVersions(appName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingRollbackVersions = true,
                rollbackVersions = emptyList(),
                error = null
            )
            try {
                when (val result = manager.apps.getRollbackVersionsWithResult(appName)) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingRollbackVersions = false,
                            rollbackVersions = result.data,
                            error = null
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingRollbackVersions = false,
                            rollbackVersions = emptyList(),
                            error = result.message
                        )
                    }
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoadingRollbackVersions = true)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRollbackVersions = false,
                    rollbackVersions = emptyList(),
                    error = e.message ?: "Failed to load rollback versions"
                )
            }
        }
    }

    fun rollbackApp(appName: String, version: String, rollbackSnapshot: Boolean = true) {
        viewModelScope.launch {
            val result = manager.apps.rollbackAppWithResult(appName, version, rollbackSnapshot)
            when (result) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    _uiState.value = _uiState.value.copy(
                        rollbackJobs = _uiState.value.rollbackJobs + (
                                appName to System.UpgradeJobState(
                                    state = "ROLLING_BACK",
                                    progress = 0,
                                    description = "Starting rollback..."
                                )
                                )
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearRollbackVersions() {
        _uiState.value = _uiState.value.copy(
            rollbackVersions = emptyList(),
            isLoadingRollbackVersions = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class AppsScreenViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppsScreenViewModel::class.java)) {
                return AppsScreenViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
