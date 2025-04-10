package com.shubhamgupta16.advancedwidget

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import com.shubhamgupta16.advancedwidget.RunnerService.Companion.STOP_FOREGROUND_ACTION
import com.shubhamgupta16.advancedwidget.RunnerService.Companion.sendTimeTick


class MainActivity : AppCompatActivity() {

    private lateinit var storageSwitch: SwitchCompat
    private lateinit var backgroundSwitch: SwitchCompat
    private lateinit var settingsBtn: ImageButton
    private lateinit var image: ImageView
    private lateinit var draw: Draw

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sf = PreferenceManager.getDefaultSharedPreferences(this);
        sf.edit().putBoolean("isAnimating", false).apply()
        if (sf.getBoolean("init", false)) {
            val editor = sf.edit()
            editor.putInt("pinsColor", Color.parseColor("#000000"))
            editor.putInt("bgColor", Color.parseColor("#ffffff"))
            editor.putBoolean("init", true)
            editor.apply()
        }

        settingsBtn = findViewById(R.id.settings_btn)
        storageSwitch = findViewById(R.id.storageSwitch)
        backgroundSwitch = findViewById(R.id.backgroundSwitch)
        image = findViewById(R.id.image)
        draw = Draw(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            storageSwitch.visibility = View.GONE
            backgroundSwitch.visibility = View.GONE
        } else {

            storageSwitch.setOnClickListener {
                if (storageSwitch.isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 12)
                        storageSwitch.isChecked = false
                    }
                }
            }
            backgroundSwitch.setOnClickListener {
                optionalAlwaysRunInBackground(backgroundSwitch.isChecked)
            }
        }

        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    @SuppressLint("BatteryLife")
    private fun optionalAlwaysRunInBackground(isOnRequest: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "HERE", Toast.LENGTH_SHORT).show()
            val intent = Intent()
            if (isOnRequest) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
            } else {
                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS;
            }
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

    }




    override fun onStart() {
        super.onStart()
        _updateImage()
//        if (isServiceRunning(RunnerService::class.java))
//            onUI()
//        else
//            offUI()
//        updateSwitchUI()
    }

    private fun _updateImage() {
        val isByTime = draw.init()
        if (isByTime) {
            draw.drawAnim { fraction ->
                if (fraction == 0f || fraction == 1f) {
//                    editor.putBoolean("isAnimating", fraction == 0f).apply()
                }
                image.setImageBitmap(draw.createTimeClockFrame(fraction))
            }
        } else {
            image.setImageBitmap(draw.createStaticTimeClock())
        }
    }

    private fun offUI() {
        settingsBtn.alpha = 0.5f
        settingsBtn.elevation = 0f
        settingsBtn.isEnabled = false
    }

    private fun onUI() {
        settingsBtn.alpha = 1f
        settingsBtn.elevation = dpToPx(10)
        settingsBtn.isEnabled = true
    }

    private fun updateSwitchUI() {
        if (hasStoragePermission()){
            storageSwitch.visibility = View.GONE
        }
        backgroundSwitch.isChecked = hasBackgroundPermission()
        if (storageSwitch.visibility == View.GONE) {
            if (!isServiceRunning(RunnerService::class.java)) {
                val intent = Intent(this, RunnerService::class.java)
                startService(intent)
                onUI()
                Toast.makeText(this, "Clock Enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun hasBackgroundPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 12) {
//            updateSwitchUI()
        }
    }

    private fun dpToPx(dp: Int): Float {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    companion object {
        @Suppress("DEPRECATION") // Deprecated for third party Services.
        fun <T> Context.isServiceRunning(service: Class<T>) =
            (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == service.name }

    }
}