package com.shubhamgupta16.advancedwidget.core

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.Calendar

interface ClockBuilder {

    fun getBackground(context: Context, appWidgetId: Int): Bitmap

    fun getClockFrame(
        context: Context,
        appWidgetId: Int,
        background: Bitmap,
        currentTime: Calendar,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean
    ): Bitmap

    val animDuration: Long

    companion object {
        private val iMap = mutableMapOf<Int, Long>()
    }


    fun drawAnim(widgetId: Int, listener: (fraction: Float, currentSec: Int) -> Unit) {
        val animProgress: Long = iMap[widgetId] ?: -1

        val animDuration = this.animDuration

        if (animProgress > 0 && animProgress + animDuration > System.currentTimeMillis()) {
            return
        }

        val va = ValueAnimator.ofFloat(0f, 1f)
        va.duration = animDuration
        va.interpolator = AccelerateDecelerateInterpolator()

        va.pause()
        va.removeAllUpdateListeners()
        iMap[widgetId] = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentSec = calendar.get(Calendar.SECOND)
        va.addUpdateListener { animation ->
            listener(animation.animatedFraction, currentSec)
            if (animation.animatedFraction == 1f) {
                va.removeAllUpdateListeners()
                iMap.remove(widgetId)
            }
        }
        va.start()
    }
}