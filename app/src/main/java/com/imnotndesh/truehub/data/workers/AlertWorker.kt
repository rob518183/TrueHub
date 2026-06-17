package com.imnotndesh.truehub.data.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.dataStore
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AlertsWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "TrueNAS_Alerts_Sync"
        private const val CHANNEL_ID = "truehub_alerts_channel"
        private val SEEN_ALERTS_KEY = stringSetPreferencesKey("seen_alerts_ids")

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AlertsWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var client: TrueNASClient? = null
        try {
            val servers = MultiAccountPrefs.getServers(context)
            if (servers.isEmpty()) return@withContext Result.success()

            val activeServer = servers.first()

            client = TrueNASClient(Config.ClientConfig(
                serverUrl = activeServer.serverUrl,
                insecure = activeServer.insecure,
            ))
            client.connect()

            val manager = TrueNASApiManager(client, context)
            val apiKey = EncryptedPrefs.getApiKey(context)

            val authResult = if (!apiKey.isNullOrBlank()) {
                manager.auth.loginWithApiKeyWithResult(apiKey)
            } else {
                val user = EncryptedPrefs.getUsername(context)
                val pass = EncryptedPrefs.getUserPass(context)
                if (user != null && pass != null) {
                    manager.auth.loginUserWithResult(AuthService.DefaultAuth(user, pass))
                } else {
                    return@withContext Result.failure()
                }
            }

            if (authResult is ApiResult.Error) {
                client.disconnect()
                return@withContext Result.retry()
            }

            val alertsResult = manager.system.listAlertsWithResult()

            if (alertsResult is ApiResult.Success) {
                val unDismissedAlerts = alertsResult.data.filter { !it.dismissed }
                val currentActiveUuids = unDismissedAlerts.map { it.uuid }.toSet()

                val prefs = context.dataStore.data.first()
                val seenAlertUuids = prefs[SEEN_ALERTS_KEY] ?: emptySet()

                val newAlerts = unDismissedAlerts.filter { !seenAlertUuids.contains(it.uuid) }

                if (newAlerts.isNotEmpty()) {
                    notifyAlerts(newAlerts)
                }

                context.dataStore.edit { it[SEEN_ALERTS_KEY] = currentActiveUuids }
            }

            client.disconnect()
            Result.success()

        } catch (e: Exception) {
            Log.e("AlertsWorker", "Error fetching system alerts in background", e)
            client?.disconnect()
            Result.retry()
        }
    }

    private fun notifyAlerts(alerts: List<System.AlertResponse>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "System Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for TrueNAS system alerts"
        }
        notificationManager.createNotificationChannel(channel)

        alerts.forEach { alert ->
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("TrueNAS Alert: ${alert.level}")
                .setContentText(alert.formatted ?: "A new system alert has been triggered.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(alert.formatted))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(alert.uuid.hashCode(), notification)
        }
    }
}