package com.shubhamgupta16.advancedwidget.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.view.animation.AccelerateDecelerateInterpolator
import com.shubhamgupta16.advancedwidget.ClockAnimation
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
        isReArrange: Boolean,
        animation: ClockAnimation = ClockAnimation.Wave
    ): Bitmap

    val animDuration: Long

    companion object {
        private val animatorMap = mutableMapOf<Int, ValueAnimator>()
        private val animEndTimeMap = mutableMapOf<Int, Long>()
    }


    fun drawAnim(
        widgetId: Int,
        duration: Long = animDuration,
        listener: (fraction: Float, currentSec: Int) -> Unit
    ) {
        synchronized(animatorMap) {
            val existing = animatorMap[widgetId]
            val endTime = animEndTimeMap[widgetId] ?: 0L
            val now = System.currentTimeMillis()

            if (existing != null && (existing.isRunning || now < endTime)) {
                return
            }

            existing?.cancel()
            existing?.removeAllUpdateListeners()

            val va = ValueAnimator.ofFloat(0f, 1f)
            va.duration = duration
            va.interpolator = AccelerateDecelerateInterpolator()

            animatorMap[widgetId] = va
            animEndTimeMap[widgetId] = now + duration

            val calendar = Calendar.getInstance()
            val currentSec = calendar.get(Calendar.SECOND)

            va.addUpdateListener { animation ->
                listener(animation.animatedFraction, currentSec)
            }

            va.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    synchronized(animatorMap) {
                        if (animatorMap[widgetId] == va) {
                            animatorMap.remove(widgetId)
                            animEndTimeMap.remove(widgetId)
                        }
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    synchronized(animatorMap) {
                        if (animatorMap[widgetId] == va) {
                            animatorMap.remove(widgetId)
                            animEndTimeMap.remove(widgetId)
                        }
                    }
                }
            })

            va.start()
        }
    }
}