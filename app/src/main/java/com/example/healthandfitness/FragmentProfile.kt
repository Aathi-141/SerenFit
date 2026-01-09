package com.example.healthandfitness

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentProfile : Fragment() {

    // Profile UI elements
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvTotalHabits: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvCompletionRate: TextView
    private lateinit var tvProfilePicture: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize all TextViews
        initializeViews(view)

        // Set up click listeners for buttons and avatar
        setupClickListeners(view)

        // Load user data from SharedPreferences
        loadUserData()

        // Load stats like streak, completion, total habits
        loadStatistics()

        return view
    }

    private fun initializeViews(view: View) {
        // Link XML elements to Kotlin variables
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvTotalHabits = view.findViewById(R.id.tvTotalHabits)
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak)
        tvCompletionRate = view.findViewById(R.id.tvCompletionRate)
        tvProfilePicture = view.findViewById(R.id.ivProfilePicture) // Avatar
    }

    private fun setupClickListeners(view: View) {
        // Settings button navigates to FragmentSettings
        val btnSettings = view.findViewById<TextView>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentSettings())
                .addToBackStack(null)
                .commit()
        }

        // Edit Profile opens dialog to change name/email
        val btnEditProfile = view.findViewById<TextView>(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener { showEditProfileDialog() }

        // Avatar click opens avatar selection dialog
        tvProfilePicture.setOnClickListener { showProfilePictureOptions() }

        // Setup achievement cards (UI updates based on progress)
        setupAchievementCards(view)
    }

    private fun showEditProfileDialog() {
        // Dialog layout for editing profile
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)

        val etUserName = dialogView.findViewById<EditText>(R.id.etUserName)
        val etUserEmail = dialogView.findViewById<EditText>(R.id.etUserEmail)

        // Load existing user data
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)
        etUserName.setText(sharedPref.getString("user_name", "Wellness Warrior"))
        etUserEmail.setText(sharedPref.getString("user_email", "warrior@wellness.com"))

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val newName = etUserName.text.toString().trim()
                val newEmail = etUserEmail.text.toString().trim()

                if (newName.isNotEmpty()) {
                    saveUserProfile(newName, newEmail) // Save to SharedPreferences
                    loadUserData() // Update UI
                    Toast.makeText(requireContext(), "Profile updated successfully! 🌟", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.cardBackground)
        dialog.show()
    }

    private fun showProfilePictureOptions() {
        // Array of emojis for avatars
        val avatars = arrayOf("👤", "👨‍💼", "👩‍💼", "🦸", "🦸‍♀️", "🧙‍♂️", "🧙‍♀️", "💁", "💁‍♂️")

        // Show selection dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Your Avatar")
            .setItems(avatars) { dialog, which ->
                saveProfileAvatar(avatars[which]) // Save choice
                tvProfilePicture.text = avatars[which] // Update UI
                Toast.makeText(requireContext(), "Avatar updated! ✨", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupAchievementCards(view: View) {
        // Update achievement status based on stored stats
        updateAchievementStatus()
    }

    private fun updateAchievementStatus() {
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)
        val totalHabitsCompleted = sharedPref.getInt("total_habits_completed", 0)
        val currentStreak = sharedPref.getInt("current_streak", 0)

        // Reference to achievement cards
        val cardHabitMaster = view?.findViewById<CardView>(R.id.cardHabitMaster)
        val cardStreakKing = view?.findViewById<CardView>(R.id.cardStreakKing)
        val cardHydrationHero = view?.findViewById<CardView>(R.id.cardHydrationHero)

        // Habit Master: complete 50 habits
        if (totalHabitsCompleted >= 50) {
            cardHabitMaster?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpleLight))
            cardHabitMaster?.findViewById<TextView>(R.id.tvAchievementDesc)?.text = "Unlocked! 🎉"
        }

        // Streak King: 7-day streak
        if (currentStreak >= 7) {
            cardStreakKing?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpleLight))
            cardStreakKing?.findViewById<TextView>(R.id.tvAchievementDesc)?.text = "Unlocked! 🔥"
        }

        // Hydration Hero: use water reminders for 3+ days
        val hydrationDays = sharedPref.getInt("hydration_days", 0)
        if (hydrationDays >= 3) {
            cardHydrationHero?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purpleLight))
            cardHydrationHero?.findViewById<TextView>(R.id.tvAchievementDesc)?.text = "Unlocked! 💧"
        }
    }

    private fun saveUserProfile(name: String, email: String) {
        // Save name and email to SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)
        sharedPref.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
    }

    private fun saveProfileAvatar(avatar: String) {
        // Save chosen avatar
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)
        sharedPref.edit().putString("user_avatar", avatar).apply()
    }

    private fun loadUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)

        // Load and display user info
        val userName = sharedPref.getString("user_name", "Wellness Warrior") ?: "Wellness Warrior"
        val userEmail = sharedPref.getString("user_email", "warrior@wellness.com") ?: "warrior@wellness.com"
        val userAvatar = sharedPref.getString("user_avatar", "👤") ?: "👤"

        tvUserName.text = userName
        tvUserEmail.text = userEmail
        tvProfilePicture.text = userAvatar
    }

    private fun loadStatistics() {
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)

        // Load and display stats
        val totalHabits = sharedPref.getInt("total_habits_completed", 0)
        val currentStreak = sharedPref.getInt("current_streak", 0)
        val completionRate = sharedPref.getInt("completion_rate", 0)

        tvTotalHabits.text = "$totalHabits"
        tvCurrentStreak.text = "$currentStreak days"
        tvCompletionRate.text = "$completionRate%"

        // Update statistics for demo (randomized)
        updateDailyStatistics()
    }

    private fun updateDailyStatistics() {
        val sharedPref = requireActivity().getSharedPreferences("user_profile", 0)
        val editor = sharedPref.edit()

        // Random demo values to simulate progress
        val random = java.util.Random()
        editor.putInt("total_habits_completed", 42 + random.nextInt(20))
        editor.putInt("current_streak", 5 + random.nextInt(10))
        editor.putInt("completion_rate", 75 + random.nextInt(20))
        editor.putInt("hydration_days", 2 + random.nextInt(3))

        editor.apply()

        // Update UI directly
        tvTotalHabits.text = "${42 + random.nextInt(20)}"
        tvCurrentStreak.text = "${5 + random.nextInt(10)} days"
        tvCompletionRate.text = "${75 + random.nextInt(20)}%"

        updateAchievementStatus() // Update achievements based on new values
    }
}
