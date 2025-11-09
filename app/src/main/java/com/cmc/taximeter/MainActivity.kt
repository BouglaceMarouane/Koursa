package com.cmc.taximeter

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableEdgeToEdge()
        }

        setContentView(R.layout.activity_main)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Get logged in user email
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("loggedInUser", "email@exemple.com") ?: "email@exemple.com"

        // Setup ViewPager with adapter
        val adapter = ViewPagerAdapter(this, userEmail)
        viewPager.adapter = adapter

        // Disable swipe for smoother experience (optional)
        viewPager.isUserInputEnabled = true

        // Setup TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.meter)
                    tab.text = "Compteur"
                }
                1 -> {
                    tab.setIcon(R.drawable.map)
                    tab.text = "Carte"
                }
                2 -> {
                    tab.setIcon(R.drawable.user)
                    tab.text = "Utilisateur"
                }
            }
        }.attach()

        // Add tab selection animation
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.view?.animate()
                    ?.scaleX(1.1f)
                    ?.scaleY(1.1f)
                    ?.setDuration(200)
                    ?.start()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.animate()
                    ?.scaleX(1.0f)
                    ?.scaleY(1.0f)
                    ?.setDuration(200)
                    ?.start()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: add bounce effect when tab is reselected
            }
        })

        // Set initial tab (Compteur)
        viewPager.setCurrentItem(0, false)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If on first tab, exit app
            super.onBackPressed()
        } else {
            // Go back to first tab
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}