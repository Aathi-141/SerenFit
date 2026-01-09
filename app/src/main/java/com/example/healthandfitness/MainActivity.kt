package com.example.healthandfitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    // Bottom navigation text views
    private lateinit var navHabits: TextView
    private lateinit var navMood: TextView
    private lateinit var navHydration: TextView
    private lateinit var navProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the navigation bar (links the TextViews with their IDs)
        initNavigation()

        // When the app opens for the first time, show the Habits screen by default
        if (savedInstanceState == null) {
            loadFragment(FragmentHabits(), false)
            setActiveTab(navHabits)
        }
    }

    // This function sets up click actions for the bottom navigation buttons
    private fun initNavigation() {
        navHabits = findViewById(R.id.nav_habits)
        navMood = findViewById(R.id.nav_mood)
        navHydration = findViewById(R.id.nav_hydration)
        navProfile = findViewById(R.id.nav_profile)

        // When the user taps a tab, switch the fragment and highlight the tab
        navHabits.setOnClickListener {
            setActiveTab(it)
            loadFragment(FragmentHabits(), true)
        }
        navMood.setOnClickListener {
            setActiveTab(it)
            loadFragment(FragmentMood(), true)
        }
        navHydration.setOnClickListener {
            setActiveTab(it)
            loadFragment(FragmentHydration(), true)
        }
        navProfile.setOnClickListener {
            setActiveTab(it)
            loadFragment(FragmentProfile(), true)
        }
    }

    // Highlights the selected tab and resets others
    private fun setActiveTab(selectedView: View) {
        // Reset all tabs to normal color and style
        navHabits.setTextColor(ContextCompat.getColor(this, R.color.textSecondary))
        navMood.setTextColor(ContextCompat.getColor(this, R.color.textSecondary))
        navHydration.setTextColor(ContextCompat.getColor(this, R.color.textSecondary))
        navProfile.setTextColor(ContextCompat.getColor(this, R.color.textSecondary))

        // Make all text normal weight
        navHabits.setTypeface(null, android.graphics.Typeface.NORMAL)
        navMood.setTypeface(null, android.graphics.Typeface.NORMAL)
        navHydration.setTypeface(null, android.graphics.Typeface.NORMAL)
        navProfile.setTypeface(null, android.graphics.Typeface.NORMAL)

        // Set the selected tab to a highlighted color and bold text
        (selectedView as TextView).setTextColor(ContextCompat.getColor(this, R.color.purpleLight))
        selectedView.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    // This function switches between fragments (different screens)
    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        // Add the transaction to backstack if user navigates manually
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    // Handles what happens when user presses the back button
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // If there are previous fragments, go back to the last one
            supportFragmentManager.popBackStack()
            updateActiveTabAfterBack()
        } else {
            // If not, exit the app normally
            super.onBackPressed()
        }
    }

    // Updates the highlighted tab after pressing back
    private fun updateActiveTabAfterBack() {
        // When the back stack is empty, we assume the user is on Habits
        if (supportFragmentManager.backStackEntryCount == 0) {
            setActiveTab(navHabits)
        }
    }
}
