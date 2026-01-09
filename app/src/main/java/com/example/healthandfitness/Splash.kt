package com.example.healthandfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    // How long the splash screen stays (in milliseconds)
    private val SPLASH_DELAY: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Handler is used to add a small delay before opening the next screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Create an intent to move from SplashActivity to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // close splash screen so user can’t come back to it
        }, SPLASH_DELAY)
    }
}
