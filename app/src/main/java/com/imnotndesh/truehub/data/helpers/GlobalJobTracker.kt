package com.imnotndesh.truehub.data.helpers

import android.content.Context
import android.content.Intent
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.JobNotificationService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.*

object GlobalJobTracker {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<Int, Job>()

    fun startTracking(
        context: Context,
        manager: TrueNASApiManager,
        jobId: Int,
        appName: String,
        showNotif: Boolean
    ) {
        if (jobs.containsKey(jobId)) return

        jobs[jobId] = scope.launch {
            var running = true
            while (running) {
                val result = manager.system.getJobInfoJobWithResult(jobId)
                if (result is ApiResult.Success) {
                    val data = result.data
                    val progress = data.progress?.percent ?: 0
                    val state = data.state
                    val isDone = state in listOf("SUCCESS", "FAILED", "ABORTED")

                    // 1. Update UI Repository
                    JobRepository.updateJob(TrackedJob(jobId, appName, state, progress, data.progress?.description))

                    // 2. Update Notification Service
                    if (showNotif) {
                        val intent = Intent(context, JobNotificationService::class.java).apply {
                            putExtra("id", jobId)
                            putExtra("name", appName)
                            putExtra("progress", progress)
                            putExtra("done", isDone)
                        }
                        context.startForegroundService(intent)
                    }

                    if (isDone) {
                        running = false
                        delay(5000)
                        JobRepository.removeJob(jobId)
                        continue
                    }
                }
                delay(2000)
            }
            jobs.remove(jobId)
        }
    }
}