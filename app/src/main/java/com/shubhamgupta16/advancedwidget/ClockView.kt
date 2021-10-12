package com.shubhamgupta16.advancedwidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout

class ClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var hourPin:View
    private var minPin:View
    init {
        val v = LayoutInflater.from(context).inflate(R.layout.view_layout_clock, this, true)
        hourPin = v.findViewById(R.id.hour_pin)
        minPin = v.findViewById(R.id.min_pin)
    }

    fun rotate(hour: Float, min: Float){
        hourPin.rotation = hour
        minPin.rotation = min
    }


}