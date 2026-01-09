package com.example.healthandfitness

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentHydration : Fragment() {

    private lateinit var radioGroupFrequency: RadioGroup
    private val CHANNEL_ID = "hydration_reminder_channel"
    private val ALARM_REQUEST_CODE = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        // Setup back button and save button click listeners
        setupClickListeners(view)

        // Create notification channel for reminders
        createNotificationChannel()

        // Load saved reminder frequency and update radio buttons
        loadSavedSettings(view)

        // Update text colors of selected/unselected radio buttons
        setupRadioButtonTextColors(view)

        return view
    }

    private fun setupClickListeners(view: View) {
        // Back button: goes back to previous fragment
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Save Settings button: saves selected reminder frequency
        val btnSaveSettings = view.findViewById<TextView>(R.id.btnSaveSettings)
        btnSaveSettings.setOnClickListener {
            saveReminderSettings()
        }

        radioGroupFrequency = view.findViewById(R.id.radioGroupFrequency)
    }

    private fun setupRadioButtonTextColors(view: View) {
        radioGroupFrequency.setOnCheckedChangeListener { _, _ ->
            // Update colors whenever selection changes
            updateRadioButtonTextColors(view)
        }

        // Set initial text colors
        updateRadioButtonTextColors(view)
    }

    private fun updateRadioButtonTextColors(view: View) {
        val radio30min = view.findViewById<RadioButton>(R.id.radio30min)
        val radio1hour = view.findViewById<RadioButton>(R.id.radio1hour)
        val radio2hours = view.findViewById<RadioButton>(R.id.radio2hours)
        val radio3hours = view.findViewById<RadioButton>(R.id.radio3hours)

        // Colors: white for selected, primary text color for others
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val normalColor = ContextCompat.getColor(requireContext(), R.color.textPrimary)

        val checkedId = radioGroupFrequency.checkedRadioButtonId

        radio30min.setTextColor(if (checkedId == R.id.radio30min) selectedColor else normalColor)
        radio1hour.setTextColor(if (checkedId == R.id.radio1hour) selectedColor else normalColor)
        radio2hours.setTextColor(if (checkedId == R.id.radio2hours) selectedColor else normalColor)
        radio3hours.setTextColor(if (checkedId == R.id.radio3hours) selectedColor else normalColor)
    }

    private fun loadSavedSettings(view: View) {
        val sharedPref = requireActivity().getSharedPreferences("hydration_settings", 0)
        val savedInterval = sharedPref.getInt("reminder_interval", 60) // Default: 60 minutes

        radioGroupFrequency = view.findViewById(R.id.radioGroupFrequency)

        // Check the saved interval
        when (savedInterval) {
            30 -> radioGroupFrequency.check(R.id.radio30min)
            60 -> radioGroupFrequency.check(R.id.radio1hour)
            120 -> radioGroupFrequency.check(R.id.radio2hours)
            180 -> radioGroupFrequency.check(R.id.radio3hours)
        }

        // Update text colors
        updateRadioButtonTextColors(view)
    }

    private fun saveReminderSettings() {
        val selectedId = radioGroupFrequency.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(requireContext(), "Please select a reminder frequency", Toast.LENGTH_SHORT).show()
            return
        }

        val intervalMinutes = when (selectedId) {
            R.id.radio30min -> 30
            R.id.radio1hour -> 60
            R.id.radio2hours -> 120
            R.id.radio3hours -> 180
            else -> 60
        }

        // Save selected interval in SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("hydration_settings", 0)
        sharedPref.edit().putInt("reminder_interval", intervalMinutes).apply()

        // Schedule water reminders
        if (scheduleWaterReminders(intervalMinutes)) {
            val intervalText = when (intervalMinutes) {
                30 -> "30 minutes"
                60 -> "1 hour"
                120 -> "2 hours"
                180 -> "3 hours"
                else -> "1 hour"
            }
            Toast.makeText(requireContext(), "💧 Reminders set for every $intervalText!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "❌ Could not set reminders. Please check permissions.", Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleWaterReminders(intervalMinutes: Int): Boolean {
        return try {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), WaterReminderReceiver::class.java).apply {
                action = "WATER_REMINDER_ACTION"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel existing alarms
            alarmManager.cancel(pendingIntent)

            val intervalMillis = intervalMinutes * 60 * 1000L
            val triggerTime = System.currentTimeMillis() + (intervalMillis / 2) // First reminder after half interval

            // Schedule exact alarms safely for all versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Use setExactAndAllowWhileIdle for API 23+
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // For older versions
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d("Hydration", "Water reminders scheduled every $intervalMinutes minutes")
            true
        } catch (e: SecurityException) {
            Log.e("Hydration", "Alarm permission denied: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("Hydration", "Error scheduling alarm: ${e.message}")
            false
        }
    }

    private fun createNotificationChannel() {
        // Required for Android O+ to show notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Water Reminder"
            val descriptionText = "Reminders to drink water and stay hydrated"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
                lockscreenVisibility = NotificationManager.IMPORTANCE_HIGH
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Utility to convert dp to pixels
    private fun dipToPx(dip: Int): Int {
        return (dip * resources.displayMetrics.density).toInt()
    }
}
