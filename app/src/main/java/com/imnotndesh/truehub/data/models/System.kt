package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


object System {
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class SystemInfo(
        @field:Json(name = "version")
        val version: String,
        @field:Json("buildtime")
        val buildTime: Map<String, Long>?,

        @field:Json("hostname")
        val hostname: String,
        @field:Json("physmem")
        val physmem: Long = 1,

        @field:Json("model")
        val model: String,

        @field:Json("cores")
        val cores: Double,

        @field:Json("physical_cores")
        val physical_cores: Int? = null,

        @field:Json("loadavg")
        val loadavg: List<Double>,

        @field:Json("uptime")
        val uptime: String,

        @field:Json("system_serial")
        val systemSerial: String?,

        @field:Json("system_product")
        val systemProduct: String?,

        @field:Json("system_product_version")
        val systemProductVersion: String?,

        @field:Json("license")
        val license: String?,

        @field:Json("boottime")
        val bootTime: Map<String, Long>?,

        @field:Json("datetime")
        val dateTime: Map<String, Long>?,

        @field:Json("timezone")
        val timezone: String,

        @field:Json("system_manufacturer")
        val system_manufacturer: String?,

        @field:Json("ecc_memory")
        val ecc_memory: Boolean
    )

    // Jobs Stuff
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class Job(
        val id: Int,
        val method: String,
        val arguments: List<Any>,
        val logs_path: String?,
        val logs_excerpt: String?,
        val result_encoding_error: String?,
        val progress: JobProgress?,
        val result: Any?,
        val error: Any?,
        val exception: String?,
        val exc_info: ExcInfo?,
        val state: String,
        val time_started: Map<String, Long>?,
        val time_finished: Any?,
        val credentials: Credentials?
    )

