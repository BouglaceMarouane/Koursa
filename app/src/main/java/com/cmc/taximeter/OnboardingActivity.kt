package com.cmc.taximeter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private lateinit var indicatorLayout: LinearLayout

    private val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.ic_taxi_map,
            "Suivi en Temps Réel",
            "Suivez votre position sur la carte en temps réel avec Google Maps"
        ),
        OnboardingItem(
            R.drawable.ic_calculator,
            "Calcul Automatique",
            "Le tarif est calculé automatiquement selon la distance et le temps"
        ),
        OnboardingItem(
            R.drawable.ic_profile,
            "Profil Chauffeur",
            "Gérez votre profil et partagez vos informations via QR Code"
        ),
        OnboardingItem(
            R.drawable.ic_notification,
            "Notifications",
            "Recevez des notifications pour chaque course terminée"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Check if onboarding has been completed
        // CHANGED: Use "UserPreferences" instead of "OnboardingPrefs" to match SplashActivity
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        if (hasSeenOnboarding) {
            navigateToAuth()
            return
        }

        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        indicatorLayout = findViewById(R.id.indicatorLayout)

        val adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        setupIndicators()
        setCurrentIndicator(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                if (position == onboardingItems.size - 1) {
                    btnNext.text = "Commencer"
                    btnSkip.visibility = View.GONE
                } else {
                    btnNext.text = "Suivant"
                    btnSkip.visibility = View.VISIBLE
                }
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingItems.size - 1) {
                viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onboardingItems.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageResource(R.drawable.indicator_inactive)
            indicators[i]?.layoutParams = layoutParams
            indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageResource(R.drawable.indicator_active)
            } else {
                imageView.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun completeOnboarding() {
        // CHANGED: Use "UserPreferences" and "hasSeenOnboarding" to match SplashActivity
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("hasSeenOnboarding", true).apply()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back to splash
        finishAffinity()
    }
}

data class OnboardingItem(
    val image: Int,
    val title: String,
    val description: String
)