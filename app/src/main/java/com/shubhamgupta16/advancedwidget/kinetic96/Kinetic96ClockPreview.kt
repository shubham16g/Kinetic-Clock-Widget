package com.shubhamgupta16.advancedwidget.kinetic96

import android.content.Context
import android.graphics.Bitmap
import com.shubhamgupta16.advancedwidget.ClockAnimation
import com.shubhamgupta16.advancedwidget.Draw2
import com.shubhamgupta16.advancedwidget.Sf
import com.shubhamgupta16.advancedwidget.core.AnimatedClockPreview
import java.util.Calendar

class Kinetic96ClockPreview(
    appWidgetId: Int,
    isSettings: Boolean = false,
    listener: (Bitmap) -> Unit

) : AnimatedClockPreview(appWidgetId, isSettings, listener ) {
    private var sf: Sf? = null

    override val animDuration: Long
        get() = Draw2.ANIM_DURATION

    override fun getBackground(context: Context, appWidgetId: Int): Bitmap {
        if (sf == null) sf = Sf(context)
        return Draw2.buildBG(context, sf!!, appWidgetId)
    }

    override fun getClockFrame(
        context: Context,
        appWidgetId: Int,
        background: Bitmap,
        currentTime: Calendar,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean,
        animation: ClockAnimation
    ): Bitmap {
        if (sf == null) sf = Sf(context)
        return Draw2.createTimeClockFrame(
            background,
            sf!!,
            appWidgetId,
            currentTime,
            fraction,
            currentSec,
            isReArrange,
            animation
        )
    }

}