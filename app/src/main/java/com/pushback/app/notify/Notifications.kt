package com.pushback.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pushback.app.MainActivity
import com.pushback.app.PushBackApp
import com.pushback.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as PushBackApp
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                CoachNotifications.ensureChannels(context)
                val snap = app.container.repo.todaySnapshot().first()
                val route = intent.getStringExtra(AlarmIds.EXTRA_ROUTE) ?: "today"
                when (intent.action) {
                    AlarmIds.ACTION_MORNING -> {
                        if (snap.requiredRemaining > 0) {
                            CoachNotifications.post(
                                context,
                                1,
                                "${snap.requiredRemaining} applications still required.",
                                snap.nextJobRole?.let { "Next: ${snap.nextJobCompany} — $it. Open the listing. Do not plan." }
                                    ?: "Name the next job and send it. WOOP is not the work.",
                                "today",
                            )
                        }
                    }
                    AlarmIds.ACTION_NUDGE -> {
                        if (snap.requiredRemaining > 0) {
                            CoachNotifications.post(
                                context,
                                2,
                                "${snap.requiredRemaining} still required.",
                                "You have not sent enough. Open PushBack and submit the next one.",
                                "today",
                            )
                            val s = app.container.repo.settings.current()
                            AlarmScheduler(context).scheduleNudgeIn(40, s.eveningHour, s.eveningMinute)
                        }
                    }
                    AlarmIds.ACTION_EVENING -> {
                        if (!snap.eveningDone) {
                            CoachNotifications.post(
                                context,
                                3,
                                "Inspect the day.",
                                "What was supposed to happen. What happened. Tomorrow's if-then. Eight minutes.",
                                "evening",
                            )
                        }
                    }
                    AlarmIds.ACTION_DRIFT -> {
                        if (snap.requiredRemaining > 0) {
                            CoachNotifications.post(
                                context,
                                4,
                                "Kill zone. Quota still open.",
                                "This is the hour you usually drift. Send one now.",
                                "focus",
                            )
                        }
                    }
                }
                app.container.repo.ensureTodayPlan()
            } finally {
                pending.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val app = context.applicationContext as PushBackApp
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.repo.ensureTodayPlan()
            } finally {
                pending.finish()
            }
        }
    }
}

object CoachNotifications {
    const val CHANNEL_COACH = "coach"
    const val CHANNEL_WASTE = "waste"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_COACH, context.getString(R.string.channel_coach), NotificationManager.IMPORTANCE_HIGH),
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_WASTE, context.getString(R.string.channel_waste), NotificationManager.IMPORTANCE_HIGH).apply {
                setBypassDnd(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            },
        )
    }

    fun post(context: Context, id: Int, title: String, body: String, route: String) {
        ensureChannels(context)
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmIds.EXTRA_ROUTE, route)
        }
        val pi = PendingIntent.getActivity(
            context,
            id,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_COACH)
            .setSmallIcon(R.drawable.ic_stat_pushback)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, n)
        } catch (_: SecurityException) {
        }
    }

    const val STICKY_ID = 7

    fun stickyQuota(context: Context, remaining: Int) {
        ensureChannels(context)
        if (remaining <= 0) {
            NotificationManagerCompat.from(context).cancel(STICKY_ID)
            return
        }
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmIds.EXTRA_ROUTE, "today")
        }
        val pi = PendingIntent.getActivity(
            context,
            STICKY_ID,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_COACH)
            .setSmallIcon(R.drawable.ic_stat_pushback)
            .setContentTitle("$remaining applications still required")
            .setContentText("Open the next listing. Logging is not sending.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(STICKY_ID, n)
        } catch (_: SecurityException) {
        }
    }
}
