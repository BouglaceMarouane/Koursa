package com.cmc.taximeter

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var tabLayout: TabLayout
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make app edge-to-edge (status bar visible, toolbar gone)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableEdgeToEdge()
        }

        setContentView(R.layout.activity_main)

        // Optional: change status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Initialize layout elements
        drawerLayout = findViewById(R.id.drawer_layout)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Setup ViewPager
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("loggedInUser", "email@exemple.com") ?: "email@exemple.com"

        val adapter = ViewPagerAdapter(this, userEmail)
        viewPager.adapter = adapter

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
    }
}
