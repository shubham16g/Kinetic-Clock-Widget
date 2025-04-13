package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import yuku.ambilwarna.AmbilWarnaDialog


private const val TAG = "SettingsActivity"

class SettingsActivity : AppCompatActivity() {

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var widgetPrefs: SharedPreferences

    // UI elements
    private lateinit var showBackgroundSwitch: SwitchCompat
    private lateinit var backgroundSettingsGroup: LinearLayout
    private lateinit var backgroundTypeSpinner: Spinner
    private lateinit var bgColorButton: ImageButton
    private lateinit var bgOpacitySeekBar: SeekBar
    private lateinit var showClocksSwitch: SwitchCompat
    private lateinit var clocksOpacitySeekBar: SeekBar
    private lateinit var pinsColorButton: ImageButton

    // Current values
    private var bgColor: Int = Color.WHITE
    private var pinsColor: Int = Color.BLACK


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.appbar)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        widgetPrefs = getSharedPreferences("widget_x", 0)

        // Get widget ID from intent
        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        Log.d(TAG, "onCreate: widgetId: $widgetId")

        // Initialize widget preferences

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = if (widgetId != -1) "Widget $widgetId Settings" else "Widget Settings"

        // Initialize UI elements
        initializeUiElements()

        // Load saved preferences
        loadSavedPreferences()

        // Set up listeners
        setupEventListeners()

        val intent = Intent()
        intent.putExtra(EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, intent)
        sendBroadcast(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initializeUiElements() {
        // Background settings
        showBackgroundSwitch = findViewById(R.id.switch_show_background)
        backgroundSettingsGroup = findViewById(R.id.background_settings_group)
        backgroundTypeSpinner = findViewById(R.id.spinner_background_type)
        bgColorButton = findViewById(R.id.btn_bg_color)
        bgOpacitySeekBar = findViewById(R.id.seekbar_bg_opacity)

        // Clock settings
        showClocksSwitch = findViewById(R.id.switch_show_clocks)
        clocksOpacitySeekBar = findViewById(R.id.seekbar_clocks_opacity)

        // Pins settings
        pinsColorButton = findViewById(R.id.btn_pins_color)

        // Setup spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.reply_entries,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            backgroundTypeSpinner.adapter = adapter
        }
    }

    private fun loadSavedPreferences() {
        // Load background settings
        val showBackground = widgetPrefs.getBoolean("showBackground_$widgetId", true)
        showBackgroundSwitch.isChecked = showBackground
        backgroundSettingsGroup.visibility = if (showBackground) View.VISIBLE else View.GONE

        // Load background type
        val backgroundType = widgetPrefs.getString("backgroundType_$widgetId", "solid_color") ?: "solid_color"
        val backgroundTypePosition =
            resources.getStringArray(R.array.reply_values).indexOf(backgroundType)
        if (backgroundTypePosition != -1) {
            backgroundTypeSpinner.setSelection(backgroundTypePosition)
        }

        // Load background color
        bgColor = widgetPrefs.getInt("bgColor_$widgetId", ContextCompat.getColor(this, R.color.white))
        updateColorButton(bgColorButton, bgColor)

        // Load background opacity
        val bgOpacity = widgetPrefs.getInt("bgOpacity_$widgetId", 100)
        bgOpacitySeekBar.progress = bgOpacity

        // Load clock settings
        val showClocks = widgetPrefs.getBoolean("showClocks_$widgetId", true)
        showClocksSwitch.isChecked = showClocks
        clocksOpacitySeekBar.isEnabled = showClocks

        // Load clock opacity
        val clocksOpacity = widgetPrefs.getInt("clocksOpacity_$widgetId", 40)
        clocksOpacitySeekBar.progress = clocksOpacity

        // Load pins color
        pinsColor = widgetPrefs.getInt("pinsColor_$widgetId", Color.BLACK)
        updateColorButton(pinsColorButton, pinsColor)
    }

    private fun setupEventListeners() {
        // Background visibility
        showBackgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            backgroundSettingsGroup.visibility = if (isChecked) View.VISIBLE else View.GONE
            saveWidgetPreference("showBackground", isChecked)
            updateWidget()
        }

        // Background type
        backgroundTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = resources.getStringArray(R.array.reply_values)[position]
                saveWidgetPreference("backgroundType", value)
                updateWidget()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Background color
        bgColorButton.setOnClickListener {
            showColorPicker(bgColor) { color ->
                bgColor = color
                updateColorButton(bgColorButton, color)
                saveWidgetPreference("bgColor", color)
                updateWidget()
            }
        }

        // Background opacity
        bgOpacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    saveWidgetPreference("bgOpacity", progress)
                    updateWidget()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Show clocks
        showClocksSwitch.setOnCheckedChangeListener { _, isChecked ->
            clocksOpacitySeekBar.isEnabled = isChecked
            saveWidgetPreference("showClocks", isChecked)
            updateWidget()
        }

        // Clocks opacity
        clocksOpacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    saveWidgetPreference("clocksOpacity", progress)
                    updateWidget()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Pins color
        pinsColorButton.setOnClickListener {
            showColorPicker(pinsColor) { color ->
                pinsColor = color
                updateColorButton(pinsColorButton, color)
                saveWidgetPreference("pinsColor", color)
                updateWidget()
            }
        }
    }

    private fun updateColorButton(button: ImageButton, color: Int) {
        button.setBackgroundColor(color)
        // Set text color to contrast with background
//        val luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
//        button.setTextColor(if (luminance > 0.5) Color.BLACK else Color.WHITE)
    }

    private fun showColorPicker(initialColor: Int, onColorSelected: (Int) -> Unit) {
        AmbilWarnaDialog(this, initialColor, false, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                onColorSelected(color)
            }
        }).show()
    }

    // Save widget-specific preferences
    private fun saveWidgetPreference(key: String, value: Any) {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val editor = widgetPrefs.edit()
        when (value) {
            is String -> editor.putString(key + "_$widgetId", value)
            is Boolean -> editor.putBoolean(key + "_$widgetId", value)
            is Int -> editor.putInt(key + "_$widgetId", value)
            is Float -> editor.putFloat(key + "_$widgetId", value)
            is Long -> editor.putLong(key + "_$widgetId", value)
        }
        editor.apply()
    }

    // Update the widget
    private fun updateWidget() {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val appWidgetManager = AppWidgetManager.getInstance(this)
//        TestWidget.updateAppWidget(this, Sf(this), appWidgetManager, TestWidget.SETTINGS_KICK, widgetId)
    }
}