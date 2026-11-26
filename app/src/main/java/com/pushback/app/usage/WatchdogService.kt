package com.pushback.app.usage

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pushback.app.MainActivity
import com.pushback.app.PushBackApp
import com.pushback.app.R
import com.pushback.app.notify.CoachNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WatchdogService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loop: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoachNotifications.ensureChannels(this)
        val notification = statusNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(42, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(42, notification)
        }
        if (loop?.isActive != true) {
            loop = scope.launch { watch() }
        }
        return START_STICKY
    }

    private suspend fun watch() {
        val app = application as PushBackApp
        val repo = app.container.repo
        var lastPkg = ""
        var lastSample = System.currentTimeMillis()
        while (scope.isActive) {
            val settings = repo.settings.current()
            if (!settings.watchdogEnabled) {
                stopSelf()
                return
            }
            val requiredOpen = repo.requiredOpen()
            val now = System.currentTimeMillis()
            if (now < settings.allowedBreakUntil) {
                delay(4000)
                continue
            }
            val pkg = if (repo.hasUsageAccess()) repo.currentForegroundPackage() else null
            if (pkg != null && pkg != packageName && pkg in settings.wastePackages && requiredOpen) {
                val minutes = (now - lastSample).coerceAtLeast(1000) / 60000f
                repo.logUsage(pkg, minutes.coerceAtLeast(0.1f), true)
                if (pkg != lastPkg) {
                    lastPkg = pkg
                    repo.bumpWasteInterrupt(null)
                    val remaining = repo.remainingRequired()
                    val label = repo.wasteLabel(pkg)
                    launchInterrupt(remaining, label)
                }
            } else if (pkg != null && pkg != packageName) {
                lastPkg = ""
            }
            lastSample = now
            delay(8000)
        }
    }

    private fun launchInterrupt(remaining: Int, appLabel: String) {
        val full = Intent(this, WasteInterruptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(WasteInterruptActivity.EXTRA_REMAINING, remaining)
            putExtra(WasteInterruptActivity.EXTRA_APP, appLabel)
        }
        val pi = PendingIntent.getActivity(
            this,
            99,
            full,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(this, CoachNotifications.CHANNEL_WASTE)
            .setSmallIcon(R.drawable.ic_stat_pushback)
            .setContentTitle("You still have $remaining applications.")
            .setContentText("$appLabel is not the job.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        startForeground(42, n)
        try {
            startActivity(full)
        } catch (_: Exception) {
        }
    }

    private fun statusNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CoachNotifications.CHANNEL_COACH)
            .setSmallIcon(R.drawable.ic_stat_pushback)
            .setContentTitle(getString(R.string.watchdog_title))
            .setContentText("Required work is unfinished. Time-wasters will be named.")
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        loop?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            val i = Intent(context, WatchdogService::class.java)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WatchdogService::class.java))
        }
    }
}
