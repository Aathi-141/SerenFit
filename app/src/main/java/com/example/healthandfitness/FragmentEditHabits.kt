package com.example.healthandfitness

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class FragmentEditHabits : Fragment() {

    private lateinit var habitsContainer: LinearLayout // Container for dynamically added habit cards

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_habits, container, false)

        // Initialize habits container
        habitsContainer = view.findViewById(R.id.habitsContainer)

        // Setup click listeners for back button, add new habit, and edit/delete existing habits
        setupClickListeners(view)

        // Load custom habits saved in SharedPreferences and display them
        loadCustomHabits()

        return view
    }

    private fun setupClickListeners(view: View) {
        // Back button: pops fragment from backstack
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Add New Habit button: opens dialog to input habit name and time
        val btnAddNewHabit = view.findViewById<TextView>(R.id.btnAddNewHabit)
        btnAddNewHabit.setOnClickListener {
            showAddHabitDialog()
        }

        // Setup edit and delete buttons for default habits
        setupHabitButtons(view, R.id.btnEditWater, R.id.btnDeleteWater, "Drink Water")
        setupHabitButtons(view, R.id.btnEditMeditate, R.id.btnDeleteMeditate, "Meditate")
        setupHabitButtons(view, R.id.btnEditExercise, R.id.btnDeleteExercise, "Exercise")
        setupHabitButtons(view, R.id.btnEditJournal, R.id.btnDeleteJournal, "Journal")
    }

    private fun setupHabitButtons(view: View, editButtonId: Int, deleteButtonId: Int, habitName: String) {
        // Get buttons from layout
        val btnEdit = view.findViewById<TextView>(editButtonId)
        val btnDelete = view.findViewById<TextView>(deleteButtonId)

        // Edit button opens dialog pre-filled with current habit info
        btnEdit.setOnClickListener {
            showEditHabitDialog(habitName)
        }

        // Delete button shows confirmation dialog before deleting habit
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog(habitName)
        }
    }

    private fun loadCustomHabits() {
        // Retrieve saved custom habits from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("custom_habits", 0)
        val habits = sharedPref.getStringSet("habit_list", mutableSetOf()) ?: mutableSetOf()

        for (habitData in habits) {
            val parts = habitData.split("|") // Split name and time
            if (parts.size == 2) {
                val habitName = parts[0]
                val habitTime = parts[1]
                createCustomHabitCard(habitName, habitTime) // Dynamically create card for each habit
            }
        }
    }

    private fun createCustomHabitCard(habitName: String, habitTime: String) {
        // CardView container for habit
        val cardView = androidx.cardview.widget.CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dipToPx(12)) } // Bottom spacing between cards
            radius = dipToPx(12).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.cardBackground))
            cardElevation = dipToPx(4).toFloat()
        }

        // Inner horizontal layout: emoji, details, buttons
        val innerLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(dipToPx(16), dipToPx(16), dipToPx(16), dipToPx(16))
        }

        // Emoji representing habit (dynamic based on habit name)
        val emojiView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dipToPx(40), dipToPx(40))
            text = getEmojiForCustomHabit(habitName)
            textSize = 20f
            gravity = android.view.Gravity.CENTER
        }

        // Details layout: name and time
        val detailsLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 1f }
            orientation = LinearLayout.VERTICAL
            setPadding(dipToPx(12), 0, dipToPx(12), 0)
        }

        // Habit name TextView
        val nameView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = habitName
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD) // Bold for emphasis
        }

        // Habit time TextView
        val timeView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = habitTime
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary))
            textSize = 14f
            setPadding(0, dipToPx(4), 0, 0)
        }

        // Horizontal layout for edit and delete buttons
        val buttonsLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // Edit button: pencil emoji
        val editButton = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dipToPx(40), dipToPx(40)).apply { marginEnd = dipToPx(8) }
            text = "✏️"
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.circle_background) // Circular background drawable
            setOnClickListener {
                showEditHabitDialog(habitName)
            }
        }

        // Delete button: trash emoji
        val deleteButton = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dipToPx(40), dipToPx(40))
            text = "🗑️"
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.circle_background_red) // Red circular background
            setOnClickListener {
                showDeleteConfirmationDialog(habitName, cardView)
            }
        }

        // Add name and time to details layout
        detailsLayout.addView(nameView)
        detailsLayout.addView(timeView)

        // Add buttons to buttons layout
        buttonsLayout.addView(editButton)
        buttonsLayout.addView(deleteButton)

        // Combine emoji, details, and buttons
        innerLayout.addView(emojiView)
        innerLayout.addView(detailsLayout)
        innerLayout.addView(buttonsLayout)

        // Add inner layout to card and card to container
        cardView.addView(innerLayout)
        habitsContainer.addView(cardView)
    }

    private fun getEmojiForCustomHabit(habitName: String): String {
        // Assign emoji based on keywords in habit name
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

    private fun showAddHabitDialog() {
        // Custom dialog layout for adding new habit
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)

        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etHabitTime = dialogView.findViewById<EditText>(R.id.etHabitTime)

        // AlertDialog for input
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add", DialogInterface.OnClickListener { dialog, which ->
                val habitName = etHabitName.text.toString().trim()
                val habitTime = etHabitTime.text.toString().trim()

                // Check input is not empty
                if (habitName.isNotEmpty() && habitTime.isNotEmpty()) {
                    saveNewHabit(habitName, habitTime) // Save in SharedPreferences
                    createCustomHabitCard(habitName, habitTime) // Add card dynamically
                    Toast.makeText(requireContext(), "Habit added: $habitName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .create()

        // Customize dialog background
        dialog.window?.setBackgroundDrawableResource(R.color.cardBackground)
        dialog.show()

        // Customize positive and negative button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.purpleLight))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary))
    }

    private fun showEditHabitDialog(habitName: String) {
        // Pre-fill dialog with habit data
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etHabitTime = dialogView.findViewById<EditText>(R.id.etHabitTime)

        etHabitName.setText(habitName)
        etHabitTime.setText(getHabitTime(habitName))

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save", DialogInterface.OnClickListener { dialog, which ->
                val newHabitName = etHabitName.text.toString().trim()
                val newHabitTime = etHabitTime.text.toString().trim()

                // Update habit if input is valid
                if (newHabitName.isNotEmpty() && newHabitTime.isNotEmpty()) {
                    updateHabit(habitName, newHabitName, newHabitTime)
                    refreshHabitsList() // Reload all cards
                    Toast.makeText(requireContext(), "Habit updated", Toast.LENGTH_SHORT).show()
                }
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .create()

        dialog.window?.setBackgroundDrawableResource(R.color.cardBackground)
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.purpleLight))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary))
    }

    private fun showDeleteConfirmationDialog(habitName: String, cardView: View) {
        // Confirm deletion of habit
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '$habitName'?")
            .setPositiveButton("Delete", DialogInterface.OnClickListener { dialog, which ->
                deleteHabit(habitName) // Remove from SharedPreferences
                (cardView.parent as? ViewGroup)?.removeView(cardView) // Remove card dynamically
                Toast.makeText(requireContext(), "Habit deleted: $habitName", Toast.LENGTH_SHORT).show()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .show()
    }

    private fun showDeleteConfirmationDialog(habitName: String) {
        // Delete default habit and refresh entire list
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '$habitName'?")
            .setPositiveButton("Delete", DialogInterface.OnClickListener { dialog, which ->
                deleteHabit(habitName)
                refreshHabitsList()
                Toast.makeText(requireContext(), "Habit deleted: $habitName", Toast.LENGTH_SHORT).show()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .show()
    }

    private fun refreshHabitsList() {
        // Clear container and reload all custom habits
        habitsContainer.removeAllViews()
        loadCustomHabits()
    }

    private fun saveNewHabit(habitName: String, habitTime: String) {
        // Save new habit in SharedPreferences as "name|time"
        val sharedPref = requireActivity().getSharedPreferences("custom_habits", 0)
        val habits = sharedPref.getStringSet("habit_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        habits.add("$habitName|$habitTime")
        sharedPref.edit().putStringSet("habit_list", habits).apply()
    }

    private fun getHabitTime(habitName: String): String {
        // Retrieve time for a habit
        val sharedPref = requireActivity().getSharedPreferences("custom_habits", 0)
        val habits = sharedPref.getStringSet("habit_list", mutableSetOf()) ?: mutableSetOf()
        for (habitData in habits) {
            val parts = habitData.split("|")
            if (parts.size == 2 && parts[0] == habitName) return parts[1]
        }
        return "Morning" // Default value
    }

    private fun updateHabit(oldHabitName: String, newHabitName: String, newHabitTime: String) {
        // Replace old habit with new habit
        val sharedPref = requireActivity().getSharedPreferences("custom_habits", 0)
        val habits = sharedPref.getStringSet("habit_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val iterator = habits.iterator()
        while (iterator.hasNext()) {
            val habit = iterator.next()
            if (habit.startsWith("$oldHabitName|")) iterator.remove()
        }
        habits.add("$newHabitName|$newHabitTime")

        sharedPref.edit().putStringSet("habit_list", habits).apply()
    }

    private fun deleteHabit(habitName: String) {
        // Delete habit from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("custom_habits", 0)
        val habits = sharedPref.getStringSet("habit_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val iterator = habits.iterator()
        while (iterator.hasNext()) {
            val habit = iterator.next()
            if (habit.startsWith("$habitName|")) iterator.remove()
        }
        sharedPref.edit().putStringSet("habit_list", habits).apply()
    }

    private fun dipToPx(dip: Int): Int {
        // Convert density-independent pixels to pixels
        return (dip * resources.displayMetrics.density).toInt()
    }
}
