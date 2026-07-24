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

}