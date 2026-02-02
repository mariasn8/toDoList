package com.example.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.todolist.DB.Task

object NotificationScheduler {

    fun scheduleNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
        }

        // Unique request code per task ID prevents conflicts
        val pendingIntent = PendingIntent.getBroadcast(
            context, task.id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Calculate trigger time: Due Time - Offset (converted to millis)
        val triggerTime = task.dueTime - (task.notificationTimeOffset * 60 * 1000)

        // Only schedule if enabled and time hasn't passed
        if (task.notificationEnabled && triggerTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            // If disabled or time passed, ensure any old alarm is cancelled
            cancelNotification(context, task)
        }
    }

    fun cancelNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, task.id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}