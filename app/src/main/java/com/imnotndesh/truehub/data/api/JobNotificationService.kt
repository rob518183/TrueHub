package com.imnotndesh.truehub.data.api

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class JobNotificationService : Service() {

    private companion object {
        const val CHANNEL_ID = "truehub_middleware_jobs"
        const val CHANNEL_NAME = "System Provisioning Status"
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private val activeJobsTracker = mutableSetOf<Int>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val jobId = intent.getIntExtra("id", -1)
        val appName = intent.getStringExtra("name") ?: "System Task"
        val progress = intent.getIntExtra("progress", 0)
        val isDone = intent.getBooleanExtra("done", false)
        val statusText = intent.getStringExtra("status_text") ?: "Provisioning resource nodes..."

        if (jobId != -1) {
            handler.removeCallbacksAndMessages(jobId)

            val notification = buildJobNotification(appName, progress, statusText, isDone)

            if (activeJobsTracker.isEmpty()) {
                activeJobsTracker.add(jobId)
                startForeground(jobId, notification)
            } else {
                activeJobsTracker.add(jobId)
                notificationManager.notify(jobId, notification)
            }

            if (isDone) {
                activeJobsTracker.remove(jobId)
                val token = jobId
                handler.postAtTime({
                    notificationManager.cancel(jobId)
                    checkAndShutdownService()
                }, token, android.os.SystemClock.uptimeMillis() + 2500)
            }
        }

        return START_NOT_STICKY
    }

    private fun checkAndShutdownService() {
        if (activeJobsTracker.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildJobNotification(
        appName: String,
        progress: Int,
        statusText: String,
        isDone: Boolean
    ): Notification {
        val titleText = if (isDone) "Complete: $appName" else appName
        val explicitStatus = if (isDone) "Infrastructure setup configured successfully." else statusText

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titleText)
            .setContentText(explicitStatus)
            .setSmallIcon(if (isDone) android.R.drawable.stat_sys_download_done else android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setOngoing(!isDone)
            .setProgress(100, if (isDone) 100 else progress, false)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tracks ongoing middleware application container deployments"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}