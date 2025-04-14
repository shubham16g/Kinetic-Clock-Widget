package com.shubhamgupta16.advancedwidget

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Calendar

class DebugTestActivity : AppCompatActivity() {
    private lateinit var timeButton: Button
    private lateinit var seekBar: SeekBar
    private lateinit var image: ImageView
    private lateinit var switch: MaterialSwitch
    var calender: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_debug_test)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.appbar)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.bottom_controls)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom + 10)
            insets
        }
        setSupportActionBar(findViewById(R.id.toolbar))

        timeButton = findViewById(R.id.debug_time_button)
        seekBar = findViewById(R.id.debug_time_seekbar)
        image = findViewById(R.id.debug_image)
        switch = findViewById(R.id.debug_switch)

        updateImage()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateImage()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        timeButton.setOnClickListener {
            showTimePicker()
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            updateImage()
        }
    }

    fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calender.set(Calendar.HOUR, hourOfDay)
                calender.set(Calendar.MINUTE, minute)
                updateImage()
            },
            calender.get(Calendar.HOUR),
            calender.get(Calendar.MINUTE),
            false
        ).show()
    }

    fun updateImage() {
        timeButton.text =
            String.format("%02d:%02d", calender.get(Calendar.HOUR), calender.get(Calendar.MINUTE))
        image.setImageBitmap(
            Draw2.createTimeClockFrame(
                Draw2.buildBG(this, Sf(this), 0),
                Sf(this), 0,
                calender,
                seekBar.progress / 1000f,
                0,
                isReArrange = switch.isChecked
            )
        )
    }
}