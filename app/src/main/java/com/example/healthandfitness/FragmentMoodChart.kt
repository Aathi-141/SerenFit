package com.example.healthandfitness

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import kotlin.math.abs
import java.util.*

class FragmentMoodChart : Fragment() {

    // Custom view that will draw the bar chart
    private lateinit var customChartView: CustomChartView
    private lateinit var tvNoData: TextView // Shown if no data for week
    private lateinit var tvInsight: TextView // Shows insights for the week
    private lateinit var tvWeekRange: TextView // Displays the week range (e.g., Oct 7-13)
    private lateinit var btnPrevWeek: TextView // Navigate to previous week
    private lateinit var btnNextWeek: TextView // Navigate to next week

    private var currentWeekOffset = 0 // 0=current week, -1=last week, +1=next week

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("MoodChart", "onCreateView started")

        try {
            val view = inflater.inflate(R.layout.fragment_mood_chart, container, false) // Inflate layout
            Log.d("MoodChart", "Layout inflated successfully")

            initializeViews(view) // Link XML views to variables
            setupClickListeners(view) // Set click actions
            setupWeekNavigation(view) // Setup prev/next/current week buttons

            // Load mood data after layout is ready
            view.post {
                loadMoodData()
            }

            return view
        } catch (e: Exception) {
            Log.e("MoodChart", "Error in onCreateView: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun initializeViews(view: View) {
        Log.d("MoodChart", "Initializing views")
        try {
            customChartView = view.findViewById(R.id.customChartView)
            tvNoData = view.findViewById(R.id.tvNoData)
            tvInsight = view.findViewById(R.id.tvInsight)
            tvWeekRange = view.findViewById(R.id.tvWeekRange)
            btnPrevWeek = view.findViewById(R.id.btnPrevWeek)
            btnNextWeek = view.findViewById(R.id.btnNextWeek)
            Log.d("MoodChart", "Views initialized successfully")
        } catch (e: Exception) {
            Log.e("MoodChart", "Error initializing views: ${e.message}")
            throw e
        }
    }

    private fun setupClickListeners(view: View) {
        Log.d("MoodChart", "Setting up click listeners")
        try {
            // Back button to return to previous fragment
            val btnBack = view.findViewById<TextView>(R.id.btnBack)
            btnBack.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            Log.d("MoodChart", "Click listeners set up successfully")
        } catch (e: Exception) {
            Log.e("MoodChart", "Error setting up click listeners: ${e.message}")
        }
    }

    private fun setupWeekNavigation(view: View) {
        // Navigate to previous week
        btnPrevWeek.setOnClickListener {
            currentWeekOffset--
            loadMoodData() // Reload chart for new week
        }

        // Navigate to next week
        btnNextWeek.setOnClickListener {
            currentWeekOffset++
            loadMoodData() // Reload chart for new week
        }

        // Go back to current week
        val btnCurrentWeek = view.findViewById<TextView>(R.id.btnCurrentWeek)
        btnCurrentWeek.setOnClickListener {
            currentWeekOffset = 0
            loadMoodData()
        }
    }

    private fun loadMoodData() {
        Log.d("MoodChart", "Loading mood data for week offset: $currentWeekOffset")
        try {
            val moodData = getMoodDataForWeek(currentWeekOffset) // Get list of moods for 7 days
            Log.d("MoodChart", "Mood data loaded: ${moodData.size} entries")

            showChartWithData(moodData) // Display chart
            generateInsight(moodData) // Generate insights based on moods
            updateWeekDisplay() // Update week range display
            Log.d("MoodChart", "Mood data processed successfully")
        } catch (e: Exception) {
            Log.e("MoodChart", "Error loading mood data: ${e.message}")
            showNoDataState() // Show empty state if error
        }
    }

    private fun getMoodDataForWeek(weekOffset: Int): List<Pair<String, Float>> {
        val moodData = ArrayList<Pair<String, Float>>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        try {
            // Move to start of requested week (Monday)
            calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            // Loop through 7 days
            for (i in 0 until 7) {
                val dateString = dateFormat.format(calendar.time) // yyyy-MM-dd
                val dayName = dayFormat.format(calendar.time) // Mon, Tue, etc.
                val averageMood = getAverageMoodForDate(dateString) // Get average mood
                moodData.add(Pair(dayName, averageMood.toFloat())) // Store day & mood
                Log.d("MoodChart", "Day: $dayName, Date: $dateString, Mood: $averageMood")

                calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to next day
            }
        } catch (e: Exception) {
            Log.e("MoodChart", "Error getting mood data: ${e.message}")
        }

        return moodData
    }

    private fun updateWeekDisplay() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val endDate = calendar.time

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val weekText = if (currentWeekOffset == 0) {
            "This Week (${dateFormat.format(startDate)} - ${dateFormat.format(endDate)})"
        } else if (currentWeekOffset == -1) {
            "Last Week (${dateFormat.format(startDate)} - ${dateFormat.format(endDate)})"
        } else if (currentWeekOffset < -1) {
            "${abs(currentWeekOffset)} Weeks Ago (${dateFormat.format(startDate)} - ${dateFormat.format(endDate)})"
        } else {
            "In ${currentWeekOffset} Weeks (${dateFormat.format(startDate)} - ${dateFormat.format(endDate)})"
        }

        tvWeekRange.text = weekText

        // Disable next week button if beyond 4 weeks in future
        btnNextWeek.isEnabled = currentWeekOffset < 4
        btnNextWeek.alpha = if (currentWeekOffset < 4) 1f else 0.5f
    }

