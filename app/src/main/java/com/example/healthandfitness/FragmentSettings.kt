package com.example.healthandfitness

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentSettings : Fragment() {

    private lateinit var switchNotifications: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var switchDailyReminders: Switch
    private lateinit var switchVibration: Switch

    private var isInitializing = true // ADD THIS FLAG

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        initializeViews(view)
        loadSettings() // Load before setting up listeners
        setupClickListeners(view)
        isInitializing = false // SET TO FALSE AFTER INITIALIZATION
        return view
    }

    private fun initializeViews(view: View) {
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        switchDailyReminders = view.findViewById(R.id.switchDailyReminders)
        switchVibration = view.findViewById(R.id.switchVibration)
    }

    private fun setupClickListeners(view: View) {
        // Back button
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Notification Settings
        switchNotifications.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isInitializing) {
                saveBooleanSetting("notifications_enabled", isChecked)
                val message = if (isChecked) getString(R.string.notifications_enabled) else getString(R.string.notifications_disabled)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // Dark Mode
        switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isInitializing) {
                saveBooleanSetting("dark_mode_enabled", isChecked)
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Toast.makeText(requireContext(), getString(R.string.dark_mode_enabled), Toast.LENGTH_SHORT).show()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Toast.makeText(requireContext(), getString(R.string.light_mode_enabled), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Daily Reminders
        switchDailyReminders.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isInitializing) {
                saveBooleanSetting("daily_reminders_enabled", isChecked)
                val message = if (isChecked) getString(R.string.daily_reminders_enabled) else getString(R.string.daily_reminders_disabled)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // Vibration
        switchVibration.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isInitializing) {
                saveBooleanSetting("vibration_enabled", isChecked)
                val message = if (isChecked) getString(R.string.vibration_enabled) else getString(R.string.vibration_disabled)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // Data Management
        view.findViewById<TextView>(R.id.btnClearData).setOnClickListener {
            showClearDataDialog()
        }

        view.findViewById<TextView>(R.id.btnExportData).setOnClickListener {
            showExportDataDialog()
        }

        // Support & About
        view.findViewById<TextView>(R.id.btnContactSupport).setOnClickListener {
            contactSupport()
        }

        view.findViewById<TextView>(R.id.btnRateApp).setOnClickListener {
            rateApp()
        }

        view.findViewById<TextView>(R.id.btnShareApp).setOnClickListener {
            shareApp()
        }

        view.findViewById<TextView>(R.id.btnAbout).setOnClickListener {
            showAboutDialog()
        }

        // App Preferences
        view.findViewById<TextView>(R.id.btnHabitReminders).setOnClickListener {
            showHabitRemindersDialog()
        }

        view.findViewById<TextView>(R.id.btnMoodSettings).setOnClickListener {
            showMoodSettingsDialog()
        }
    }

    private fun loadSettings() {
        val sharedPref = requireActivity().getSharedPreferences("app_settings", 0)

        // Set switches without triggering listeners
        switchNotifications.isChecked = sharedPref.getBoolean("notifications_enabled", true)
        switchDarkMode.isChecked = sharedPref.getBoolean("dark_mode_enabled", false)
        switchDailyReminders.isChecked = sharedPref.getBoolean("daily_reminders_enabled", true)
        switchVibration.isChecked = sharedPref.getBoolean("vibration_enabled", true)
    }

    private fun saveBooleanSetting(key: String, value: Boolean) {
        val sharedPref = requireActivity().getSharedPreferences("app_settings", 0)
        sharedPref.edit().putBoolean(key, value).apply()
    }

    private fun saveStringSetting(key: String, value: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_settings", 0)
        sharedPref.edit().putString(key, value).apply()
    }


    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_data_title))
            .setMessage(getString(R.string.clear_data_message))
            .setPositiveButton(getString(R.string.clear_everything), DialogInterface.OnClickListener { dialog, which ->
                clearAllData()
                Toast.makeText(requireContext(), getString(R.string.all_data_cleared), Toast.LENGTH_LONG).show()
            })
            .setNegativeButton(getString(android.R.string.cancel), null)
            .setNeutralButton(getString(R.string.clear_habits_only)) { dialog, which ->
                clearHabitsOnly()
                Toast.makeText(requireContext(), getString(R.string.habits_cleared), Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showExportDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.export_data_title))
            .setMessage(getString(R.string.export_data_message))
            .setPositiveButton(getString(R.string.generate_report), DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(requireContext(), getString(R.string.generating_report), Toast.LENGTH_LONG).show()
            })
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun contactSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@wellnessapp.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body))
        }
        startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_support_chooser)))
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, requireContext().packageName))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser)))
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about_app_title))
            .setMessage(getString(R.string.about_app_message))
            .setPositiveButton(getString(R.string.awesome), null)
            .show()
    }

    private fun showHabitRemindersDialog() {
        val reminderTimes = arrayOf(
            getString(R.string.time_7am),
            getString(R.string.time_8am),
            getString(R.string.time_9am),
            getString(R.string.time_10am),
            getString(R.string.custom_time)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.habit_reminder_title))
            .setItems(reminderTimes) { dialog, which ->
                val selectedTime = reminderTimes[which]
                saveStringSetting("habit_reminder_time", selectedTime)
                Toast.makeText(requireContext(), getString(R.string.habit_reminders_set, selectedTime), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun showMoodSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.mood_settings_title))
            .setMessage(getString(R.string.mood_settings_message))
            .setPositiveButton(getString(R.string.enable_features)) { dialog, which ->
                Toast.makeText(requireContext(), getString(R.string.mood_features_activated), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.later), null)
            .show()
    }

    private fun clearAllData() {
        val habitPrefs = requireActivity().getSharedPreferences("custom_habits", 0)
        val progressPrefs = requireActivity().getSharedPreferences("habits", 0)
        val moodPrefs = requireActivity().getSharedPreferences("mood_entries", 0)
        val profilePrefs = requireActivity().getSharedPreferences("user_profile", 0)

        habitPrefs.edit().clear().apply()
        progressPrefs.edit().clear().apply()
        moodPrefs.edit().clear().apply()
        profilePrefs.edit().clear().apply()
    }

    private fun clearHabitsOnly() {
        val habitPrefs = requireActivity().getSharedPreferences("custom_habits", 0)
        val progressPrefs = requireActivity().getSharedPreferences("habits", 0)

        habitPrefs.edit().clear().apply()
        progressPrefs.edit().clear().apply()
    }
}