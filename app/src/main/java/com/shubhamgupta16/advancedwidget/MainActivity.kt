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
            if (isChecked) {
                Utils.requestStoragePermission(this)
            }
        }
        
        backgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Utils.requestIgnoreBatteryOptimization(this)
            }
        }
    }




    

    
    private fun refreshSwitchStates() {
        findViewById<SwitchCompat>(R.id.storageSwitch).isChecked = Utils.haveMangeStoragePermission(this)
        findViewById<SwitchCompat>(R.id.backgroundSwitch).isChecked = Utils.hasIgnoreBatteryOptimizationPermission(this)
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