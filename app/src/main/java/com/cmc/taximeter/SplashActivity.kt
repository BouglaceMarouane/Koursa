package com.cmc.taximeter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cmc.taximeter.R

class SplashActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        // Set status bar to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logo = findViewById<ImageView>(R.id.splashLogo)

        logo.alpha = 0f
        logo.animate().alpha(1f).setDuration(3000)

        val splashDuration: Long = 4000
        Handler().postDelayed({
            val intent = Intent(this, OnboardingActivity::class.java)  // Changed from AuthenticationActivity
            startActivity(intent)
            finish()
        }, splashDuration)

    }
}