package com.shubhamgupta16.advancedwidget.core

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import com.shubhamgupta16.advancedwidget.Draw2
import com.shubhamgupta16.advancedwidget.R
import java.util.Calendar

abstract class AnimatedClockWidgetProvider : ClockBuilder, ClockWidgetProvider() {

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        updateType: UpdateType,
        appWidgetId: Int
    ) {
        val currentTime = Calendar.getInstance()
        val secondsNow = currentTime.get(Calendar.SECOND)
        val isByTime = secondsNow <= 1

        if (updateType == UpdateType.SETTINGS) {
            Draw2.invalidateBg(appWidgetId)
        }

        val baseBg = getBackground(context, appWidgetId)
        if (updateType == UpdateType.USER_CLICK) {
            val animation = Draw2.getNextAnimation()
            drawAnim(appWidgetId, animation.durationMs) { fraction, currentSec ->
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    updateRemoteView(
                        context,
                        appWidgetId,
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
                )
            }
        } else
            if (isByTime) {
                drawAnim(appWidgetId) { fraction, currentSec ->
                    appWidgetManager.updateAppWidget(
                        appWidgetId,
                        updateRemoteView(
                            context,
                            appWidgetId,
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
                    )
                }
            } else {
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    updateRemoteView(
                        context,
                        appWidgetId,
                        getClockFrame(
                            context,
                            appWidgetId,
                            baseBg,
                            currentTime,
                            0f,
                            secondsNow,
                            true
                        )
                    )
                )
            }
    }

    private fun updateRemoteView(
        context: Context,
        id: Int,
        b: Bitmap?,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.test_widget)
        if (b == null) {
            views.setViewVisibility(R.id.testText, View.VISIBLE)
            views.setViewVisibility(R.id.image, View.GONE)
        } else {
            views.setViewVisibility(R.id.testText, View.GONE)
            views.setViewVisibility(R.id.image, View.VISIBLE)
            views.setOnClickPendingIntent(
                R.id.image,
//                click pending intent
                getPendingIntent(context, intArrayOf(id), WIDGET_CLICK)
            )
            views.setImageViewBitmap(R.id.image, b)
        }
        return views
    }


}