    @JsonClass(generateAdapter = true)
    data class JobProgress(
        val percent: Int,
        val description: String?,
        val extra: Any?
    )
    @JsonClass(generateAdapter = true)
    data class Credentials(
        val type: String?,
        val data: CredentialsData?
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class CredentialsData(
        val username: String?,
        val login_at: Map<String, Long>?
    )
    @JsonClass(generateAdapter = true)
    data class ExcInfo(
        val repr: String?,
        val type: String?,
        val errno: Double?,
        val extra: Any?
    )
    data class UpgradeJobState(
        val state: String,
        val progress: Int = 0,
        val description: String? = null
    )

    data class TrackedJob(
        val jobId: Int,
        val appName: String,
        val state: String = "PENDING",
        val progress: Int = 0,
        val description: String? = null,
        val showNotification: Boolean = false
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class GraphResult(
        val name :String,
        val title :String,
        val vertical_label : String,
        val identifiers : List<String>?,
    )
    // Graph request
    @JsonClass(generateAdapter = true)
    data class ReportingGraphQuery(
        val unit: ReportingUnit? = null,
        val page: Int? = 1,
        val aggregate: Boolean? = true,
        val start: Long? = null,
        val end: Long? = null
    )
    @JsonClass(generateAdapter = true)
    data class ReportingGraphRequest(
        val name: String,
        val identifier: String? = null
    ) {
        constructor(name: ReportingGraphName, identifier: String? = null) :
                this(name.value, identifier)
    }

    // Graph response
    @JsonClass(generateAdapter = true)
    data class ReportingGraphResponse(
        val name: String,
        val identifier: String?,
        val aggregations: Map<String, Map<String, Double>>?,
        val data: List<List<Double>>,
        val start: Int,
        val end: Int,
        val legend: List<String>
    )

    enum class ReportingGraphName(val value: String) {
        CPU("cpu"),
        CPUTEMP("cputemp"),
        DISK("disk"),
        INTERFACE("interface"),
        LOAD("load"),
        PROCESSES("processes"),
        MEMORY("memory"),
        UPTIME("uptime"),
        ARCACTUALRATE("arcactualrate"),
        ARCRATE("arcrate"),
        ARCSIZE("arcsize"),
        ARCRESULT("arcresult"),
        DISKTEMP("disktemp"),
        UPSCHARGE("upscharge"),
        UPSRUNTIME("upsruntime"),
        UPSVOLTAGE("upsvoltage"),
        UPSCURRENT("upscurrent"),
        UPSFREQUENCY("upsfrequency"),
        UPSLOAD("upsload"),
        UPSTEMPERATURE("upstemperature");
    }
    enum class ReportingUnit(val value: String) {
        HOUR("HOUR"),
        DAY("DAY"),
        WEEK("WEEK"),
        MONTH("MONTH"),
        YEAR("YEAR");
    }


    // Device stuff
    enum class DeviceType {
        DISK,
        SERIAL,
        GPU
    }
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class DeviceInfoQuery(
        val type: DeviceType? = null,
        val get_partitions: Boolean? = null,
        val serials_only: Boolean? = null
    )

    // Disk Details
    @Suppress("PropertyName")
    data class DiskDetails(
        val identifier: String,
        val name: String,
        val subsystem: String,
        val number: Int,
        val serial: String,
        val lunid: String?,
        val size: Long,
        val description: String,
        val transfermode: String,
        val hddstandby: String,
        val advpowermgmt: String,
        val togglesmart: Boolean?,
        val smartoptions: String?,
        val expiretime: String?,
        val critical: String?,
        val difference: String?,
        val informational: String?,
        val model: String?= null,
        val rotationrate: Int?,
        val type: String,
        val zfs_guid: String?,
        val bus: String,
        val devname: String,
        val enclosure: String?,
        val supports_smart: Boolean?,
        val pool: String?
    )

    // Pool Info
    @Suppress("PropertyName")
    data class Pool(
        val id: Int,
        val name: String,
        val guid: String,
        val status: String,
        val path: String,
        val scan: PoolScan? = null,
        val expand: PoolExpand? = null,
        val is_upgraded : Boolean ?= false,
        val healthy: Boolean,
        val warning: Boolean,
        val status_code: String ?= null,
        val status_detail: String? = null,
        val size: Long ?= null,
        val allocated: Long ?= null,
        val free: Long ?= null,
        val freeing: Long ?= null,
        val dedup_table_size: Long ?= null,
        val dedup_table_quota: String? = null,
        val fragmentation: String ?= null,
        val size_str: String? = null,
        val allocated_str: String ? = null,
        val free_str: String ? = null,
        val freeing_str: String ?= null,
        val autotrim: AutoTrim,
        val topology: PoolTopology ?= null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class PoolScan(
        val function: String?,
        val state: String?,
        val start_time: Map<String, Long>?,
        val end_time: Map<String, Long>?,
        val percentage: Double?,
        val bytes_to_process: Long?,
        val bytes_processed: Long?,
        val bytes_issued: Long?,
        val pause: String?,
        val errors: Int?,
        val total_secs_left: Long?
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class PoolExpand(
        val state: String?,
        val expanding_vdev: Any?,
        val start_time: Map<String, Long>?,
        val end_time: Map<String, Long>?,
        val bytes_to_reflow: Long?,
        val bytes_reflowed: Long?,
        val waiting_for_resilver: Boolean?,
        val total_secs_left: Long?,
        val percentage: Double?
    )

    data class PoolTopology(
        val data: List<PoolDevice>,
        val log: List<PoolDevice>,
        val cache: List<PoolDevice>,
        val spare: List<PoolDevice>,
        val special: List<PoolDevice>,
        val dedup: List<PoolDevice>
    )

    @Suppress("PropertyName")
    data class PoolDevice(
        val name: String,
        val type: String,
        val path: String,
        val guid: String,
        val status: String,
        val stats: PoolStats?,
        val children: List<PoolDevice>,
        val device: String?,
        val disk: String?,
        val unavail_disk: String?
    )

    @Suppress("PropertyName")
    data class PoolStats(
        val timestamp: Long,
        val read_errors: Int,
        val write_errors: Int,
        val checksum_errors: Int,
        val ops: List<Long>,
        val bytes: List<Long>,
        val size: Long,
        val allocated: Long,
        val fragmentation: Int,
        val self_healed: Int,
        val configured_ashift: Int,
        val logical_ashift: Int,
        val physical_ashift: Int
    )

    data class AutoTrim(
        val value: String,
        val rawvalue: String,
        val parsed: String,
        val source: String
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class AlertResponse(
        val uuid: String,
        val source: String,
        val klass: String,
        val args: Any? = null,
        val node: String,
        val key: String,
        val datetime: MongoDate,
        val last_occurrence: MongoDate,
        val dismissed: Boolean,
        val mail: Any? = null,
        val text: String,
        val id: String,
        val level: String,
        val formatted: String? = null,
        val one_shot: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class MongoDate(
        @Json(name = "\$date") val date: Long
    )

    @JsonClass(generateAdapter = true)
    data class AlertCategoriesResponse(
        val id : String,
        val title : String,
        val classes : List<AlertCategoriesClasses>,
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AlertCategoriesClasses(
        val id : String,
        val title:String,
        val level : String,
        val proactive_support: Boolean
    )
    @JsonClass(generateAdapter = true)
    data class UpdateAvailableVersionsResponse(
        val train : String,
        val version : UpdateAvailableVersionsVersions
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UpdateAvailableVersionsVersions(
        val version: String,
        val manifest: Map<String, Any>,
        val release_notes : String ?= null,
        val release_notes_url : String
    )

    @JsonClass(generateAdapter = true)
    data class UpdateConfigResponse(
        val id : Int,
        val autocheck: Boolean,
        val profile: String
    )
    @JsonClass(generateAdapter = true)
    data class UpdateDownloadParams(
        val train : String = "null",
        val version : String = "null"
    )
    @JsonClass(generateAdapter = true)
    data class UpdateProfileChoicesResponse(
        val name : String,
        val footnote :String,
        val description: String,
        val available : Boolean
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UpdateRunDefaults(
        val dataset_name : String ?= "null",
        val resume : Boolean ?= false,
        val train :String ?= "null",
        val version : String ?= "null",
        val reboot : Boolean ?= false
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UpdateStatusResponse(
        val code :String,
        val status : UpdateStatusOptions ?= null,
        val error : UpdateStatusErrorOptions ?= null,
        val upgrade_download_progress : UpdateDownloadProgress ?= null
    )
    @Suppress("PropertyName")
    data class UpdateStatusOptions(
        val current_version: CurrentVersionOptions ?= null,
        val new_version : NewVersionOptions ?= null
    )
    @Suppress("PropertyName")
    data class CurrentVersionOptions(
        val train :String,
        val profile : String,
        val matches_profile: Boolean
    )
    @Suppress("PropertyName")
    data class NewVersionOptions(
        val version :String,
        val manifest :String,
        val release_notes :String ?= null,
        val release_notes_url: String
    )
    @JsonClass(generateAdapter = true)
    data class UpdateStatusErrorOptions(
        val errname : String,
        val reason : String
    )
    @JsonClass(generateAdapter = true)
    data class UpdateDownloadProgress(
        val percent : Int,
        val description: String,
        val version : String
    )
    @JsonClass(generateAdapter = true)
    data class ProfileUpdateResult(
        val id : Int,
        val autocheck : Boolean,
        val profile : String
    )

    // Service structs
    @JsonClass(generateAdapter = true)
    data class ServiceQueryResponse(
        val id : Int,
        val service : String,
        val enable : Boolean,
        val state :String,
        val pids : List<Int>
    )
    enum class ServiceControlOptions{
        START,
        STOP,
        RESTART,
        RELOAD
    }
    data class ServiceUpdate(
        var enable : Boolean
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class ServiceControlCallOptions(
        val ha_propagate : Boolean = true,
        val silent : Boolean = true,
        val timeout : Int ?= 120
    )

    /** Configuration for creating a new user account. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserCreate(
        @field:Json(name = "uid") val uid: Int? = null,
        @field:Json(name = "username") val username: String,
        @field:Json(name = "home") val home: String = "/var/empty",
        @field:Json(name = "shell") val shell: String = "/usr/bin/zsh",
        @field:Json(name = "full_name") val full_name: String,
        @field:Json(name = "smb") val smb: Boolean = true,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: Int? = null,
        @field:Json(name = "groups") val groups: List<Int>? = null,
        @field:Json(name = "password_disabled") val password_disabled: Boolean = false,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean = false,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean = false,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "group_create") val group_create: Boolean = false,
        @field:Json(name = "home_create") val home_create: Boolean = false,
        @field:Json(name = "home_mode") val home_mode: String = "700",
        @field:Json(name = "password") val password: String? = null,
        @field:Json(name = "random_password") val random_password: Boolean = false
    )

    /** Fields that can be updated on an existing user account. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserUpdate(
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "home") val home: String? = null,
        @field:Json(name = "shell") val shell: String? = null,
        @field:Json(name = "full_name") val full_name: String? = null,
        @field:Json(name = "smb") val smb: Boolean? = null,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: Int? = null,
        @field:Json(name = "groups") val groups: List<Int>? = null,
        @field:Json(name = "password_disabled") val password_disabled: Boolean? = null,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean? = null,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean? = null,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "home_create") val home_create: Boolean? = null,
        @field:Json(name = "home_mode") val home_mode: String? = null,
        @field:Json(name = "password") val password: String? = null,
        @field:Json(name = "random_password") val random_password: Boolean? = null
    )

    /** Result returned by user.create and user.update. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserCreateUpdateResult(
        @field:Json(name = "id") val id: Int ?= null ,
        @field:Json(name = "uid") val uid: Int,
        @field:Json(name = "username") val username: String,
        @field:Json(name = "unixhash") val unixhash: String? = null,
        @field:Json(name = "smbhash") val smbhash: String? = null,
        @field:Json(name = "home") val home: String = "/var/empty",
        @field:Json(name = "shell") val shell: String = "/usr/bin/zsh",
        @field:Json(name = "full_name") val full_name: String,
        @field:Json(name = "builtin") val builtin: Boolean,
        @field:Json(name = "smb") val smb: Boolean = true,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: UserGroupEntry ?= null,
        @field:Json(name = "groups") val groups: List<Int>,
        @field:Json(name = "password_disabled") val password_disabled: Boolean = false,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean = false,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean = false,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "immutable") val immutable: Boolean,
        @field:Json(name = "twofactor_auth_configured") val twofactor_auth_configured: Boolean,
        @field:Json(name = "sid") val sid: String? = null,
        @field:Json(name = "last_password_change") val last_password_change: MongoDate? = null,
        @field:Json(name = "password_age") val password_age: Int? = null,
        @field:Json(name = "password_history") val password_history: List<String>? = null,
        @field:Json(name = "password_change_required") val password_change_required: Boolean,
        @field:Json(name = "roles") val roles: List<String>,
        @field:Json(name = "api_keys") val api_keys: List<Int>,
        @field:Json(name = "password") val password: String? = null
    )


    /** Primary group info embedded in user entries. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGroupEntry(
        @field:Json(name = "id") val id: Int ?= null,
        @field:Json(name = "bsdgrp_gid") val bsdgrp_gid: Int? = null,
        @field:Json(name = "bsdgrp_group") val bsdgrp_group: String? = null
    )


    /** Options for user.delete. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserDeleteOptions(
        @field:Json(name = "delete_group") val delete_group: Boolean = true
    )

    /** Arguments for user.get_user_obj. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGetUserObjArgs(
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "uid") val uid: Int? = null,
        @field:Json(name = "get_groups") val get_groups: Boolean = false,
        @field:Json(name = "sid_info") val sid_info: Boolean = false
    )

    /** Response from user.get_user_obj (struct passwd info). */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGetUserObj(
        @field:Json(name = "pw_name") val pw_name: String,
        @field:Json(name = "pw_gecos") val pw_gecos: String,
        @field:Json(name = "pw_dir") val pw_dir: String,
        @field:Json(name = "pw_shell") val pw_shell: String,
        @field:Json(name = "pw_uid") val pw_uid: Int,
        @field:Json(name = "pw_gid") val pw_gid: Int,
        @field:Json(name = "grouplist") val grouplist: List<Int>? = null,
        @field:Json(name = "sid") val sid: String? = null,
        @field:Json(name = "source") val source: String,
        @field:Json(name = "local") val local: Boolean
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserSetPasswordArgs(
        @field:Json(name = "username") val username: String,
        @field:Json(name = "old_password") val old_password: String? = null,
        @field:Json(name = "new_password") val new_password: String
    )

    @JsonClass(generateAdapter = true)
    data class UserSetupLocalAdministratorArgs(
        @field:Json(name = "username") val username: String,
        @field:Json(name = "password") val password: String,
        @field:Json(name = "options") val options: UserSetupLocalAdministratorOptions? = null
    )

    @JsonClass(generateAdapter = true)
    data class UserSetupLocalAdministratorOptions(
        @field:Json(name = "ec2") val ec2: UserSetupLocalAdministratorEC2Options? = null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserSetupLocalAdministratorEC2Options(
        @field:Json(name = "instance_id") val instance_id: String
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserRenew2faSecretResult(
        @field:Json(name = "id") val id: Int?,
        @field:Json(name = "uid") val uid: Int,
        @field:Json(name = "username") val username: String,
        @field:Json(name = "unixhash") val unixhash: String? = null,
        @field:Json(name = "smbhash") val smbhash: String? = null,
        @field:Json(name = "home") val home: String = "/var/empty",
        @field:Json(name = "shell") val shell: String = "/usr/bin/zsh",
        @field:Json(name = "full_name") val full_name: String,
        @field:Json(name = "builtin") val builtin: Boolean,
        @field:Json(name = "smb") val smb: Boolean = true,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: UserGroupEntry,
        @field:Json(name = "groups") val groups: List<Int>,
        @field:Json(name = "password_disabled") val password_disabled: Boolean = false,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean = false,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean = false,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "immutable") val immutable: Boolean,
        @field:Json(name = "twofactor_auth_configured") val twofactor_auth_configured: Boolean,
        @field:Json(name = "sid") val sid: String? = null,
        @field:Json(name = "last_password_change") val last_password_change: MongoDate? = null,
        @field:Json(name = "password_age") val password_age: Int? = null,
        @field:Json(name = "password_history") val password_history: List<String>? = null,
        @field:Json(name = "password_change_required") val password_change_required: Boolean,
        @field:Json(name = "roles") val roles: List<String>,
        @field:Json(name = "api_keys") val api_keys: List<Int>,
        @field:Json(name = "twofactor_config") val twofactor_config: UserTwofactorConfigEntry
    )


    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserTwofactorConfigEntry(
        @field:Json(name = "provisioning_uri") val provisioning_uri: String? = null,
        @field:Json(name = "secret_configured") val secret_configured: Boolean,
        @field:Json(name = "interval") val interval: Int,
        @field:Json(name = "otp_digits") val otp_digits: Int
    )

    /** Arguments for user.renew_2fa_secret. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserRenew2faSecretArgs(
        @field:Json(name = "username") val username: String,
        @field:Json(name = "twofactor_options") val twofactor_options: UserTwofactorOptions
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserTwofactorOptions(
        @field:Json(name = "otp_digits") val otp_digits: Int = 6,
        @field:Json(name = "interval") val interval: Int = 30
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserQueryOptions(
        @field:Json(name = "extra") val extra: Map<String, Any>? = null,
        @field:Json(name = "order_by") val order_by: List<String>? = null,
        @field:Json(name = "select") val select: List<Any>? = null,
        @field:Json(name = "count") val count: Boolean = false,
        @field:Json(name = "get") val get: Boolean = false,
        @field:Json(name = "offset") val offset: Int = 0,
        @field:Json(name = "limit") val limit: Int = 0,
        @field:Json(name = "force_sql_filters") val force_sql_filters: Boolean = false
    )

    @JsonClass(generateAdapter = true)
    data class UserShellChoice(
        @field:Json(name = "path") val path: String,
        @field:Json(name = "name") val name: String
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyCreate(
        @field:Json(name = "name") val name: String = "nobody",
        @field:Json(name = "username") val username: String,
        @field:Json(name = "expires_at") val expires_at: String? = null
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyEntry(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "name") val name: String = "nobody",
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "user_identifier") val user_identifier: Any? = null,
        @field:Json(name = "keyhash") val keyhash: String,
        @field:Json(name = "created_at") val created_at: MongoDate? = null,
        @field:Json(name = "expires_at") val expires_at: MongoDate? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "revoked") val revoked: Boolean,
        @field:Json(name = "revoked_reason") val revoked_reason: String? = null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyEntryWithKey(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "name") val name: String = "nobody",
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "user_identifier") val user_identifier: Any? = null,
        @field:Json(name = "keyhash") val keyhash: String,
        @field:Json(name = "created_at") val created_at: MongoDate? = null,
        @field:Json(name = "expires_at") val expires_at: MongoDate? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "revoked") val revoked: Boolean,
        @field:Json(name = "revoked_reason") val revoked_reason: String? = null,
        @field:Json(name = "key") val key: String
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyUpdate(
        @field:Json(name = "name") val name: String? = null,
        @field:Json(name = "expires_at") val expires_at: String? = null,
        @field:Json(name = "reset") val reset: Boolean? = null
    )

    fun MongoDate?.formatDate(): String {
        if (this == null) return "—"
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getDefault()
        return sdf.format(java.util.Date(this.date))
    }


}