package com.shubhamgupta16.advancedwidget

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    private lateinit var btnBg: View
    private lateinit var powerBtn: ImageButton
    private lateinit var settingsBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sf = PreferenceManager.getDefaultSharedPreferences(this);
        if (sf.getBoolean("init", false)) {
            val editor = sf.edit()
            editor.putInt("pinsColor", Color.parseColor("#000000"))
            editor.putInt("bgColor", Color.parseColor("#ffffff"))
            editor.putBoolean("init", true)
            editor.apply()
        }

        btnBg = findViewById(R.id.btn_bg)
        powerBtn = findViewById(R.id.power_btn)
        settingsBtn = findViewById(R.id.settings_btn)

        powerBtn.setOnClickListener {
            if (isServiceRunning(RunnerService::class.java))
                offService()
            else
                onService()
        }
        settingsBtn.setOnClickListener {
            openSettings()
        }
    }

    override fun onStart() {
        super.onStart()
        if (isServiceRunning(RunnerService::class.java))
            onUI()
        else
            offUI()
    }

    private fun offService() {
        val i = Intent(this, RunnerService::class.java)
        i.action = RunnerService.STOP_FOREGROUND_ACTION
        startService(i)
        offUI()
    }

    private fun offUI() {
        btnBg.setBackgroundResource(R.drawable.background_power_bg_off)
        settingsBtn.alpha = 0.5f
        settingsBtn.elevation = 0f
        settingsBtn.isEnabled = false
    }

    private fun onUI() {
        btnBg.setBackgroundResource(R.drawable.background_power_bg_on)
        settingsBtn.alpha = 1f
        settingsBtn.elevation = dpToPx(10)
        settingsBtn.isEnabled = true
    }

    private fun onService() {
        tryStoragePermissionAndEnable()
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    @SuppressLint("BatteryLife")
    private fun tryStoragePermissionAndEnable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                return false
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 12)
                return false
            }
        }
//        if (ids.isNotEmpty()) {
        val i = Intent(this, RunnerService::class.java)
        i.action = RunnerService.START_FOREGROUND_ACTION
        startService(i)
        onUI()
//        } else{
//            Toast.makeText(this, "No WIDGET found on Home Screen", Toast.LENGTH_SHORT).show()
//        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 12) {
            tryStoragePermissionAndEnable()
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