    private fun getAverageMoodForDate(date: String): Int {
        return try {
            val sharedPref = requireActivity().getSharedPreferences("mood_entries", 0)
            val moodEntriesSet = sharedPref.getStringSet("mood_list", mutableSetOf()) ?: mutableSetOf()

            Log.d("MoodChart", "Found ${moodEntriesSet.size} total mood entries")

            var totalMood = 0
            var entryCount = 0

            for (entryData in moodEntriesSet) {
                val parts = entryData.split("|") // Split "id|emoji|note|date"
                if (parts.size == 4) {
                    val moodEmoji = parts[1] // emoji
                    val entryDate = parts[3] // date
                    if (entryDate == date) {
                        val moodValue = convertEmojiToNumber(moodEmoji) // Convert to number 1-5
                        totalMood += moodValue
                        entryCount++
                        Log.d("MoodChart", "Match found! Mood: $moodEmoji -> $moodValue")
                    }
                }
            }

            if (entryCount > 0) totalMood / entryCount else 0
        } catch (e: Exception) {
            Log.e("MoodChart", "Error getting average mood: ${e.message}")
            0
        }
    }

    private fun convertEmojiToNumber(emoji: String): Int {
        // Map mood emojis to numeric values
        return when (emoji) {
            "😊", "😄", "🤩" -> 5  // Great
            "😌", "🙂" -> 4        // Good
            "😐", "😶", "🤔" -> 3  // Okay
            "😔", "😞", "😕" -> 2  // Bad
            "😢", "😭", "😡" -> 1  // Awful
            else -> 3
        }
    }

    private fun showChartWithData(moodData: List<Pair<String, Float>>) {
        try {
            customChartView.setMoodData(moodData) // Send data to custom chart
            customChartView.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE
            Log.d("MoodChart", "Chart displayed with ${moodData.size} data points")
        } catch (e: Exception) {
            Log.e("MoodChart", "Error showing chart: ${e.message}")
            showNoDataState() // Fallback to empty state
        }
    }

    private fun showNoDataState() {
        try {
            customChartView.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
            tvNoData.text = "No mood data available for this week"
            tvInsight.text = "• Navigate to current week to see your latest moods\n• Mood data is saved for historical viewing"
            Log.d("MoodChart", "No data state shown")
        } catch (e: Exception) {
            Log.e("MoodChart", "Error showing no data state: ${e.message}")
        }
    }

    private fun generateInsight(moodData: List<Pair<String, Float>>) {
        try {
            val moodValues = moodData.map { it.second }
            val daysWithData = moodData.count { it.second > 0 } // Count days with mood entries

            val insights = ArrayList<String>()

            if (daysWithData == 0) {
                insights.add("No mood data recorded this week")
                insights.add("Log moods to track your emotional patterns")
            } else {
                val averageMood = moodValues.filter { it > 0 }.average()
                val maxMood = moodValues.maxOrNull() ?: 0f
                val minMood = moodValues.filter { it > 0 }.minOrNull() ?: 0f

                // Overall mood insight
                insights.add(when {
                    averageMood >= 4 -> "You were feeling great this week! 😄"
                    averageMood >= 3 -> "Overall, you had a good week! 😊"
                    averageMood > 0 -> "This was a challenging week 💖"
                    else -> "No mood data available"
                })

                // Data tracking insight
                insights.add("You tracked $daysWithData of 7 days")

                // Best and worst days
                if (daysWithData >= 2) {
                    val bestDayIndex = moodData.indexOfFirst { it.second == maxMood }
                    val worstDayIndex = moodData.indexOfFirst { it.second == minMood && it.second > 0 }

                    if (bestDayIndex != -1) insights.add("Best day: ${moodData[bestDayIndex].first}")
                    if (worstDayIndex != -1 && maxMood != minMood) insights.add("Toughest day: ${moodData[worstDayIndex].first}")
                }

                // Consistency insight
                if (daysWithData >= 3) {
                    val moodRange = maxMood - minMood
                    if (moodRange <= 1) insights.add("Very consistent mood pattern 📊")
                    else if (moodRange >= 2.5) insights.add("Emotionally varied week 🌈")
                }
            }

            tvInsight.text = insights.joinToString("\n• ", "• ") // Show insights in bullet points
            Log.d("MoodChart", "Insights generated for $daysWithData days with data")
        } catch (e: Exception) {
            Log.e("MoodChart", "Error generating insights: ${e.message}")
            tvInsight.text = "• Keep tracking your mood for better insights!"
        }
    }
}

