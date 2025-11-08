package com.cmc.taximeter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION: Long = 4000 // 4 seconds

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Animate logo
        val logo = findViewById<ImageView>(R.id.splashLogo)
        logo.alpha = 0f
        logo.animate().alpha(1f).setDuration(3000)

        // Navigate after splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DURATION)
    }

    private fun navigateToNextScreen() {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString("loggedInUser", null)
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        val intent = when {
            // User is logged in -> Go directly to MainActivity
            loggedInUser != null -> {
                Intent(this, MainActivity::class.java)
            }

            // User has seen onboarding but not logged in -> Go to Authentication
            hasSeenOnboarding -> {
                Intent(this, AuthenticationActivity::class.java)
            }

            // First time user -> Go to Onboarding
            else -> {
                Intent(this, OnboardingActivity::class.java)
            }
        }

        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Do nothing - prevent going back from splash screen
    }
}