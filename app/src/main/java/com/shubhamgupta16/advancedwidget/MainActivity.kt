package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.shubhamgupta16.advancedwidget.kinetic96.KineticHorizontalClockWidget
import com.shubhamgupta16.advancedwidget.kinetic96.KineticVerticalClockWidget

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

        findViewById<View>(R.id.btn_add_widget).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(this)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val options = arrayOf(
                        getString(R.string.widget_vertical_label),
                        getString(R.string.widget_horizontal_label)
                    )
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.select_widget_type)
                        .setItems(options) { _, which ->
                            val providerClass = if (which == 0) {
                                KineticVerticalClockWidget::class.java
                            } else {
                                KineticHorizontalClockWidget::class.java
                            }
                            val provider = ComponentName(this, providerClass)
                            appWidgetManager.requestPinAppWidget(provider, null, null)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                } else {
                    Toast.makeText(this, R.string.pin_widget_not_supported, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, R.string.pin_widget_not_supported, Toast.LENGTH_SHORT).show()
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
