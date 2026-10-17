package com.shubhamgupta16.advancedwidget.kinetic96

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import com.shubhamgupta16.advancedwidget.Draw2
import com.shubhamgupta16.advancedwidget.Sf
import com.shubhamgupta16.advancedwidget.core.AnimatedClockWidgetProvider
import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider
import com.shubhamgupta16.advancedwidget.core.UpdateType
import java.util.Calendar

class KineticHorizontalClockWidget : AnimatedClockWidgetProvider() {
    private var sf: Sf? = null

    override val animDuration: Long
        get() = Draw2.ANIM_DURATION

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        updateType: UpdateType,
        appWidgetId: Int
    ) {
        if (sf == null) sf = Sf(context)
        sf?.setIsHorizontal(appWidgetId, true)
        super.updateAppWidget(context, appWidgetManager, updateType, appWidgetId)
    }

    override fun getBackground(context: Context, appWidgetId: Int): Bitmap {
        if (sf == null) sf = Sf(context)
        sf?.setIsHorizontal(appWidgetId, true)
        return Draw2.buildBG(context, sf!!, appWidgetId)
    }

    override fun getClockFrame(
        context: Context,
        appWidgetId: Int,
        background: Bitmap,
        currentTime: Calendar,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean
    ): Bitmap {
        if (sf == null) sf = Sf(context)
        sf?.setIsHorizontal(appWidgetId, true)
        return Draw2.createTimeClockFrame(
            background,
            sf!!,
            appWidgetId,
            currentTime,
            fraction,
            currentSec,
            isReArrange
        )
    }

    override fun getWidgetClass(): Class<out ClockWidgetProvider> = KineticHorizontalClockWidget::class.java
}
