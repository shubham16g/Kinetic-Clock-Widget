package com.shubhamgupta16.advancedwidget

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val storageSwitch = findViewById<SwitchCompat>(R.id.storageSwitch)
        val backgroundSwitch = findViewById<SwitchCompat>(R.id.backgroundSwitch)
        
        // Set initial states
        refreshSwitchStates()
        
        // Set up listeners
        storageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !hasStoragePermission()) {
                requestStoragePermission()
            }
        }
        
        backgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestIgnoreBatteryOptimization()
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimization(isOnRequest: Boolean) {
        Log.d(TAG, "requestIgnoreBatteryOptimization: onRequest: $isOnRequest")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = packageName

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback: open general battery settings
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(fallbackIntent)
                }
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

    private fun hasIgnoreBatteryOptimizationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                return false
            }
        }
        return true
    }


    
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName"))
                intent.setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
//        }
    }
    
    private fun refreshSwitchStates() {
        findViewById<SwitchCompat>(R.id.storageSwitch).isChecked = hasStoragePermission()
        findViewById<SwitchCompat>(R.id.backgroundSwitch).isChecked = hasIgnoreBatteryOptimizationPermission()
    }
    
    override fun onResume() {
        super.onResume()
        refreshSwitchStates() // Refresh when returning from permission screens
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
                refreshSwitchStates()
            }
        }
    }
}