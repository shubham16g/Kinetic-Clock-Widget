package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class SettingsActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_WIDGET_ID = "widget_id"
    }
    
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onBackPressed() {
        finishWithResult()
        super.onBackPressed()
    }



    private fun finishWithResult() {
        // Set the result to OK and include the widget ID
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(RESULT_OK, resultValue)

        // Update the widget one final time before closing
        val appWidgetManager = AppWidgetManager.getInstance(this)
        TestWidget.updateAppWidget(this, appWidgetManager, TestWidget.SETTINGS_KICK, widgetId)
    }

    override fun onDestroy() {
        // Remove this code from onDestroy - setting result here is too late
        // if (isFinishing) {
        //     val resultValue = Intent().apply {
        //         putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        //     }
        //     setResult(RESULT_OK, resultValue)
        //
        //     TestWidget.configureWidget(this, widgetId)
        // }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        
        // Get widget ID from intent
        widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        
        if (savedInstanceState == null) {
            val fragment = SettingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_WIDGET_ID, widgetId)
                }
            }
            
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit()
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = if (widgetId != -1) "Widget $widgetId Settings" else "Widget Settings"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
        
        private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                widgetId = it.getInt(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            }
        }
        
        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            return super.onPreferenceTreeClick(preference)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            Log.d("WidgetSettings", "Updating widget $widgetId for key: $key")

            if (key == null || sharedPreferences == null || widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

            // Get the new value based on type and save it to widget-specific preferences
            when (val preference = findPreference<Preference>(key)) {
                is androidx.preference.CheckBoxPreference -> {
                    val value = sharedPreferences.getBoolean(key, false)
                    saveWidgetPreference(key, value)
                }
                is androidx.preference.EditTextPreference -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    saveWidgetPreference(key, value)
                }
                is androidx.preference.ListPreference -> {
                    val value = sharedPreferences.getString(key, "") ?: ""
                    saveWidgetPreference(key, value)
                }
                else -> {
                    // Handle other preference types as needed
                    if (preference != null) {
                        Log.d("WidgetSettings", "Unhandled preference type: ${preference.javaClass.simpleName}")
                    }
                }
            }

            // Update the widget immediately to reflect changes
            context?.let { ctx ->
                val appWidgetManager = AppWidgetManager.getInstance(ctx)
                TestWidget.updateAppWidget(ctx, appWidgetManager, TestWidget.SETTINGS_KICK, widgetId)
            }
        }
        
        // Save widget-specific preferences
        private fun saveWidgetPreference(key: String, value: Any) {
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
            
            context?.let { ctx ->
                val prefs = ctx.getSharedPreferences("widget_$widgetId", 0)
                val editor = prefs.edit()
                
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Long -> editor.putLong(key, value)
                }
                
                editor.apply()
            }
        }
    }


}