package com.shubhamgupta16.advancedwidget.core

import android.animation.ValueAnimator
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RemoteViews
import com.shubhamgupta16.advancedwidget.R
import java.util.Calendar

abstract class AnimatedClockWidgetProvider : ClockWidgetProvider() {

    abstract fun getBackground(context: Context, appWidgetId: Int): Bitmap

    abstract fun getClockFrame(
        context: Context,
        appWidgetId: Int,
        background: Bitmap,
        currentTime: IntArray,
        futureTime: IntArray,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean
    ): Bitmap

    abstract val animDuration: Long

    abstract val currentTime: IntArray

    abstract val futureTime: IntArray

    companion object {
        private val iMap = mutableMapOf<Int, Long>()
    }

    private fun drawAnim(widgetId: Int, listener: (fraction: Float, currentSec: Int) -> Unit) {
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

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        updateType: UpdateType,
        appWidgetId: Int
    ) {
        val calendar = Calendar.getInstance()
        val secondsNow = calendar.get(Calendar.SECOND)
        val isByTime = secondsNow <= 1

        val currentTime = this.currentTime
        val futureTime = this.futureTime

        val baseBg = getBackground(context, appWidgetId)
        if (updateType == UpdateType.USER_CLICK) {
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
                            futureTime,
                            fraction,
                            currentSec,
                            true
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
                                futureTime,
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