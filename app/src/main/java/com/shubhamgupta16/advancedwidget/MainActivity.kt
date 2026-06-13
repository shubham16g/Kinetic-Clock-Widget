package com.shubhamgupta16.advancedwidget

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
    }

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

//        startActivity(Intent(this, DebugTestActivity::class.java))

        val storageSwitch = findViewById<SwitchCompat>(R.id.storageSwitch)
        val backgroundSwitch = findViewById<SwitchCompat>(R.id.backgroundSwitch)
//        findViewById<View>(R.id.settings_btn).setOnClickListener {
//            startActivity(Intent(this, DebugTestActivity::class.java))
//        }
        
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
        val hasStoragePermission = Utils.haveMangeStoragePermission(this)
        val hasBgPermission = Utils.hasIgnoreBatteryOptimizationPermission(this)

        findViewById<SwitchCompat>(R.id.storageSwitch).isChecked = hasStoragePermission
        findViewById<SwitchCompat>(R.id.backgroundSwitch).isChecked = hasBgPermission

        findViewById<View>(R.id.card_storage_permission).visibility = if (hasStoragePermission) View.GONE else View.VISIBLE
        findViewById<View>(R.id.card_background_permission).visibility = if (hasBgPermission) View.GONE else View.VISIBLE
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