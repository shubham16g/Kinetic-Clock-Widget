package com.shubhamgupta16.advancedwidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.materialswitch.MaterialSwitch

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var backgroundSwitch: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val appbar = findViewById<View>(R.id.appbar)
        val mainScroll = findViewById<View>(R.id.main_scroll_view)

        ViewCompat.setOnApplyWindowInsetsListener(appbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(mainScroll) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom + dpToPx(16).toInt())
            insets
        }

        backgroundSwitch = findViewById(R.id.backgroundSwitch)

        refreshSwitchStates()

        backgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !Utils.hasIgnoreBatteryOptimizationPermission(this)) {
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
        backgroundSwitch.isChecked = hasBgPermission
        findViewById<View>(R.id.card_background_permission).visibility = if (hasBgPermission) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        refreshSwitchStates()
    }
}
