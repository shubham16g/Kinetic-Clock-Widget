package com.shubhamgupta16.advancedwidget.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import com.shubhamgupta16.advancedwidget.Draw2
import java.util.Calendar

abstract class AnimatedClockPreview(
    private val appWidgetId: Int,
    private val isSettings: Boolean,
    private val listener: (bitmap: Bitmap) -> Unit,
) : ClockBuilder {

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null) return
            if (intent == null) return
            if (Intent.ACTION_TIME_TICK == intent.action) {
                updateBitmap(context, if (isSettings) UpdateType.SETTINGS else UpdateType.TIME_TICK)
            }
        }
    }

    fun register(context: Context) {
        context.registerReceiver(timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(timeTickReceiver)
    }

    fun notifyClick(context: Context) {
        updateBitmap(context, UpdateType.USER_CLICK)
    }

    fun notifySettingsUpdated(context: Context) {
        cancelAnim(appWidgetId)
        Draw2.invalidateBg(appWidgetId)
        updateBitmap(context, UpdateType.SETTINGS)
    }

    private fun updateBitmap(
        context: Context,
        updateType: UpdateType,
    ) {
        val currentTime = Calendar.getInstance()
        val secondsNow = currentTime.get(Calendar.SECOND)
        val isByTime = secondsNow <= 1
        val baseBg = getBackground(context, appWidgetId)
        if (updateType == UpdateType.USER_CLICK) {
            val animation = Draw2.getNextAnimation()
            drawAnim(appWidgetId, animation.durationMs) { fraction, currentSec ->
                listener(
                    getClockFrame(
                        context,
                        appWidgetId,
                        baseBg,
                        currentTime,
                        fraction,
                        currentSec,
                        true,
                        animation
                    )
                )
            }
        } else
            if (isByTime && updateType != UpdateType.SETTINGS) {
                drawAnim(appWidgetId) { fraction, currentSec ->
                    listener(
                        getClockFrame(
                            context,
                            appWidgetId,
                            baseBg,
                            currentTime,
                            fraction,
                            currentSec,
                            false
                        )
                    )
                }
            } else {
                listener(
                    getClockFrame(
                        context,
                        appWidgetId,
                        baseBg,
                        currentTime,
                        1f,
                        secondsNow,
                        false
                    )
                )
            }
    }
}