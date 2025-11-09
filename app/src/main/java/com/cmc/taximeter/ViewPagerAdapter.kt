package com.cmc.taximeter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPagerAdapter
 *
 * Fragment adapter for MainActivity's tab navigation system.
 *
 * Purpose:
 * - Manages three main fragments (Meter, Maps, User)
 * - Provides fragments to ViewPager2
 * - Passes user email to UserFragment via Bundle arguments
 *
 * Fragments managed:
 * - Position 0: MeterFragment (Taximeter functionality)
 * - Position 1: MapsFragment (Google Maps tracking)
 * - Position 2: UserFragment (Profile and logout)
 *
 * FragmentStateAdapter benefits:
 * - Efficient memory management
 * - Handles fragment lifecycle automatically
 * - Optimized for ViewPager2
 *
 * @param fragmentActivity The host activity (MainActivity)
 * @param userEmail Email of logged-in user (passed to UserFragment)
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class ViewPagerAdapter(fragmentActivity: FragmentActivity, userEmail: String) : FragmentStateAdapter(fragmentActivity) {

    /**
     * Email of currently logged-in user.
     *
     * Passed from MainActivity and forwarded to UserFragment
     * so it can load and display the correct user's profile.
     */
    private val userEmail: String = userEmail

    /**
     * Returns the number of fragments in the ViewPager.
     *
     * @return 3 (Meter, Maps, User fragments)
     */
    override fun getItemCount(): Int = 3

    /**
     * Creates and returns the fragment for a given position.
     *
     * Fragment mapping:
     * - Position 0: MeterFragment
     * - Position 1: MapsFragment
     * - Position 2: UserFragment (with email in arguments)
     * - Default: MeterFragment (fallback, should never happen)
     *
     * Special handling for UserFragment:
     * - Creates Bundle with "userEmail" key
     * - Sets Bundle as fragment arguments
     * - Allows UserFragment to access user email via arguments
     *
     * @param position The position in the ViewPager (0, 1, or 2)
     * @return Fragment instance for the requested position
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            // ===== POSITION 0: METER FRAGMENT =====
            0 -> MeterFragment()  // Taximeter with fare calculation

            // ===== POSITION 1: MAPS FRAGMENT =====
            1 -> MapsFragment()   // Google Maps with real-time tracking

            // ===== POSITION 2: USER FRAGMENT =====
            2 -> {
                // User profile fragment needs email to load data
                val fragment = UserFragment()

                // Create Bundle with user email
                val bundle = Bundle()
                bundle.putString("userEmail", userEmail)  // Pass email to fragment

                // Attach Bundle to fragment
                fragment.arguments = bundle

                // Return configured fragment
                fragment
            }

            // ===== DEFAULT: METER FRAGMENT (FALLBACK) =====
            else -> MeterFragment()  // Should never be reached with itemCount = 3
        }
    }
}