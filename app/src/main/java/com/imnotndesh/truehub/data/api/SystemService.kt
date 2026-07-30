package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.ApiMethods.System.GET_GRAPH_DATA
import com.imnotndesh.truehub.data.api.ApiMethods.System.GET_POOL_DETAILS
import com.imnotndesh.truehub.data.models.System
import com.squareup.moshi.Types

class SystemService(val manager: TrueNASApiManager){
    suspend fun getSystemInfoWithResult(): ApiResult<System.SystemInfo> {
        return manager.callWithResult(
            method = ApiMethods.System.SYSTEM_INFO,
            params = listOf(),
            resultType = System.SystemInfo::class.java
        )
    }
    // Shutdown Call
    suspend fun shutdownSystemWithResult(reason: String): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.System.SHUTDOWN,
            params = listOf(reason),
            resultType = Any::class.java
        )
    }
    // Getting Disk Details
    suspend fun getPoolsWithResult(): ApiResult<List<System.Pool>> {
        return manager.callWithResult(
            method = GET_POOL_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, System.Pool::class.java)
        )
    }

    // Get Disk Info
    suspend fun getDisksWithResult(): ApiResult<List<System.DiskDetails>> {
        return manager.callWithResult(
            method = ApiMethods.System.GET_DISK_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, System.DiskDetails::class.java)
        )
    }

    // Get all possible graph types
    suspend fun getPossibleGraphsWithResult(): ApiResult<System.GraphResult>{
        return manager.callWithResult(
            method = ApiMethods.System.GET_GRAPHS,
            params = listOf(),
            resultType = System.GraphResult::class.java
        )
    }

    // Get actual graph reporting data
    suspend fun getReportingDataWithResult(
        graphs: List<System.ReportingGraphRequest>,
        query: System.ReportingGraphQuery? = null): ApiResult<List<System.ReportingGraphResponse>> {
        return manager.callWithResult(
            method = GET_GRAPH_DATA,
            params = listOf(graphs, query),
            resultType = Types.newParameterizedType(
                List::class.java,
                System.ReportingGraphResponse::class.java
            )
        )
    }

    // Get latest job info
    suspend fun getJobInfoJobWithResult(jobId: Int): ApiResult<System.Job> {
        val filters = listOf(listOf("id", "=", jobId))
        val arrayResult = manager.callWithResult<Array<System.Job>>(
            method = ApiMethods.System.GET_JOB_STATUS,
            params = listOf(filters),
            resultType = Array<System.Job>::class.java
        )
        return when (arrayResult) {
            is ApiResult.Success -> {
                val job = arrayResult.data.firstOrNull()
                if (job != null) {
                    ApiResult.Success(job)
                } else {
                    ApiResult.Error("Job with ID $jobId not found.")
                }
            }
            is ApiResult.Error -> {
                arrayResult
            }
            is ApiResult.Loading -> {
                arrayResult
            }
        }
    }
    suspend fun getActiveJobsWithResult(state: String = "RUNNING"): ApiResult<List<System.Job>> {
        val filters = listOf(listOf("state", "=", state))

        val arrayResult = manager.callWithResult<Array<System.Job>>(
            method = ApiMethods.System.GET_JOB_STATUS,
            params = listOf(filters),
            resultType = Array<System.Job>::class.java
        )

        return when (arrayResult) {
            is ApiResult.Success -> {
                ApiResult.Success(arrayResult.data.toList())
            }

            is ApiResult.Error -> {
                ApiResult.Error(arrayResult.message)
            }

            is ApiResult.Loading -> {
                ApiResult.Loading
            }
        }
    }


    // Alerts Info
    suspend fun dismissAlertWithResult(uuid: String): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.System.DISMISS_ALERT,
            params = listOf(uuid),
            resultType = Any::class.java
        )
    }

    /**
     * Fetch all alerts from system
     * @param none
     * @see ApiMethods.System.LIST_ALERTS
     */
    suspend fun listAlertsWithResult(): ApiResult<List<System.AlertResponse>>{
        val type = Types.newParameterizedType(List::class.java, System.AlertResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.LIST_ALERTS,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Fetch alert categories from server
     * @param none
     * @see ApiMethods.System.LIST_CATEGORIES
     */

    suspend fun listCategoriesWithResult(): ApiResult<List<System.AlertCategoriesResponse>>{
        val type = Types.newParameterizedType(List::class.java, System.AlertCategoriesResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.LIST_CATEGORIES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * List all alert policies from server
     * @return ArrayOf(String)
     * @param none
     */
    suspend fun listAlertPoliciesWithResult(): ApiResult<List<String>>{
        val type  = Types.newParameterizedType(List::class.java,String::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.LIST_POLICIES,
            params = listOf(),
            resultType = type
        )
    }
    /**
     * Restore alerts based on their `uuid`
     * @param uuid
     * @see ApiMethods.System.RESTORE_ALERTS
     * @return null
     */
    suspend fun restoreAlertWithResult(uuid:String): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.System.RESTORE_ALERTS,
            params = listOf(uuid),
            resultType = Any::class.java
        )
    }
        suspend fun getSystemUpdateVersions(): ApiResult<List<System.UpdateAvailableVersionsResponse>>{
            val result = Types.newParameterizedType(List::class.java, System.UpdateAvailableVersionsResponse::class.java)
            return manager.callWithResult(
                method = ApiMethods.System.GET_SYSTEM_UPDATE_VERSIONS,
                params = emptyList(),
                resultType = result
            )
        }

        suspend fun getSystemUpdatesConfig(): ApiResult<System.UpdateConfigResponse>{
            return manager.callWithResult(
                method = ApiMethods.System.GET_SYSTEM_UPDATE_CONFIG,
                params = emptyList(),
                resultType = System.UpdateConfigResponse::class.java
            )
        }

        suspend fun downloadUpdate(train: String ?= "null", version: String ?= "null"): ApiResult<Boolean>{
            return manager.callWithResult(
                method = ApiMethods.System.DOWNLOAD_SYSTEM_UPDATE_VERSION,
                params = listOf(train,version),
                resultType = Boolean::class.java
            )
        }
        suspend fun getUpdateProfiles(): ApiResult<System.UpdateProfileChoicesResponse>{
            return manager.callWithResult(
                method = ApiMethods.System.GET_UPDATE_PROFILES,
                params = emptyList(),
                resultType = System.UpdateProfileChoicesResponse::class.java
            )
        }
        /** Updates system with cached update files **/
        suspend fun runSystemUpdate(options: System.UpdateRunDefaults ?= System.UpdateRunDefaults()): ApiResult<Int>{
            return manager.callWithResult(
                method = ApiMethods.System.RUN_SYSTEM_UPDATES,
                params = listOf(options),
                resultType = Int::class.java
            )
        }

        suspend fun getUpdateStatus(): ApiResult<System.UpdateStatusResponse>{
            return manager.callWithResult(
                method = ApiMethods.System.GET_UPDATE_STATUS,
                params = emptyList(),
                resultType = System.UpdateStatusResponse::class.java
            )
        }

        /** Update the update profile / config **/
        suspend fun updateSystemUpdateProfileConfig(autoCheck : Boolean ?= false, profile : String): ApiResult<System.ProfileUpdateResult>{
            return manager.callWithResult(
                method = ApiMethods.System.UPDATE_SYSTEM_UPDATE_CONFIGURATION,
                params = listOf(autoCheck,profile),
                resultType = System.ProfileUpdateResult::class.java
            )

        }
    suspend fun cancelJob(id: Int): ApiResult<Unit>{
        return manager.callWithResult(
            method = ApiMethods.System.STOP_JOB,
            params = listOf(id),
            resultType = Unit::class.java
        )
    }

    // Services calls
    suspend fun getInstanceServices(): ApiResult<List<System.ServiceQueryResponse>>{
        val result = Types.newParameterizedType(List::class.java, System.ServiceQueryResponse::class.java)
        return manager.callWithResult(
            ApiMethods.System.GET_SERVICES,
            emptyList(),
            result
        )
    }

    suspend fun getInstanceServiceInstance(id : Int) : ApiResult<System.ServiceQueryResponse>{
        return manager.callWithResult(
            ApiMethods.System.GET_SERVICE_INSTANCE,
            listOf(id),
            System.ServiceQueryResponse::class.java
        )
    }
    suspend fun controlService(action : System.ServiceControlOptions,service : String, options: System.ServiceControlCallOptions): ApiResult<Double>{
        return manager.callWithResult(
            ApiMethods.System.CONTROL_SERVICE,
            listOf(action,service,options),
            Double::class.java
        )
    }

    suspend fun isServiceStarted(service:String): ApiResult<Boolean>{
        return manager.callWithResult(
            ApiMethods.System.IS_SERVICE_STARTED,
            listOf(service),
            Boolean::class.java
        )
    }

    suspend fun isServiceStartedOrEnabled(service:String): ApiResult<Boolean>{
        return manager.callWithResult(
            ApiMethods.System.IS_SERVICE_STARTED_OR_ENABLED,
            listOf(service),
            Boolean::class.java
        )
    }

    suspend inline fun <reified T> updateService(
        serviceIdentifier: T,
        startOnBoot: Boolean
    ): ApiResult<Int> {
        val parameter = when (T::class) {
            String::class -> serviceIdentifier as String
            Int::class -> serviceIdentifier as Int
            else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
        }

        return manager.callWithResult(
            ApiMethods.System.UPDATE_SERVICE,
            listOf(parameter, System.ServiceUpdate(startOnBoot)),
            Int::class.java
        )
    }

    /**
     * Create a new user account.
     */
    suspend fun createUserWithResult(create: System.UserCreate): ApiResult<System.UserCreateUpdateResult> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_CREATE,
            params = listOf(create),
            resultType = System.UserCreateUpdateResult::class.java
        )
    }

    /**
     * Delete a user account by id.
     */
    suspend fun deleteUserWithResult(id: Int, options: System.UserDeleteOptions = System.UserDeleteOptions()): ApiResult<Int> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_DELETE,
            params = listOf(id, options),
            resultType = Int::class.java
        )
    }

    /**
     * Returns a single user instance matching id.
     */
    suspend fun getUserInstanceWithResult(id: Int): ApiResult<System.UserCreateUpdateResult> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_GET_INSTANCE,
            params = listOf(id),
            resultType = System.UserCreateUpdateResult::class.java
        )
    }

    /**
     * Get the next available/free UID.
     */
    suspend fun getNextUidWithResult(): ApiResult<Int> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_GET_NEXT_UID,
            params = emptyList(),
            resultType = Int::class.java
        )
    }

    /**
     * Returns struct passwd info for a user by username or uid.
     */
    suspend fun getUserObjWithResult(args: System.UserGetUserObjArgs): ApiResult<System.UserGetUserObj> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_GET_USER_OBJ,
            params = listOf(args),
            resultType = System.UserGetUserObj::class.java
        )
    }

    /**
     * Returns whether a local administrator with a valid password exists.
     */
    suspend fun hasLocalAdministratorSetUpWithResult(): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_HAS_LOCAL_ADMINISTRATOR_SET_UP,
            params = emptyList(),
            resultType = Boolean::class.java
        )
    }

    /**
     * Query users with filters and options.
     */
    suspend fun queryUsersWithResult(
        filters: List<Any> = emptyList(),
        options: System.UserQueryOptions = System.UserQueryOptions()
    ): ApiResult<List<System.UserCreateUpdateResult>> {
        val resultType = Types.newParameterizedType(List::class.java, System.UserCreateUpdateResult::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.USER_QUERY,
            params = listOf(filters, options),
            resultType = resultType
        )
    }

    /**
     * Renew a user's two-factor authentication secret.
     */
    suspend fun renewUser2faSecretWithResult(args: System.UserRenew2faSecretArgs): ApiResult<System.UserRenew2faSecretResult> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_RENEW_2FA_SECRET,
            params = listOf(args.username, args.twofactor_options),
            resultType = System.UserRenew2faSecretResult::class.java
        )
    }

    /**
     * Set the password of a user.
     */
    suspend fun setUserPasswordWithResult(args: System.UserSetPasswordArgs): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_SET_PASSWORD,
            params = listOf(args),
            resultType = Any::class.java
        )
    }

    /**
     * Set up local administrator (no auth required if not already set up).
     */
    suspend fun setupLocalAdministratorWithResult(args: System.UserSetupLocalAdministratorArgs): ApiResult<Any> {
        val params = if (args.options != null) {
            listOf(args.username, args.password, args.options)
        } else {
            listOf(args.username, args.password)
        }
        return manager.callWithResult(
            method = ApiMethods.System.USER_SETUP_LOCAL_ADMINISTRATOR,
            params = params,
            resultType = Any::class.java
        )
    }

    /**
     * Return available shell choices for user.create and user.update.
     */
    suspend fun getShellChoicesWithResult(groupIds: List<Int> = emptyList()): ApiResult<Map<String, String>> {
        val resultType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.USER_SHELL_CHOICES,
            params = listOf(groupIds),
            resultType = resultType
        )
    }

    /**
     * Unset two-factor authentication secret for a user.
     */
    suspend fun unsetUser2faSecretWithResult(username: String): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_UNSET_2FA_SECRET,
            params = listOf(username),
            resultType = Any::class.java
        )
    }

    /**
     * Update attributes of an existing user account.
     */
    suspend fun updateUserWithResult(id: Int, update: System.UserUpdate): ApiResult<System.UserCreateUpdateResult> {
        return manager.callWithResult(
            method = ApiMethods.System.USER_UPDATE,
            params = listOf(id, update),
            resultType = System.UserCreateUpdateResult::class.java
        )
    }
    /**
     * API key management
     */

    /**
     * Creates a new API key.
     */
    suspend fun createApiKeyWithResult(create: System.ApiKeyCreate): ApiResult<System.ApiKeyEntryWithKey> {
        return manager.callWithResult(
            method = ApiMethods.System.API_KEY_CREATE,
            params = listOf(create),
            resultType = System.ApiKeyEntryWithKey::class.java
        )
    }

    /**
     * Delete an API key by id.
     */
    suspend fun deleteApiKeyWithResult(id: Int): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.System.API_KEY_DELETE,
            params = listOf(id),
            resultType = Boolean::class.java
        )
    }

    /**
     * Returns a single API key instance matching id.
     */
    suspend fun getApiKeyInstanceWithResult(id: Int): ApiResult<System.ApiKeyEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.API_KEY_GET_INSTANCE,
            params = listOf(id),
            resultType = System.ApiKeyEntry::class.java
        )
    }

    /**
     * Get the existing API keys for the currently-authenticated user.
     */
    suspend fun getMyApiKeysWithResult(): ApiResult<List<System.ApiKeyEntry>> {
        val resultType = Types.newParameterizedType(List::class.java, System.ApiKeyEntry::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.API_KEY_MY_KEYS,
            params = emptyList(),
            resultType = resultType
        )
    }

    /**
     * Query API keys with filters and options.
     */
    suspend fun queryApiKeysWithResult(
        filters: List<Any> = emptyList(),
        options: System.UserQueryOptions = System.UserQueryOptions()
    ): ApiResult<List<System.ApiKeyEntry>> {
        val resultType = Types.newParameterizedType(List::class.java, System.ApiKeyEntry::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.API_KEY_QUERY,
            params = listOf(filters, options),
            resultType = resultType
        )
    }
    // ──────────────────────────────────────────────
    // System General Settings
    // ──────────────────────────────────────────────

    /**
     * Check-in UI settings changes to prevent automatic rollback.
     * Must be called within the rollback_timeout after updating settings.
     * Returns null on success.
     */
    suspend fun generalCheckin(): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_CHECKIN,
            params = listOf(),
            resultType = Any::class.java
        )
    }

    /**
     * Returns the number of seconds before an automatic rollback,
     * or null if no rollback is pending.
     */
    suspend fun generalCheckinWaiting(): ApiResult<Int?> {
        // checkin_waiting can legitimately return null (no pending rollback).
        // The RPC layer throws on null for non-Unit types, so we catch that
        // and return a success with null.
        return try {
            manager.callWithResult<Int>(
                method = ApiMethods.System.GENERAL_CHECKIN_WAITING,
                params = listOf(),
                resultType = Int::class.java
            ).let { result ->
                @Suppress("UNCHECKED_CAST")
                result as ApiResult<Int?>
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("null result", ignoreCase = true)) {
                ApiResult.Success(null)
            } else {
                ApiResult.Error(msg, e)
            }
        }
    }

    /**
     * Get the current system general configuration.
     */
    suspend fun generalConfig(): ApiResult<System.SystemGeneralEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_CONFIG,
            params = listOf(),
            resultType = System.SystemGeneralEntry::class.java
        )
    }

    /**
     * Returns a dictionary of country codes (ISO 3166-1 alpha-2) to country names.
     */
    suspend fun generalCountryChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_COUNTRY_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Returns keyboard map choices.
     */
    suspend fun generalKbdmapChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_KBDMAP_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Returns the configured local URL in the format of protocol://host:port.
     */
    suspend fun generalLocalUrl(): ApiResult<String> {
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_LOCAL_URL,
            params = listOf(),
            resultType = String::class.java
        )
    }

    /**
     * Returns available timezone choices.
     */
    suspend fun generalTimezoneChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_TIMEZONE_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Returns network interfaces with statically configured IPv4 addresses
     * that can be used to bind the UI server.
     */
    suspend fun generalUiAddressChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_UI_ADDRESS_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Returns available certificates that may be used to bind the webserver
     * when connecting via HTTPS.
     */
    suspend fun generalUiCertificateChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_UI_CERTIFICATE_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Returns available HTTPS protocol choices.
     */
    suspend fun generalUiHttpsProtocolsChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_UI_HTTPSPROTOCOLS_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Restart the HTTP server to apply latest UI settings.
     * @param delay Seconds to wait before restart (default: 3, minimum: 0)
     */
    suspend fun generalUiRestart(delay: Int = 3): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_UI_RESTART,
            params = listOf(delay),
            resultType = Any::class.java
        )
    }

    /**
     * Returns network interfaces with statically configured IPv6 addresses
     * that can be used to bind the UI server.
     */
    suspend fun generalUiV6AddressChoices(): ApiResult<Map<String, String>> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_UI_V6ADDRESS_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Update the system general service configuration.
     *
     * UI configuration is not applied automatically. Use [generalUiRestart]
     * or specify [uiRestartDelay] in the args to apply changes.
     *
     * If incorrect UI config is applied, specify [rollbackTimeout] to enable
     * automatic rollback. Call [generalCheckin] within that window to confirm.
     *
     * @param settings The general settings to update.
     * @return The updated system general configuration.
     */
    suspend fun generalUpdate(
        settings: System.SystemGeneralUpdateArgs
    ): ApiResult<System.SystemGeneralEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.GENERAL_UPDATE,
            params = listOf(settings),
            resultType = System.SystemGeneralEntry::class.java
        )
    }

    suspend fun updateApiKeyWithResult(
        id: Int,
        update: System.ApiKeyUpdate
    ): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.System.API_KEY_UPDATE,
            params = listOf(id, update),
            resultType = Any::class.java
        )
    }

    // system.advanced.* calls
    suspend fun advancedConfig(): ApiResult<System.SystemAdvancedEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.GET_ADVANCED_CONFIG,
            params = emptyList(),
            resultType = System.SystemAdvancedEntry::class.java
        )
    }

    suspend fun advancedGpuPciChoices(): ApiResult<Map<String, String>> {
        val result = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.GET_GPU_PCI_CHOICES,
            params = emptyList(),
            resultType = result
        )
    }

    suspend fun advancedLoginBanner(): ApiResult<String> {
        return manager.callWithResult(
            method = ApiMethods.System.GET_LOGIN_BANNER,
            params = emptyList(),
            resultType = String::class.java
        )
    }

    suspend fun advancedSedGlobalPassword(): ApiResult<String> {
        return manager.callWithResult(
            method = ApiMethods.System.GET_SED_GLOBAL_PASSWORD,
            params = emptyList(),
            resultType = String::class.java
        )
    }

    suspend fun advancedSedGlobalPasswordIsSet(): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.System.GET_SED_GLOBAL_PASSWORD_IS_SET,
            params = emptyList(),
            resultType = Boolean::class.java
        )
    }

    suspend fun advancedSerialPortChoices(): ApiResult<Map<String, String>> {
        val result = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.GET_SERIAL_PORT_CHOICES,
            params = emptyList(),
            resultType = result
        )
    }

    suspend fun advancedSyslogCertificateAuthorityChoices(): ApiResult<Map<String, String>> {
        val result = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.GET_SYSLOG_CERTIFICATE_AUTHORITY_CHOICES,
            params = emptyList(),
            resultType = result
        )
    }

    suspend fun advancedSyslogCertificateChoices(): ApiResult<Map<String, String>> {
        val result = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.GET_SYSLOG_CERTIFICATE_CHOICES,
            params = emptyList(),
            resultType = result
        )
    }

    suspend fun advancedUpdate(data: System.SystemAdvancedUpdateArgs): ApiResult<System.SystemAdvancedEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.UPDATE_ADVANCED_CONFIG,
            params = listOf(data),
            resultType = System.SystemAdvancedEntry::class.java
        )
    }

    // ──────────────────────────────────────────────
    // audit.* service methods
    // ──────────────────────────────────────────────

    /**
     * Get the current audit configuration.
     * Required role: SYSTEM_AUDIT_READ
     */
    suspend fun auditConfig(): ApiResult<System.AuditEntry> {
        return manager.callWithResult(
            method = ApiMethods.Audit.CONFIG,
            params = emptyList(),
            resultType = System.AuditEntry::class.java
        )
    }

    /**
     * Update default audit settings.
     * Required role: SYSTEM_AUDIT_WRITE
     *
     * Note: `space`, `remote_logging_enabled`, and `enabled_services` are
     * read-only fields returned in the response but ignored on input.
     */
    suspend fun auditUpdate(data: System.AuditUpdateArgs): ApiResult<System.AuditEntry> {
        return manager.callWithResult(
            method = ApiMethods.Audit.UPDATE,
            params = listOf(data),
            resultType = System.AuditEntry::class.java
        )
    }

    /**
     * Query audit database contents by service.
     * Required role: SYSTEM_AUDIT_READ
     *
     * Returns either a count (Int), a single result (List<AuditQueryResultItem>),
     * or a list of results depending on query-options (count, get).
     */
    suspend fun auditQuery(data: System.AuditQueryArgs): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.Audit.QUERY,
            params = listOf(data),
            resultType = Any::class.java
        )
    }

    /**
     * Generate an audit report. Returns a local filesystem path to the report.
     * Required role: SYSTEM_AUDIT_READ
     *
     * Supported export_formats: "CSV", "JSON", "YAML"
     * This method is a job — use with core.download to retrieve the file.
     */
    suspend fun auditExport(data: System.AuditExportArgs): ApiResult<String> {
        return manager.callWithResult(
            method = ApiMethods.Audit.EXPORT,
            params = listOf(data),
            resultType = String::class.java
        )
    }

    /**
     * Download an audit report by name.
     * Required role: SYSTEM_AUDIT_READ
     *
     * This method is a job. Users can only download reports they generated.
     * MUST be used with core.download.
     */
    suspend fun auditDownloadReport(data: System.AuditDownloadReportArgs): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.Audit.DOWNLOAD_REPORT,
            params = listOf(data),
            resultType = Any::class.java
        )
    }

    suspend fun advancedUpdateGpuPciIds(pciIds: List<String>): ApiResult<Unit> {
        return manager.callWithResult(
            method = ApiMethods.System.UPDATE_GPU_PCI_IDS,
            params = listOf(pciIds),
            resultType = Unit::class.java
        )
    }

    // ──────────────────────────────────────────────
    // network.general.* / network.configuration.* service methods
    // ──────────────────────────────────────────────

    /**
     * Retrieve general network information (IPs, default routes, nameservers).
     * Required role: NETWORK_GENERAL_READ
     */
    suspend fun networkGeneralSummary(): ApiResult<System.NetworkGeneralSummaryResult> {
        return manager.callWithResult(
            method = ApiMethods.System.NETWORK_GENERAL_SUMMARY,
            params = emptyList(),
            resultType = System.NetworkGeneralSummaryResult::class.java
        )
    }

    /**
     * Returns allowed/forbidden network activity choices.
     * Required role: NETWORK_GENERAL_READ | READONLY_ADMIN
     */
    suspend fun networkConfigurationActivityChoices(): ApiResult<List<List<String>>> {
        val type = Types.newParameterizedType(
            List::class.java,
            Types.newParameterizedType(List::class.java, String::class.java)
        )
        return manager.callWithResult(
            method = ApiMethods.System.NETWORK_CONFIGURATION_ACTIVITY_CHOICES,
            params = emptyList(),
            resultType = type
        )
    }

    /**
     * Get the current network configuration.
     * Required role: NETWORK_GENERAL_READ
     */
    suspend fun networkConfigurationConfig(): ApiResult<System.NetworkConfigurationEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.NETWORK_CONFIGURATION_CONFIG,
            params = emptyList(),
            resultType = System.NetworkConfigurationEntry::class.java
        )
    }

    /**
     * Update network configuration.
     * Required role: NETWORK_GENERAL_WRITE
     *
     * @param data The fields to update (all optional, only provided fields are changed).
     * @return The updated network configuration.
     */
    suspend fun networkConfigurationUpdate(data: System.NetworkConfigurationUpdateArgs): ApiResult<System.NetworkConfigurationEntry> {
        return manager.callWithResult(
            method = ApiMethods.System.NETWORK_CONFIGURATION_UPDATE,
            params = listOf(data),
            resultType = System.NetworkConfigurationEntry::class.java
        )
    }

}