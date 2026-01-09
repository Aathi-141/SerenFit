package com.example.healthandfitness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentHabits : Fragment() {

    // TextViews to show percentage progress of each habit
    private lateinit var tvWaterPercent: TextView
    private lateinit var tvMeditatePercent: TextView
    private lateinit var tvExercisePercent: TextView
    private lateinit var tvJournalPercent: TextView

    // Container for custom habit cards
    private lateinit var habitsContainer: LinearLayout

    // Predefined goals for habits
    private val waterGoal = 8           // 8 glasses of water
    private val meditationGoal = 20     // 20 minutes
    private val exerciseGoal = 30       // 30 minutes
    private val journalGoal = 1         // 1 entry

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        // Initialize habit percentage TextViews
        tvWaterPercent = view.findViewById(R.id.tvWaterPercent)
        tvMeditatePercent = view.findViewById(R.id.tvMeditatePercent)
        tvExercisePercent = view.findViewById(R.id.tvExercisePercent)
        tvJournalPercent = view.findViewById(R.id.tvJournalPercent)

        // Initialize container where dynamic habit cards will be added
        habitsContainer = view.findViewById(R.id.habitsContainer)

        // Top-right Edit button: navigates to edit custom habits page
        val btnEditHabits = view.findViewById<TextView>(R.id.btnEditHabits)
        btnEditHabits.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentEditHabits())
                .addToBackStack(null)
                .commit()
        }

        // Load saved progress for predefined habits
        loadSavedProgress()

        // Load dynamically saved custom habits
        loadCustomHabits()

        // Set click listeners for predefined habit cards
        setupCardClickListeners(view)

        return view
    }

    // Load custom habits saved in SharedPreferences
    private fun loadCustomHabits() {
        val sharedPref = requireActivity().getSharedPreferences("custom_habits", 0)
        val habits = sharedPref.getStringSet("habit_list", mutableSetOf()) ?: mutableSetOf()

        // For each habit, create a card dynamically
        for (habitData in habits) {
            val parts = habitData.split("|") // Stored format: "name|time"
            if (parts.size == 2) {
                val habitName = parts[0]
                val habitTime = parts[1]
                createCustomHabitCard(habitName, habitTime)
            }
        }
    }

    // Dynamically create a CardView for a custom habit
    private fun createCustomHabitCard(habitName: String, habitTime: String) {
        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dipToPx(12)) } // bottom margin
            radius = dipToPx(12).toFloat() // rounded corners
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.cardBackground))
            cardElevation = dipToPx(4).toFloat() // shadow
        }

        // Horizontal layout inside CardView
        val innerLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(dipToPx(16), dipToPx(16), dipToPx(16), dipToPx(16))
        }

        // Emoji representing habit type
        val emojiView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dipToPx(40), dipToPx(40))
            text = getEmojiForCustomHabit(habitName) // emoji based on name
            textSize = 20f
            gravity = android.view.Gravity.CENTER
        }

        // Vertical layout for habit name and time
        val detailsLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            orientation = LinearLayout.VERTICAL
            setPadding(dipToPx(12), 0, dipToPx(12), 0)
        }

        val nameView = TextView(requireContext()).apply {
            text = habitName
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val timeView = TextView(requireContext()).apply {
            text = habitTime
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary))
            textSize = 14f
            setPadding(0, dipToPx(4), 0, 0)
        }

        // Progress percentage TextView for custom habit
        val percentView = TextView(requireContext()).apply {
            text = "0%"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.purpleLight))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
        }

        // Add views to layouts
        detailsLayout.addView(nameView)
        detailsLayout.addView(timeView)
        innerLayout.addView(emojiView)
        innerLayout.addView(detailsLayout)
        innerLayout.addView(percentView)
        cardView.addView(innerLayout)
        habitsContainer.addView(cardView)

        // Click listener to update custom habit progress
        cardView.setOnClickListener {
            updateCustomHabitProgress(percentView, habitName)
        }
    }

    // Return an emoji based on habit name
    private fun getEmojiForCustomHabit(habitName: String): String {
        return when {
            habitName.contains("read", ignoreCase = true) -> "📖"
            habitName.contains("sleep", ignoreCase = true) -> "😴"
            habitName.contains("walk", ignoreCase = true) -> "🚶"
            habitName.contains("yoga", ignoreCase = true) -> "🧘"
            habitName.contains("fruit", ignoreCase = true) -> "🍎"
            habitName.contains("vegetable", ignoreCase = true) -> "🥦"
            habitName.contains("water", ignoreCase = true) -> "💧"
            habitName.contains("meditate", ignoreCase = true) -> "🧘"
            habitName.contains("exercise", ignoreCase = true) -> "🏋️"
            habitName.contains("journal", ignoreCase = true) -> "📖"
            else -> "⭐"
        }
    }

    // Update progress for custom habits stored in SharedPreferences
    private fun updateCustomHabitProgress(percentView: TextView, habitName: String) {
        val sharedPref = requireActivity().getSharedPreferences("custom_habits_progress", 0)
        val currentPercent = sharedPref.getInt("progress_$habitName", 0)
        val newPercent = if (currentPercent < 100) currentPercent + 25 else 0 // increase by 25% per click

        sharedPref.edit().putInt("progress_$habitName", newPercent).apply()
        percentView.text = "$newPercent%"

        Toast.makeText(requireContext(), "$habitName: $newPercent% completed!", Toast.LENGTH_SHORT).show()
    }

    // Load progress for predefined habits
    private fun loadSavedProgress() {
        val sharedPref = requireActivity().getSharedPreferences("habits", 0)

        // Water
        val waterGlasses = sharedPref.getInt("water_glasses", 0)
        tvWaterPercent.text = "${waterGlasses * 100 / waterGoal}%"

        // Meditation
        val meditationMinutes = sharedPref.getInt("meditation_minutes", 0)
        tvMeditatePercent.text = "${meditationMinutes * 100 / meditationGoal}%"

        // Exercise
        val exerciseMinutes = sharedPref.getInt("exercise_minutes", 0)
        tvExercisePercent.text = "${exerciseMinutes * 100 / exerciseGoal}%"

        // Journal
        val journalEntries = sharedPref.getInt("journal_entries", 0)
        tvJournalPercent.text = "${journalEntries * 100}%"
    }

    // Attach click listeners to predefined habit cards
    private fun setupCardClickListeners(view: View) {
        val waterCard = view.findViewById<CardView>(R.id.cardWater)
        val meditateCard = view.findViewById<CardView>(R.id.cardMeditate)
        val exerciseCard = view.findViewById<CardView>(R.id.cardExercise)
        val journalCard = view.findViewById<CardView>(R.id.cardJournal)

        waterCard.setOnClickListener { updateWaterProgress() }
        meditateCard.setOnClickListener { updateMeditationProgress() }
        exerciseCard.setOnClickListener { updateExerciseProgress() }
        journalCard.setOnClickListener { updateJournalProgress() }
    }

    // Update water habit progress
    private fun updateWaterProgress() {
        val sharedPref = requireActivity().getSharedPreferences("habits", 0)
        val currentGlasses = sharedPref.getInt("water_glasses", 0)

        if (currentGlasses < waterGoal) {
            val newGlasses = currentGlasses + 1
            val newPercent = (newGlasses * 100 / waterGoal)

            sharedPref.edit().putInt("water_glasses", newGlasses).apply()
            tvWaterPercent.text = "$newPercent%"

            Toast.makeText(requireContext(), "💧 Glass $newGlasses/$waterGoal completed!", Toast.LENGTH_SHORT).show()

            if (newGlasses == waterGoal) {
                Toast.makeText(requireContext(), "🎉 Daily water goal achieved!", Toast.LENGTH_LONG).show()
            }
        } else {
            // Reset for next day
            sharedPref.edit().putInt("water_glasses", 0).apply()
            tvWaterPercent.text = "0%"
            Toast.makeText(requireContext(), "Water tracking reset for new day", Toast.LENGTH_SHORT).show()
        }
    }

    // Update meditation habit progress
    private fun updateMeditationProgress() {
        val sharedPref = requireActivity().getSharedPreferences("habits", 0)
        val currentMinutes = sharedPref.getInt("meditation_minutes", 0)

        if (currentMinutes < meditationGoal) {
            val newMinutes = currentMinutes + 5
            val newPercent = (newMinutes * 100 / meditationGoal)

            sharedPref.edit().putInt("meditation_minutes", newMinutes).apply()
            tvMeditatePercent.text = "$newPercent%"

            Toast.makeText(requireContext(), "🧘 $newMinutes/$meditationGoal minutes meditated", Toast.LENGTH_SHORT).show()

            if (newMinutes == meditationGoal) {
                Toast.makeText(requireContext(), "🎉 Daily meditation goal achieved!", Toast.LENGTH_LONG).show()
            }
        } else {
            sharedPref.edit().putInt("meditation_minutes", 0).apply()
            tvMeditatePercent.text = "0%"
            Toast.makeText(requireContext(), "Meditation tracking reset", Toast.LENGTH_SHORT).show()
        }
    }

    // Update exercise habit progress
    private fun updateExerciseProgress() {
        val sharedPref = requireActivity().getSharedPreferences("habits", 0)
        val currentMinutes = sharedPref.getInt("exercise_minutes", 0)

        if (currentMinutes < exerciseGoal) {
            val newMinutes = currentMinutes + 10
            val newPercent = (newMinutes * 100 / exerciseGoal)

            sharedPref.edit().putInt("exercise_minutes", newMinutes).apply()
            tvExercisePercent.text = "$newPercent%"

            Toast.makeText(requireContext(), "🏋️ $newMinutes/$exerciseGoal minutes exercised", Toast.LENGTH_SHORT).show()

            if (newMinutes == exerciseGoal) {
                Toast.makeText(requireContext(), "🎉 Daily exercise goal achieved!", Toast.LENGTH_LONG).show()
            }
        } else {
            sharedPref.edit().putInt("exercise_minutes", 0).apply()
            tvExercisePercent.text = "0%"
            Toast.makeText(requireContext(), "Exercise tracking reset", Toast.LENGTH_SHORT).show()
        }
    }

    // Update journal habit progress
    private fun updateJournalProgress() {
        val sharedPref = requireActivity().getSharedPreferences("habits", 0)
        val currentEntries = sharedPref.getInt("journal_entries", 0)

        if (currentEntries < journalGoal) {
            val newEntries = currentEntries + 1
            val newPercent = newEntries * 100

            sharedPref.edit().putInt("journal_entries", newEntries).apply()
            tvJournalPercent.text = "$newPercent%"

            Toast.makeText(requireContext(), "📖 Journal entry completed!", Toast.LENGTH_SHORT).show()
        } else {
            sharedPref.edit().putInt("journal_entries", 0).apply()
            tvJournalPercent.text = "0%"
            Toast.makeText(requireContext(), "Journal tracking reset for new day", Toast.LENGTH_SHORT).show()
        }
    }

    // Utility function: convert dp to pixels
    private fun dipToPx(dip: Int): Int {
        return (dip * resources.displayMetrics.density).toInt()
    }

}
