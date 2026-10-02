package com.shubhamgupta16.advancedwidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.appbar)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val backgroundSwitch = findViewById<SwitchCompat>(R.id.backgroundSwitch)

        // Set initial states
        refreshSwitchStates()

        // Set up listeners
        backgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Utils.requestIgnoreBatteryOptimization(this)
            }
        }

        findViewById<View>(R.id.btn_privacy_policy).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://kinetic-clock-widget.netlify.app/privacy_policy"))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun refreshSwitchStates() {
        val hasBgPermission = Utils.hasIgnoreBatteryOptimizationPermission(this)

        findViewById<SwitchCompat>(R.id.backgroundSwitch).isChecked = hasBgPermission
        findViewById<View>(R.id.card_background_permission).visibility = if (hasBgPermission) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        refreshSwitchStates() // Refresh when returning from permission screens
    }
}