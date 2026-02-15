package com.dinar.myproject

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        bottom.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, dest, _ ->
            bottom.visibility = when (dest.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.registerFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }
}
