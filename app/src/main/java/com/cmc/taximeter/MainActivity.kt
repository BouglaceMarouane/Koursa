package com.cmc.taximeter

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * MainActivity
 *
 * Main container activity for the authenticated user experience.
 *
 * Architecture:
 * - Uses ViewPager2 for swipeable tab navigation
 * - TabLayout for tab indicators and navigation
 * - Hosts three main fragments: Meter, Maps, and User
 *
 * Key Features:
 * - Three-tab navigation (Meter, Map, User)
 * - Tab icons and labels
 * - Smooth swipe navigation between tabs
 * - Tab selection animations (scale effect)
 * - Custom back button behavior (navigates through tabs)
 * - Passes user email to fragments via ViewPagerAdapter
 *
 * Tabs:
 * 1. Meter: Taximeter functionality
 * 2. Map: Real-time location tracking
 * 3. User: Profile and logout
 *
 * Navigation:
 * - Swipe or tap tabs to navigate
 * - Back button: Returns to previous tab or exits app from first tab
 *
 * Technical Details:
 * - ViewPager2: Modern replacement for ViewPager with better performance
 * - TabLayoutMediator: Automatically syncs tabs with ViewPager2
 * - Edge-to-edge: Full screen experience on Android R+ (API 30+)
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class MainActivity : AppCompatActivity() {

    // ============================================
    // VIEW PROPERTIES
    // ============================================

    /** ViewPager2 for swipeable fragment navigation */
    private lateinit var viewPager: ViewPager2

    /** TabLayout for tab indicators and selection */
    private lateinit var tabLayout: TabLayout

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the activity is created.
     *
     * Initialization sequence:
     * 1. Enables edge-to-edge display (Android R+)
     * 2. Sets content view
     * 3. Sets status bar color
     * 4. Initializes ViewPager2 and TabLayout
     * 5. Retrieves logged-in user email
     * 6. Sets up ViewPager adapter with user email
     * 7. Configures TabLayout with icons and labels
     * 8. Adds tab selection animations
     * 9. Sets initial tab to Meter (index 0)
     *
     * @param savedInstanceState Previously saved state (if any)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== ENABLE EDGE-TO-EDGE DISPLAY =====
        // Available on Android R (API 30) and above
        // Extends content under system bars for immersive experience
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableEdgeToEdge()
        }

        // ===== INFLATE LAYOUT =====
        setContentView(R.layout.activity_main)

        // ===== SET STATUS BAR COLOR =====
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // ===== INITIALIZE VIEWS =====
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // ===== GET LOGGED-IN USER EMAIL =====
        // Email is needed to pass to UserFragment for profile display
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("loggedInUser", "email@example.com") ?: "email@example.com"

        // ===== SETUP VIEWPAGER =====
        val adapter = ViewPagerAdapter(this, userEmail)
        viewPager.adapter = adapter

        // Enable swipe gestures (true = swipe enabled, false = swipe disabled)
        viewPager.isUserInputEnabled = true

        // ===== SETUP TABLAYOUT WITH VIEWPAGER =====
        // TabLayoutMediator automatically syncs tabs with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Configure each tab based on its position
            when (position) {
                0 -> {
                    // ===== TAB 1: METER =====
                    tab.setIcon(R.drawable.meter)
                    tab.text = "Meter"
                }
                1 -> {
                    // ===== TAB 2: MAP =====
                    tab.setIcon(R.drawable.map)
                    tab.text = "Map"
                }
                2 -> {
                    // ===== TAB 3: USER =====
                    tab.setIcon(R.drawable.user)
                    tab.text = "User"
                }
            }
        }.attach()  // Attach mediator to link TabLayout and ViewPager2

        // ===== ADD TAB SELECTION ANIMATION =====
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            /**
             * Called when a tab is selected.
             *
             * Animates tab by scaling it up slightly to provide visual feedback.
             *
             * Animation:
             * - Scale X: 1.1 (10% larger horizontally)
             * - Scale Y: 1.1 (10% larger vertically)
             * - Duration: 200ms
             *
             * @param tab The selected tab
             */
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.view?.animate()
                    ?.scaleX(1.1f)
                    ?.scaleY(1.1f)
                    ?.setDuration(200)
                    ?.start()
            }

            /**
             * Called when a tab is unselected.
             *
             * Animates tab back to normal size.
             *
             * Animation:
             * - Scale X: 1.0 (normal size)
             * - Scale Y: 1.0 (normal size)
             * - Duration: 200ms
             *
             * @param tab The unselected tab
             */
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.animate()
                    ?.scaleX(1.0f)
                    ?.scaleY(1.0f)
                    ?.setDuration(200)
                    ?.start()
            }

            /**
             * Called when a tab is reselected (tapped again while already selected).
             *
             * Currently no action, but could add bounce effect or other feedback.
             *
             * @param tab The reselected tab
             */
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: add bounce effect when tab is reselected
            }
        })

        // ===== SET INITIAL TAB =====
        // Start on the Meter tab
        // false = no smooth scroll animation for initial positioning
        viewPager.setCurrentItem(0, false)
    }

    // ============================================
    // BACK BUTTON HANDLING
    // ============================================

    /**
     * Handles device back button press with custom navigation behavior.
     *
     * Custom back navigation:
     * - If on first tab (index 0): Exit app normally
     * - If on other tabs: Navigate back to previous tab
     *
     * This provides intuitive navigation where back button cycles
     * through tabs before exiting the app.
     *
     * Example flow:
     * - User on tab 2 → Press back → Go to tab 1
     * - User on tab 1 → Press back → Go to tab 0
     * - User on tab 0 → Press back → Exit app
     *
     * @deprecated Use OnBackPressedDispatcher for modern implementation
     */
    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // ===== ON FIRST TAB - EXIT APP =====
            // User is on Meter tab, so exit normally
            super.onBackPressed()
        } else {
            // ===== ON OTHER TABS - GO TO PREVIOUS TAB =====
            // Navigate to previous tab instead of exiting
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}