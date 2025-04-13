package com.shubhamgupta16.advancedwidget


import android.content.Context
import android.graphics.Bitmap
import com.shubhamgupta16.advancedwidget.core.AnimatedClockWidgetProvider

abstract class KineticClockWidget : AnimatedClockWidgetProvider() {
    private var sf: Sf? = null

    override fun getBackground(context: Context, appWidgetId: Int): Bitmap {
        if (sf == null) sf = Sf(context)
        return Draw2.buildBG(context, sf!!, appWidgetId)
    }

    override fun getClockFrame(
        context: Context,
        appWidgetId: Int,
        background: Bitmap,
        currentTime: IntArray,
        futureTime: IntArray,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean
    ): Bitmap {
        if (sf == null) sf = Sf(context)
        /*return if (fraction == 0f)
            Draw2.createStaticTimeClock(
                background,
                sf!!,
                appWidgetId,
                currentTime,
            )
        else*/
        return Draw2.createTimeClockFrame(
            background,
            sf!!,
            appWidgetId,
            currentTime,
            futureTime,
            fraction,
            currentSec,
            isReArrange
        )
    }

    override val animDuration: Long
        get() = Draw2.ANIM_DURATION
    override val currentTime: IntArray
        get() = Draw2.currentTime
    override val futureTime: IntArray
        get() = Draw2.futureTime

//    override fun getClass(): Class<out ClockWidgetProvider> = KineticClockWidget::class.java


}