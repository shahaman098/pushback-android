package com.pushback.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pushback.app.data.Settings
import java.util.Calendar

object AlarmIds {
    const val MORNING = 1001
    const val NUDGE = 1002
    const val EVENING = 1003
    const val DRIFT = 1004
    const val ACTION_MORNING = "com.pushback.app.ACTION_MORNING"
    const val ACTION_NUDGE = "com.pushback.app.ACTION_NUDGE"
    const val ACTION_EVENING = "com.pushback.app.ACTION_EVENING"
    const val ACTION_DRIFT = "com.pushback.app.ACTION_DRIFT"
    const val EXTRA_ROUTE = "route"
}

class AlarmScheduler(private val context: Context) {
    fun scheduleAll(settings: Settings) {
        schedule(AlarmIds.MORNING, AlarmIds.ACTION_MORNING, settings.wakeHour, settings.wakeMinute, "morning")
        schedule(AlarmIds.NUDGE, AlarmIds.ACTION_NUDGE, settings.nudgeHour, settings.nudgeMinute, "today")
        schedule(AlarmIds.EVENING, AlarmIds.ACTION_EVENING, settings.eveningHour, settings.eveningMinute, "evening")
        val kill = settings.killZoneStart
        if (kill != null) {
            val hour = (kill + 23) % 24
            schedule(AlarmIds.DRIFT, AlarmIds.ACTION_DRIFT, hour, 45, "focus")
        } else {
            cancel(AlarmIds.DRIFT)
        }
    }

    private fun schedule(id: Int, action: String, hour: Int, minute: Int, route: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val pi = pending(id, action, route)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }

    fun scheduleNudgeIn(minutes: Int, eveningHour: Int, eveningMinute: Int) {
        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes) }
        val evening = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, eveningHour)
            set(Calendar.MINUTE, eveningMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.after(evening)) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pending(AlarmIds.NUDGE, AlarmIds.ACTION_NUDGE, "today"),
        )
    }

    private fun cancel(id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(id, AlarmIds.ACTION_DRIFT, "focus"))
    }

    private fun pending(id: Int, action: String, route: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmIds.EXTRA_ROUTE, route)
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
