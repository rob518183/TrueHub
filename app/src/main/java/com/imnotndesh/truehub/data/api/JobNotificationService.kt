package com.imnotndesh.truehub.data.api

import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.R

class JobNotificationService : LifecycleService() {
    private val nm by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private val CHANNEL_ID = "truehub_job_channel"

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL_ID, "Background Jobs", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val jobId = intent?.getIntExtra("id", -1) ?: -1
        val name = intent?.getStringExtra("name") ?: "Job"
        val progress = intent?.getIntExtra("progress", 0) ?: 0
        val isDone = intent?.getBooleanExtra("done", false) ?: false

        if (jobId != -1) {
            val notification = buildNotification(jobId, name, progress, isDone)
            startForeground(jobId, notification)

            if (isDone) {
                nm.notify(jobId, notification)
                stopForeground(STOP_FOREGROUND_DETACH)
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(id: Int, name: String, prog: Int, done: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (done) "Finished: $name" else "Upgrading $name")
            .setContentText(if (done) "Process completed successfully" else "$prog% complete")
            .setSmallIcon(R.drawable.missing_app_icon)
            .setOngoing(!done)
            .setProgress(100, prog, prog == 0 && !done)
            .setContentIntent(pi)
            .build()
    }
}