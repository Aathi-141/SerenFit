package com.example.healthandfitness

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class WaterReminderReceiver : BroadcastReceiver() {

    // Called when the alarm triggers
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WaterReminder", "Water reminder triggered!")

        // Show a notification reminding user to drink water
        showWaterReminderNotification(context)

        // Schedule the next reminder
        rescheduleNextReminder(context)
    }

    private fun showWaterReminderNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Intent to open app when notification is clicked
            val contentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Fun, random messages to make notification engaging
            val messages = arrayOf(
                "💧 Time to hydrate! Your body will thank you!",
                "🚰 Drink up! Staying hydrated boosts your energy!",
                "🌟 Water break! Your skin is craving hydration!",
                "💫 Sip sip hooray! Time for a water refresh!",
                "🌊 Don't forget to drink! Hydration = Happiness!",
                "⚡ Quick water break! Fuel your amazing day!",
                "🎯 Hydration reminder! You're doing great!",
                "🌈 Time for water! Stay glowing and healthy!"
            )
            val randomMessage = messages.random()

            // Build notification
            val notificationBuilder = NotificationCompat.Builder(context, "hydration_reminder_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🌊 Time to Drink Water!")
                .setContentText(randomMessage)
                .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.purpleLight))
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val notification = notificationBuilder.build()
            notificationManager.notify(System.currentTimeMillis().toInt(), notification) // unique ID

            Log.d("WaterReminder", "Notification shown successfully!")

        } catch (e: Exception) {
            Log.e("WaterReminder", "Error showing notification: ${e.message}")
        }
    }

    private fun rescheduleNextReminder(context: Context) {
        try {
            // Get user's preferred reminder interval from SharedPreferences
            val sharedPref = context.getSharedPreferences("hydration_settings", 0)
            val intervalMinutes = sharedPref.getInt("reminder_interval", 60)
            val intervalMillis = intervalMinutes * 60 * 1000L

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                action = "WATER_REMINDER_ACTION"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                123,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextTriggerTime = System.currentTimeMillis() + intervalMillis

            // Schedule the alarm depending on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+ supports setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } else {
                // Fallback for older devices
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }

            Log.d("WaterReminder", "Next reminder scheduled in $intervalMinutes minutes")

        } catch (e: Exception) {
            Log.e("WaterReminder", "Error rescheduling reminder: ${e.message}")
        }
    }
}
