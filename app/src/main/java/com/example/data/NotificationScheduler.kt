package com.example.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {
    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        activityId: String,
        triggerTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("activityId", activityId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            activityId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d("NotificationScheduler", "Scheduled notification for $title at $triggerTimeMillis")
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            Log.e("NotificationScheduler", "Exact alarms security restriction; fall back to normal alarm.", e)
        }
    }

    fun triggerInstantTestNotification(context: Context, title: String = "Test Reminder", message: String = "Bonjour! This is a test notification for your Paris trip.") {
        val triggerTime = System.currentTimeMillis() + 2000
        scheduleNotification(
            context,
            title = title,
            message = message,
            activityId = "test_instant",
            triggerTimeMillis = triggerTime
        )
    }

    fun scheduleAllTripReminders(context: Context) {
        val days = TripData.getItineraryDays()
        for (day in days) {
            val date = day.localDate
            for (act in day.activities) {
                val actDateTime = LocalDateTime.of(date, act.getLocalTime())
                val reminderDateTime = actDateTime.minusMinutes(15)
                val triggerTimeMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (triggerTimeMillis > System.currentTimeMillis()) {
                    scheduleNotification(
                        context,
                        title = act.title,
                        message = "Starting at ${act.timeLabel} today! Location: ${act.location}",
                        activityId = act.id,
                        triggerTimeMillis = triggerTimeMillis
                    )
                }
            }
        }
    }
}
