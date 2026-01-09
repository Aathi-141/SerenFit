package com.example.healthandfitness

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class FragmentMood : Fragment() {

    private lateinit var pastEntriesContainer: LinearLayout // Container for past mood entries
    private var selectedMood: String = "" // Currently selected mood
    private var selectedEmoji: TextView? = null // Currently selected emoji view

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        setupClickListeners(view) // Setup all button and emoji clicks
        loadPastEntries() // Load previously saved mood entries

        return view
    }

    private fun setupClickListeners(view: View) {
        // Back button
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack() // Go back to previous fragment
        }

        // Mood selection buttons
        setupMoodSelection(view)

        // View Trends button (navigate to chart fragment)
        val btnViewTrends = view.findViewById<TextView>(R.id.btnViewTrends)
        btnViewTrends.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentMoodChart())
                .addToBackStack(null)
                .commit()
        }

        // Log Mood button
        val btnLogMood = view.findViewById<TextView>(R.id.btnLogMood)
        btnLogMood.setOnClickListener {
            if (selectedMood.isNotEmpty()) {
                logCurrentMood() // Save selected mood
            } else {
                Toast.makeText(requireContext(), "Please select a mood first", Toast.LENGTH_SHORT).show()
            }
        }

        pastEntriesContainer = view.findViewById(R.id.pastEntriesContainer) // Initialize container
    }

    private fun setupMoodSelection(view: View) {
        // Map button IDs to mood names
        val moods = mapOf(
            R.id.btnHappy to "Happy",
            R.id.btnContent to "Content",
            R.id.btnNeutral to "Neutral",
            R.id.btnSad to "Sad",
            R.id.btnAngry to "Angry",
            R.id.btnTired to "Tired"

        )

        moods.forEach { (id, mood) ->
            val moodView = view.findViewById<TextView>(id)
            moodView.setOnClickListener {
                selectedMood = mood
                selectedEmoji?.isSelected = false // Deselect previous selection
                moodView.isSelected = true // Highlight selected mood
                selectedEmoji = moodView
            }
        }
    }

    private fun logCurrentMood() {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val moodEntry = MoodEntry(selectedMood, getEmojiForMood(selectedMood), currentTime, currentDate)

        saveMoodEntry(moodEntry) // Save to SharedPreferences
        addMoodEntryToView(moodEntry, true) // Show entry on top of the list

        Toast.makeText(requireContext(), "Mood logged: $selectedMood", Toast.LENGTH_SHORT).show()

        // Reset selection
        selectedMood = ""
        selectedEmoji?.isSelected = false
        selectedEmoji = null
    }

    private fun getEmojiForMood(mood: String): String {
        // Map mood to emoji
        return when (mood) {
            "Happy" -> "😊"
            "Content" -> "😌"
            "Neutral" -> "😐"
            "Sad" -> "😔"
            "Angry" -> "😠"
            "Tired" -> "😴"
            else -> "😐"
        }
    }

    private fun saveMoodEntry(moodEntry: MoodEntry) {
        val sharedPref = requireActivity().getSharedPreferences("mood_entries", 0)
        val entries = sharedPref.getStringSet("mood_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // Format: mood|emoji|time|date
        val entryData = "${moodEntry.mood}|${moodEntry.emoji}|${moodEntry.time}|${moodEntry.date}"
        entries.add(entryData)

        sharedPref.edit().putStringSet("mood_list", entries).apply() // Save updated set
    }

    private fun loadPastEntries() {
        val sharedPref = requireActivity().getSharedPreferences("mood_entries", 0)
        val entries = sharedPref.getStringSet("mood_list", mutableSetOf()) ?: mutableSetOf()

        // Convert each saved string to MoodEntry and sort by date & time descending
        val sortedEntries = entries.mapNotNull { entryData ->
            val parts = entryData.split("|")
            if (parts.size == 4) MoodEntry(parts[0], parts[1], parts[2], parts[3])
            else null
        }.sortedByDescending { it.date + it.time }

        // Add each entry to the UI
        sortedEntries.forEach { entry ->
            addMoodEntryToView(entry, false)
        }
    }

    private fun addMoodEntryToView(moodEntry: MoodEntry, addToTop: Boolean) {
        val entryView = createMoodEntryView(moodEntry)

        if (addToTop && pastEntriesContainer.childCount > 0) {
            pastEntriesContainer.addView(entryView, 0) // Add at top
        } else {
            pastEntriesContainer.addView(entryView) // Add at bottom
        }
    }

    private fun createMoodEntryView(moodEntry: MoodEntry): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = dipToPx(12).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.cardBackground)) // Use ContextCompat
            cardElevation = dipToPx(2).toFloat()
        }

        val innerLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(dipToPx(16), dipToPx(16), dipToPx(16), dipToPx(16))
        }

        // Mood Emoji
        val emojiView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dipToPx(40), dipToPx(40))
            text = moodEntry.emoji
            textSize = 20f
            gravity = android.view.Gravity.CENTER
        }

        // Mood details (mood name + date/time)
        val detailsLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            orientation = LinearLayout.VERTICAL
            setPadding(dipToPx(12), 0, dipToPx(12), 0)
        }

        val moodTextView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = moodEntry.mood
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary)) // Use ContextCompat
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val timeTextView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = getDisplayDate(moodEntry.date) + " • " + moodEntry.time
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary)) // Use ContextCompat
            textSize = 14f
            setPadding(0, dipToPx(4), 0, 0)
        }

        // Options menu (three dots)
        val optionsMenu = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "⋮"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary)) // Use ContextCompat
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            setPadding(dipToPx(16), 0, 0, 0)
            setOnClickListener {
                showDeleteDialog(moodEntry, layout) // Delete entry on click
            }
        }

        // Add views together
        detailsLayout.addView(moodTextView)
        detailsLayout.addView(timeTextView)

        innerLayout.addView(emojiView)
        innerLayout.addView(detailsLayout)
        innerLayout.addView(optionsMenu)

        cardView.addView(innerLayout)
        layout.addView(cardView)

        return layout
    }

    private fun getDisplayDate(entryDate: String): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date().apply {
            time -= 24 * 60 * 60 * 1000
        })

        return when (entryDate) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                try {
                    outputFormat.format(inputFormat.parse(entryDate)!!)
                } catch (e: Exception) {
                    entryDate
                }
            }
        }
    }

    private fun showDeleteDialog(moodEntry: MoodEntry, entryView: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteMoodEntry(moodEntry) // Remove from SharedPreferences
                (entryView.parent as? ViewGroup)?.removeView(entryView) // Remove from UI
                Toast.makeText(requireContext(), "Mood entry deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        val sharedPref = requireActivity().getSharedPreferences("mood_entries", 0)
        val entries = sharedPref.getStringSet("mood_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val entryToRemove = "${moodEntry.mood}|${moodEntry.emoji}|${moodEntry.time}|${moodEntry.date}"
        entries.remove(entryToRemove)

        sharedPref.edit().putStringSet("mood_list", entries).apply()
    }

    private fun dipToPx(dip: Int): Int {
        return (dip * resources.displayMetrics.density).toInt() // Convert dp to px
    }

    data class MoodEntry(
        val mood: String,
        val emoji: String,
        val time: String,
        val date: String
    )
}
