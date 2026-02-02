package com.example.todolist

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"

        // Intent to open the app when notification is clicked
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TASK_ID_TO_OPEN", taskId) // Pass ID to open specific task
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId, tapIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task due soon")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(taskId, notification)
    }
}