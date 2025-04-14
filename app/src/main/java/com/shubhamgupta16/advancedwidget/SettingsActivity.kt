package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.slider.Slider
import com.shubhamgupta16.advancedwidget.core.AnimatedClockPreview
import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider.Companion.SETTINGS_KICK
import com.shubhamgupta16.advancedwidget.kinetic96.Kinetic96ClockPreview
import com.shubhamgupta16.advancedwidget.kinetic96.Kinetic96ClockWidget
import yuku.ambilwarna.AmbilWarnaDialog
import kotlin.math.roundToInt


private const val TAG = "SettingsActivity"

class SettingsActivity : AppCompatActivity() {

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var widgetPrefs: Sf

    // UI elements
    private lateinit var pinRadioGroup: RadioGroup
    private lateinit var bgRadioGroup: RadioGroup
    private lateinit var bgPicker: ImageView
    private lateinit var bgBlur: ImageView
    private lateinit var pinPicker: ImageView
    private lateinit var showBackgroundSwitch: SwitchCompat
    private lateinit var bgOpacitySlider: Slider
    private lateinit var showClocksSwitch: SwitchCompat
    private lateinit var clocksOpacitySeekBar: Slider
    private lateinit var applyButton: Button

    // Current values
    private var bgColor: Int = Color.WHITE
    private var pinColor: Int = Color.BLACK
    private lateinit var image: ImageView
    private lateinit var preview: AnimatedClockPreview


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.appbar)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            findViewById<View>(R.id.btn_save_container)
                .updatePadding(bottom = systemBars.bottom + dpToPx(2).roundToInt())
            insets
        }
        widgetPrefs = Sf(this)

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
        preview = Kinetic96ClockPreview(widgetId, isSettings = true) { bitmap ->
            image.setImageBitmap(bitmap)
        }
        preview.register(this)
        image.setOnClickListener {
            preview.notifyClick(this)
        }

        // Load saved preferences
        loadSavedPreferences()

        // Set up listeners
        setupEventListeners()
        preview.notifySettingsUpdated(this)

        val intent = Intent()
        intent.putExtra(EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, intent)
        sendBroadcast(intent)
    }


    private fun initializeUiElements() {
        // preview image
        image = findViewById(R.id.image)
        // Background settings
        pinRadioGroup = findViewById(R.id.pin_radio_group)
        showBackgroundSwitch = findViewById(R.id.switch_show_background)
        bgRadioGroup = findViewById(R.id.background_radio_group)
        bgOpacitySlider = findViewById(R.id.seekbar_bg_opacity)
        showClocksSwitch = findViewById(R.id.switch_show_clocks)
        clocksOpacitySeekBar = findViewById(R.id.seekbar_clocks_opacity)
        bgPicker = findViewById(R.id.bg_picker)
        bgBlur = findViewById(R.id.bg_blur)
        pinPicker = findViewById(R.id.pin_picker)
        applyButton = findViewById(R.id.btn_save)

//        pinRadioGroup.addView(generateRadioButton(Color.RED))
//        pinRadioGroup.addView(generateRadioButton(Color.BLUE))
//        pinRadioGroup.addView(generateRadioButton(Color.YELLOW))
//        pinRadioGroup.addView(generateRadioButton(Color.GRAY))

//        get resource array
        val colors = resources.getStringArray(R.array.colors_array)
        for (strColor in colors) {
            val color = Color.parseColor(strColor)
            pinRadioGroup.addView(generateRadioButton(color))
            bgRadioGroup.addView(generateRadioButton(color))
        }

    }

    private fun generateRadioButton(color: Int): RadioButton {
        val radioButton = RadioButton(this)
        radioButton.id = View.generateViewId()
        radioButton.tag = color
        radioButton.layoutParams = RadioGroup.LayoutParams(
            dpToPx(60).roundToInt(),
            dpToPx(60).roundToInt(),
        )
        radioButton.background = Utils.radioDrawable(color)
        radioButton.buttonDrawable = StateListDrawable()
        return radioButton
    }


    private fun loadSavedPreferences() {
        // Load background settings
        val showBackground = widgetPrefs.showBackground(widgetId)
        showBackgroundSwitch.isChecked = showBackground

        // Load background color
        bgColor = widgetPrefs.bgColor(widgetId)
        pinColor = widgetPrefs.color(widgetId)
        // Load background opacity
        val bgOpacity = widgetPrefs.bgOpacity(widgetId)
        bgOpacitySlider.value = bgOpacity.toFloat()

        // Load clock settings
        val showClocks = widgetPrefs.showClocks(widgetId)
        showClocksSwitch.isChecked = showClocks
        clocksOpacitySeekBar.isEnabled = showClocks

        // Load clock opacity
        val clocksOpacity = widgetPrefs.clocksOpacity(widgetId)
        clocksOpacitySeekBar.value = clocksOpacity.toFloat()


        val checkedPinRadio = pinRadioGroup.findViewWithTag<RadioButton>(pinColor)
        val checkedBgRadio = bgRadioGroup.findViewWithTag<RadioButton>(bgColor)
        if (checkedPinRadio != null) {
            pinRadioGroup.check(checkedPinRadio.id)
        } else {
            pinPicker.background = Utils.selectedDrawable(pinColor)
        }
        if (checkedBgRadio != null) {
            bgRadioGroup.check(checkedBgRadio.id)
        } else if (bgColor == Sf.BLUR_COLOR) {
            bgBlur.background = Utils.selectedDrawable(bgColor)
        } else {
            bgPicker.background = Utils.selectedDrawable(bgColor)
        }
    }

    private fun setupEventListeners() {
        // Background visibility
        showBackgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            widgetPrefs.setShowBackground(widgetId, isChecked)
            preview.notifySettingsUpdated(this@SettingsActivity)
            updateWidget()
        }

        // Background opacity
        bgOpacitySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                widgetPrefs.setBgOpacity(widgetId, value.roundToInt())
                preview.notifySettingsUpdated(this@SettingsActivity)
                updateWidget()
            }
        }

        // Show clocks
        showClocksSwitch.setOnCheckedChangeListener { _, isChecked ->
            clocksOpacitySeekBar.isEnabled = isChecked
            widgetPrefs.setShowClocks(widgetId, isChecked)
            preview.notifySettingsUpdated(this@SettingsActivity)
            updateWidget()
        }

        // Clocks opacity
        clocksOpacitySeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                widgetPrefs.setClocksOpacity(widgetId, value.roundToInt())
                preview.notifySettingsUpdated(this@SettingsActivity)
                updateWidget()
            }
        }

        pinRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val button = pinRadioGroup.findViewById<RadioButton?>(checkedId)
                ?: return@setOnCheckedChangeListener
            val selectedColor = button.tag as Int
            pinColor = selectedColor
            widgetPrefs.setColor(widgetId, selectedColor)
            pinPicker.background = Utils.unSelectedRadioDrawable(Color.WHITE)
            preview.notifySettingsUpdated(this@SettingsActivity)
            updateWidget()
        }

        bgRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val button = bgRadioGroup.findViewById<RadioButton?>(checkedId)
                ?: return@setOnCheckedChangeListener
            val selectedColor = button.tag as Int
            bgColor = selectedColor
            widgetPrefs.setBgColor(widgetId, selectedColor)
            pinPicker.background = Utils.unSelectedRadioDrawable(Color.WHITE)
            bgBlur.background = Utils.unSelectedRadioDrawable(Color.WHITE)
            preview.notifySettingsUpdated(this@SettingsActivity)
            updateWidget()
        }

        pinPicker.setOnClickListener {
            showColorPicker(pinColor) { color ->
                Log.d(TAG, "COLOR:::::PICKER: color: $color")
                pinRadioGroup.check(View.NO_ID)
                pinColor = color
                widgetPrefs.setColor(widgetId, color)
                preview.notifySettingsUpdated(this@SettingsActivity)
                updateWidget()
                pinPicker.background = Utils.selectedDrawable(color)
            }
        }

        bgPicker.setOnClickListener {
            showColorPicker(bgColor) { color ->
                Log.d(TAG, "COLOR:::::PICKER: color: $color")
                bgRadioGroup.check(View.NO_ID)
                bgColor = color
                widgetPrefs.setBgColor(widgetId, color)
                preview.notifySettingsUpdated(this@SettingsActivity)
                updateWidget()
                bgBlur.background = Utils.unSelectedRadioDrawable(Color.WHITE)
                bgPicker.background = Utils.selectedDrawable(color)
            }
        }

        bgBlur.setOnClickListener {
            val color = Sf.BLUR_COLOR
            bgRadioGroup.check(View.NO_ID)
            bgColor = color
            widgetPrefs.setBgColor(widgetId, color)
            preview.notifySettingsUpdated(this@SettingsActivity)
            updateWidget()
            bgPicker.background = Utils.unSelectedRadioDrawable(Color.WHITE)
            bgBlur.background = Utils.selectedDrawable(color)

        }
    }

    private fun showColorPicker(initialColor: Int, onColorSelected: (Int) -> Unit) {
        AmbilWarnaDialog(this, initialColor, false, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {}

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                onColorSelected(color)
            }
        }).show()
    }

    // Update the widget
    private fun updateWidget() {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val kickstart = Intent(this, Kinetic96ClockWidget::class.java)
        kickstart.setAction(SETTINGS_KICK)
        kickstart.setPackage(packageName)
        sendBroadcast(kickstart)
    }

    override fun onDestroy() {
        preview.unregister(this)
        super.onDestroy()
    }
}