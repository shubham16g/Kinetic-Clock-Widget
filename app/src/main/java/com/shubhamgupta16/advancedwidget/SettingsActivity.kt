package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var layoutRadioGroup: RadioGroup
    private lateinit var radioVertical: RadioButton
    private lateinit var radioHorizontal: RadioButton
    private lateinit var pinRadioGroup: RadioGroup
    private lateinit var bgRadioGroup: RadioGroup
    private lateinit var bgPicker: ImageButton
    private lateinit var pinPicker: ImageButton
    private lateinit var showBackgroundSwitch: CompoundButton
    private lateinit var bgOpacitySlider: Slider
    private lateinit var borderRadiusSlider: Slider
    private lateinit var paddingSlider: Slider
    private lateinit var marginSlider: Slider
    private lateinit var showClocksSwitch: CompoundButton
    private lateinit var clocksOpacitySeekBar: Slider

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
            findViewById<View>(R.id.settings_scroll_view)
                .updatePadding(bottom = systemBars.bottom + dpToPx(16).roundToInt())
            insets
        }
        widgetPrefs = Sf(this)

        // Get widget ID from intent
        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        Log.d(TAG, "onCreate: widgetId: $widgetId")

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        title = if (widgetId != -1) "Widget $widgetId Settings" else "Widget Settings"
        findViewById<View>(R.id.btn_close).setOnClickListener {
            finish()
        }

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
        // Layout settings
        layoutRadioGroup = findViewById(R.id.layout_radio_group)
        radioVertical = findViewById(R.id.radio_layout_vertical)
        radioHorizontal = findViewById(R.id.radio_layout_horizontal)
        // Background settings
        pinRadioGroup = findViewById(R.id.pin_radio_group)
        showBackgroundSwitch = findViewById(R.id.switch_show_background)
        bgRadioGroup = findViewById(R.id.background_radio_group)
        bgOpacitySlider = findViewById(R.id.seekbar_bg_opacity)
        borderRadiusSlider = findViewById(R.id.seekbar_border_radius)
        paddingSlider = findViewById(R.id.seekbar_padding)
        marginSlider = findViewById(R.id.seekbar_margin)
        showClocksSwitch = findViewById(R.id.switch_show_clocks)
        clocksOpacitySeekBar = findViewById(R.id.seekbar_clocks_opacity)
        bgPicker = findViewById(R.id.bg_picker)
        pinPicker = findViewById(R.id.pin_picker)

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
        // Load layout setting
        val isHorizontal = widgetPrefs.isHorizontal(widgetId)
        if (isHorizontal) {
            radioHorizontal.isChecked = true
        } else {
            radioVertical.isChecked = true
        }

        // Load background settings
        val showBackground = widgetPrefs.showBackground(widgetId)
        showBackgroundSwitch.isChecked = showBackground

        // Load background color
        bgColor = widgetPrefs.bgColor(widgetId)
        pinColor = widgetPrefs.color(widgetId)
        // Load background opacity
        val bgOpacity = widgetPrefs.bgOpacity(widgetId)
        bgOpacitySlider.value = bgOpacity.toFloat()

        // Load border radius
        val borderRadiusPx = widgetPrefs.borderRadius(widgetId)
        val borderRadiusDp = pxToDp(borderRadiusPx)
        borderRadiusSlider.value = borderRadiusDp.coerceIn(0f, 40f)

        // Load padding
        val paddingPx = widgetPrefs.padding(widgetId)
        val paddingDp = pxToDp(paddingPx)
        paddingSlider.value = paddingDp.coerceIn(0f, 30f)

        // Load margin
        val marginPx = widgetPrefs.margin(widgetId)
        val marginDp = pxToDp(marginPx)
        marginSlider.value = marginDp.coerceIn(0f, 30f)

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
        } else {
            bgPicker.background = Utils.selectedDrawable(bgColor)
        }
    }

    private fun setupEventListeners() {
        // Layout selection
        layoutRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val isHorizontal = checkedId == R.id.radio_layout_horizontal
            widgetPrefs.setIsHorizontal(widgetId, isHorizontal)
            preview.notifySettingsUpdated(this@SettingsActivity)
            updateWidget()
        }

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

        // Border Radius
        borderRadiusSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                widgetPrefs.setBorderRadius(widgetId, dpToPx(value.roundToInt()))
                preview.notifySettingsUpdated(this@SettingsActivity)
                updateWidget()
            }
        }

        // Padding
        paddingSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                widgetPrefs.setPadding(widgetId, dpToPx(value.roundToInt()))
                preview.notifySettingsUpdated(this@SettingsActivity)
                updateWidget()
            }
        }

        // Margin
        marginSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                widgetPrefs.setMargin(widgetId, dpToPx(value.roundToInt()))
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
                bgPicker.background = Utils.selectedDrawable(color)
            }
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
