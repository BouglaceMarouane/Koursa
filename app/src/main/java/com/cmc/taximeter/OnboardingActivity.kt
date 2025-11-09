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

/**
 * OnboardingActivity
 *
 * Displays an introductory walkthrough for first-time users.
 *
 * Key Features:
 * - Multi-page swipeable introduction using ViewPager2
 * - Page indicators showing current position
 * - Skip button (hidden on last page)
 * - Next button (becomes "Commencer" on last page)
 * - Checks if onboarding was already seen
 * - Saves completion status to SharedPreferences
 *
 * Onboarding Pages:
 * 1. Real-Time Tracking - Google Maps integration
 * 2. Automatic Calculation - Distance and time-based fares
 * 3. Driver Profile - QR code sharing
 * 4. Notifications - Ride completion alerts
 *
 * Navigation Flow:
 * - First launch → Onboarding → Authentication
 * - Subsequent launches → Skip directly to Authentication
 *
 * Storage:
 * Uses "UserPreferences" SharedPreferences with key "hasSeenOnboarding"
 * (Matches SplashActivity's storage convention)
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class OnboardingActivity : AppCompatActivity() {

    // ============================================
    // VIEW PROPERTIES
    // ============================================

    /** ViewPager for swiping between onboarding pages */
    private lateinit var viewPager: ViewPager2

    /** Button to proceed to next page or complete onboarding */
    private lateinit var btnNext: Button

    /** Button to skip onboarding and go directly to authentication */
    private lateinit var btnSkip: Button

    /** Container for page indicator dots */
    private lateinit var indicatorLayout: LinearLayout

    // ============================================
    // ONBOARDING CONTENT
    // ============================================

    /**
     * List of onboarding items to display.
     *
     * Each item contains:
     * - Image resource ID
     * - Title text
     * - Description text
     */

    private val onboardingItems = listOf(
        OnboardingItem(
            image = null,
            lottieFile = R.raw.lottie_money,
            title = "Real-Time Tracking",
            description = "Track your position on the map in real-time with Google Maps"
        ),
        OnboardingItem(
            image = null,
            lottieFile = R.raw.lottie_location,
            title = "Automatic Calculation",
            description = "Fare is calculated automatically based on distance and time"
        ),
        OnboardingItem(
            image = null,
            lottieFile = R.raw.lottie_chart,
            title = "Complete Trip History",
            description = "View and manage all your past trips with detailed statistics"
        ),
        OnboardingItem(
            image = null,
            lottieFile = R.raw.lottie_taxi,
            title = "Let's Get Started!",
            description = "Join thousands of drivers using our smart taxi meter"
        )
    )
    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the activity is created.
     *
     * Flow:
     * 1. Sets status bar color
     * 2. Checks if onboarding was already completed
     * 3. If completed → Navigate directly to Authentication
     * 4. If not completed → Show onboarding screens
     * 5. Initialize ViewPager and indicators
     * 6. Setup page change callbacks
     * 7. Setup button listeners
     *
     * @param savedInstanceState Previously saved state (if any)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black for consistent UI
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // ===== CHECK ONBOARDING STATUS =====
        // IMPORTANT: Use "UserPreferences" to match SplashActivity
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        if (hasSeenOnboarding) {
            // User has already seen onboarding - skip to authentication
            navigateToAuth()
            return  // Don't proceed with onCreate
        }

        // ===== FIRST TIME USER - SHOW ONBOARDING =====
        setContentView(R.layout.activity_onboarding)

        // Initialize view references
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        indicatorLayout = findViewById(R.id.indicatorLayout)

        // ===== SETUP VIEWPAGER =====
        val adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        // ===== SETUP INDICATORS =====
        setupIndicators()
        setCurrentIndicator(0)  // Highlight first page

        // ===== PAGE CHANGE LISTENER =====
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            /**
             * Called when user swipes to a new page.
             *
             * Updates:
             * - Page indicator dots
             * - Button text (last page shows "Commencer" instead of "Suivant")
             * - Skip button visibility (hidden on last page)
             *
             * @param position Index of the new page (0-based)
             */
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Update indicator to show current page
                setCurrentIndicator(position)

                // Check if this is the last page
                if (position == onboardingItems.size - 1) {
                    // ===== LAST PAGE =====
                    btnNext.text = "Start"  // Begin/Start
                    btnSkip.visibility = View.GONE  // Hide skip button
                } else {
                    // ===== OTHER PAGES =====
                    btnNext.text = "Next"  // Next
                    btnSkip.visibility = View.VISIBLE  // Show skip button
                }
            }
        })

        // ===== BUTTON LISTENERS =====

        // Next/Begin button
        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingItems.size - 1) {
                // Not on last page - go to next page
                viewPager.currentItem += 1
            } else {
                // On last page - complete onboarding
                completeOnboarding()
            }
        }

        // Skip button
        btnSkip.setOnClickListener {
            // Skip directly to authentication
            completeOnboarding()
        }
    }

    // ============================================
    // INDICATOR METHODS
    // ============================================

    /**
     * Creates page indicator dots dynamically.
     *
     * Creates one ImageView (dot) for each onboarding page.
     * All dots initially show as inactive (gray).
     *
     * Dots are added to indicatorLayout with equal spacing.
     */
    private fun setupIndicators() {
        // Create array to hold indicator ImageViews
        val indicators = arrayOfNulls<ImageView>(onboardingItems.size)

        // Configure layout parameters for each dot
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)  // 8dp spacing between dots

        // Create one dot for each page
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageResource(R.drawable.indicator_inactive)  // Gray dot
            indicators[i]?.layoutParams = layoutParams
            indicatorLayout.addView(indicators[i])  // Add to container
        }
    }

    /**
     * Updates indicators to highlight the current page.
     *
     * Active page dot: Yellow/highlighted
     * Other page dots: Gray/inactive
     *
     * @param position Index of current page (0-based)
     */
    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorLayout.childCount

        // Loop through all indicator dots
        for (i in 0 until childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView

            if (i == position) {
                // This is the current page - highlight it
                imageView.setImageResource(R.drawable.indicator_active)
            } else {
                // Not current page - show as inactive
                imageView.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }

    // ============================================
    // NAVIGATION METHODS
    // ============================================

    /**
     * Marks onboarding as completed and navigates to authentication.
     *
     * Actions:
     * 1. Saves "hasSeenOnboarding" = true to SharedPreferences
     * 2. Navigates to AuthenticationActivity
     *
     * Called when:
     * - User completes all pages and taps "Commencer"
     * - User taps "Skip" button
     *
     * Storage key: "hasSeenOnboarding" in "UserPreferences"
     * This matches SplashActivity's check, ensuring consistency.
     */
    private fun completeOnboarding() {
        // ===== SAVE COMPLETION STATUS =====
        // IMPORTANT: Use "UserPreferences" and "hasSeenOnboarding"
        // to match SplashActivity
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("hasSeenOnboarding", true).apply()

        // ===== NAVIGATE TO AUTHENTICATION =====
        navigateToAuth()
    }

    /**
     * Navigates to the authentication screen.
     *
     * Calls finish() to remove onboarding from back stack,
     * preventing users from returning to it via back button.
     */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()  // Remove onboarding from back stack
    }

    // ============================================
    // BACK BUTTON HANDLING
    // ============================================

    /**
     * Handles device back button press.
     *
     * Calls finishAffinity() to close the app entirely,
     * preventing navigation back to splash screen.
     *
     * This ensures clean exit from onboarding flow.
     *
     * @deprecated Use OnBackPressedDispatcher for modern implementation
     */
    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent going back to splash screen
        finishAffinity()
    }
}

// ============================================
// DATA CLASS
// ============================================

/**
 * Represents a single onboarding page.
 *
 * @property image Drawable resource ID for the page image
 * @property lottie Drawable resource ID for the page Lottie option
 * @property title Title text displayed on the page
 * @property description Description text displayed on the page
 */
data class OnboardingItem(
    val image: Int?,              // Make nullable
    val lottieFile: Int?,         // Add Lottie option
    val title: String,
    val description: String
)