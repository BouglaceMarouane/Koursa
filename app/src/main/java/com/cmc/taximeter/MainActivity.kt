package com.cmc.taximeter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.cmc.taximeter.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var viewPager: ViewPager2
    lateinit var tabLayout: TabLayout
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Si vous ciblez Android 11 ou plus, vous pouvez activer la gestion des bords
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableEdgeToEdge()
        }

        setContentView(R.layout.activity_main)



        drawerLayout = findViewById(R.id.drawer_layout)

        // Configuration de la barre d'action
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configuration du NavigationView et du listener
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // ActionBarDrawerToggle pour le menu hamburger
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configuration du ViewPager et des onglets
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)


        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("loggedInUser", "email@exemple.com") ?: "email@exemple.com"  // Récupère l'email de l'utilisateur connecté

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logOutUser -> {
                // Créez un PopupMenu qui s'affiche quand l'utilisateur clique sur l'élément logOutUser
                val popupMenu = PopupMenu(this, findViewById(R.id.logOutUser))

                // Ajoutez un item dans le PopupMenu pour le logout
                popupMenu.menu.add(Menu.NONE, 1, Menu.NONE, "Se déconnecter")

                // Ajoutez un listener pour détecter la sélection de l'élément dans le PopupMenu
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> {
                            // Quand le bouton de logout est cliqué, lancez l'Activity de connexion
                            val intent = Intent(this, AuthenticationActivity::class.java)
                            startActivity(intent)
                            finish()
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> Toast.makeText(this, "Page d'accueil sélectionnée", Toast.LENGTH_SHORT).show()
            R.id.nav_info -> Toast.makeText(this, "Informations sélectionnées", Toast.LENGTH_SHORT).show()
            R.id.nav_share -> Toast.makeText(this, "Partager sélectionné", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