// Custom Chart View
class CustomChartView @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var moodData: List<Pair<String, Float>> = emptyList() // List of day -> mood value
    private val paint = Paint() // Main line/outline paint
    private val textPaint = Paint() // Text for labels
    private val gridPaint = Paint() // Grid lines
    private val barPaint = Paint() // Bars

    init {
        setupPaints() // Configure colors, stroke width, etc.
    }

    private fun setupPaints() {
        // Bar paint
        barPaint.isAntiAlias = true
        barPaint.color = ContextCompat.getColor(context, R.color.purpleLight)
        barPaint.style = Paint.Style.FILL

        // Text paint
        textPaint.isAntiAlias = true
        textPaint.color = ContextCompat.getColor(context, R.color.textPrimary)
        textPaint.textSize = 28f
        textPaint.textAlign = Paint.Align.CENTER

        // Grid paint
        gridPaint.isAntiAlias = true
        gridPaint.color = ContextCompat.getColor(context, R.color.textSecondary)
        gridPaint.strokeWidth = 1f
        gridPaint.alpha = 80

        // Main paint for lines
        paint.isAntiAlias = true
        paint.color = ContextCompat.getColor(context, R.color.purpleLight)
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
    }

    fun setMoodData(data: List<Pair<String, Float>>) {
        moodData = data
        invalidate() // Redraw the chart
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        try {
            if (moodData.isEmpty()) {
                drawNoDataMessage(canvas) // Show no data message
                return
            }

            drawBarChart(canvas) // Draw chart bars
            Log.d("CustomChart", "Chart drawn successfully with ${moodData.size} data points")
        } catch (e: Exception) {
            Log.e("CustomChart", "Error drawing chart: ${e.message}")
            drawErrorMessage(canvas)
        }
    }

    private fun drawNoDataMessage(canvas: Canvas) {
        textPaint.color = ContextCompat.getColor(context, R.color.textSecondary)
        textPaint.textSize = 32f
        canvas.drawText("No Data Available", width / 2f, height / 2f, textPaint)
    }

    private fun drawErrorMessage(canvas: Canvas) {
        textPaint.color = Color.RED
        textPaint.textSize = 24f
        canvas.drawText("Chart Error", width / 2f, height / 2f, textPaint)
    }

    private fun drawBarChart(canvas: Canvas) {
        val padding = 100f // Space for labels
        val chartWidth = width - 2 * padding
        val chartHeight = height - 3 * padding
        val barWidth = chartWidth / moodData.size - 20f

        drawGridAndLabels(canvas, padding, chartWidth, chartHeight) // Draw background grid

        // Draw each bar
        for (i in moodData.indices) {
            drawBar(canvas, i, padding, chartWidth, chartHeight, barWidth)
        }
    }

    private fun drawGridAndLabels(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        // Horizontal lines for moods 1-5
        for (i in 0..5) {
            val y = padding + (i * chartHeight / 5)
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint)

            // Mood level labels
            textPaint.textSize = 14f
            textPaint.textAlign = Paint.Align.RIGHT
            val moodLevel = 5 - i
            val moodText = when (moodLevel) {
                5 -> "Great"
                4 -> "Good"
                3 -> "Okay"
                2 -> "Bad"
                1 -> "Awful"
                else -> ""
            }
            canvas.drawText(moodText, padding - 10f, y + 5f, textPaint)
        }
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun drawBar(canvas: Canvas, index: Int, padding: Float, chartWidth: Float, chartHeight: Float, barWidth: Float) {
        val moodValue = moodData[index].second
        val dayLabel = moodData[index].first

        val barSpacing = chartWidth / moodData.size
        val x = padding + index * barSpacing + (barSpacing - barWidth) / 2

        val barHeight = if (moodValue > 0) (moodValue / 5f) * chartHeight else 10f
        val y = padding + chartHeight - barHeight

        // Color based on mood
        val barColor = when {
            moodValue >= 4 -> ContextCompat.getColor(context, R.color.progressBar)
            moodValue >= 3 -> ContextCompat.getColor(context, R.color.purpleLight)
            moodValue >= 2 -> ContextCompat.getColor(context, R.color.lavender)
            moodValue > 0 -> ContextCompat.getColor(context, R.color.pinkish)
            else -> ContextCompat.getColor(context, R.color.textSecondary)
        }

        barPaint.color = barColor
        canvas.drawRect(x, y, x + barWidth, padding + chartHeight, barPaint)

        // Day label
        textPaint.textSize = 16f
        textPaint.color = ContextCompat.getColor(context, R.color.textPrimary)
        canvas.drawText(dayLabel, x + barWidth / 2, padding + chartHeight + 30f, textPaint)

        // Mood value text above bar
        if (moodValue > 0) {
            textPaint.textSize = 12f
            textPaint.color = ContextCompat.getColor(context, R.color.textPrimary)
            val valueText = when (moodValue.toInt()) {
                5 -> "Great"
                4 -> "Good"
                3 -> "Okay"
                2 -> "Bad"
                1 -> "Awful"
                else -> moodValue.toInt().toString()
            }
            canvas.drawText(valueText, x + barWidth / 2, y - 10f, textPaint)
        }
    }
}
