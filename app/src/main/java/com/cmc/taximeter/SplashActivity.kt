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

/**
 * SplashActivity
 *
 * Initial launch screen with app logo and navigation logic.
 *
 * Purpose:
 * - Provides branded launch experience
 * - Checks user authentication and onboarding status
 * - Navigates to appropriate screen based on user state
 * - Displays animated logo during delay
 *
 * Navigation Logic:
 * Three possible navigation paths based on SharedPreferences:
 *
 * 1. Logged In User:
 *    - "loggedInUser" exists in SharedPreferences
 *    - → Navigate to MainActivity (skip onboarding and login)
 *
 * 2. Returning User (Not Logged In):
 *    - "hasSeenOnboarding" = true
 *    - "loggedInUser" = null
 *    - → Navigate to AuthenticationActivity (skip onboarding)
 *
 * 3. First-Time User:
 *    - "hasSeenOnboarding" = false
 *    - "loggedInUser" = null
 *    - → Navigate to OnboardingActivity
 *
 * Splash Duration:
 * - 4 seconds (4000ms)
 * - Logo fades in during first 3 seconds
 * - Navigation occurs after full duration
 *
 * Technical Details:
 * - Uses Handler with Looper for delayed navigation
 * - Edge-to-edge display for immersive experience
 * - Fade-in animation for logo (alpha 0 → 1)
 * - System window insets handling for notches/cutouts
 * - Back button disabled (prevents exit during splash)
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class SplashActivity : AppCompatActivity() {

    // ============================================
    // CONSTANTS
    // ============================================

    /** Duration to display splash screen (milliseconds) */
    private val SPLASH_DURATION: Long = 4000 // 4 seconds

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the activity is created.
     *
     * Initialization sequence:
     * 1. Sets status bar color to black
     * 2. Enables edge-to-edge display
     * 3. Inflates layout
     * 4. Applies window insets (handles system bars)
     * 5. Animates logo (fade in)
     * 6. Schedules navigation after SPLASH_DURATION
     *
     * @param savedInstanceState Previously saved state (if any)
     *
     * @SuppressLint("MissingInflatedId") - Suppresses warning about
     * findViewById for views that may not exist in all configurations
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== SET STATUS BAR COLOR =====
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // ===== ENABLE EDGE-TO-EDGE =====
        // Extends content under system bars for full-screen experience
        enableEdgeToEdge()

        // ===== INFLATE LAYOUT =====
        setContentView(R.layout.activity_splash)

        // ===== APPLY WINDOW INSETS =====
        // Handles system bars (status bar, navigation bar) padding
        // Ensures content doesn't overlap with system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Get system bar dimensions
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply padding to avoid overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            // Return insets for other views to consume
            insets
        }

        // ===== ANIMATE LOGO =====
        val logo = findViewById<ImageView>(R.id.splashLogo)

        // Start with transparent logo
        logo.alpha = 0f

        // Fade in over 3 seconds
        logo.animate().alpha(1f).duration = 3000

        // ===== SCHEDULE NAVIGATION =====
        // Use Handler to delay navigation
        Handler(Looper.getMainLooper()).postDelayed({
            // After 4 seconds, determine and navigate to next screen
            navigateToNextScreen()
        }, SPLASH_DURATION)
    }

    // ============================================
    // NAVIGATION METHODS
    // ============================================

    /**
     * Determines appropriate screen based on user state and navigates to it.
     *
     * Decision tree:
     *
     * 1. Check if user is logged in (loggedInUser exists)
     *    YES → Go to MainActivity
     *    NO  → Continue to step 2
     *
     * 2. Check if user has seen onboarding (hasSeenOnboarding = true)
     *    YES → Go to AuthenticationActivity
     *    NO  → Go to OnboardingActivity
     *
     * SharedPreferences keys used:
     * - "loggedInUser": Email of logged-in user (null if not logged in)
     * - "hasSeenOnboarding": Boolean indicating if onboarding was completed
     *
     * Navigation behavior:
     * - Starts appropriate activity
     * - Calls finish() to remove splash from back stack
     * - User cannot navigate back to splash screen
     *
     * Storage: "UserPreferences" SharedPreferences
     */
    private fun navigateToNextScreen() {
        // ===== ACCESS SHARED PREFERENCES =====
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        // ===== CHECK USER STATE =====
        val loggedInUser = sharedPreferences.getString("loggedInUser", null)
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        // ===== DETERMINE NAVIGATION TARGET =====
        val intent = when {
            // ===== CASE 1: USER IS LOGGED IN =====
            // User has an active session - go directly to main app
            loggedInUser != null -> {
                Intent(this, MainActivity::class.java)
            }

            // ===== CASE 2: USER HAS SEEN ONBOARDING =====
            // User has completed onboarding but is not logged in
            // Skip onboarding and go to login screen
            hasSeenOnboarding -> {
                Intent(this, AuthenticationActivity::class.java)
            }

            // ===== CASE 3: FIRST-TIME USER =====
            // User has never launched the app before
            // Show onboarding flow
            else -> {
                Intent(this, OnboardingActivity::class.java)
            }
        }

        // ===== NAVIGATE =====
        startActivity(intent)
        finish()  // Remove splash from back stack
    }

    // ============================================
    // BACK BUTTON HANDLING
    // ============================================

    /**
     * Handles device back button press during splash screen.
     *
     * Behavior: Does nothing (prevents exit during splash)
     *
     * Rationale:
     * - Splash screen is temporary (4 seconds)
     * - Navigation is automatic
     * - Allowing back press would exit app prematurely
     * - Provides smoother user experience
     *
     * Note: Empty implementation overrides default back behavior
     *
     * @deprecated Use OnBackPressedDispatcher for modern implementation
     */
    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing - prevent going back from splash screen
        // This prevents user from accidentally exiting the app during splash
    }
}