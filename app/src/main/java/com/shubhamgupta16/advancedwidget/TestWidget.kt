package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.shubhamgupta16.advancedwidget.MainActivity.Companion.isServiceRunning

class TestWidget : AppWidgetProvider() {

    private lateinit var draw: Draw

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        if (context?.isServiceRunning(RunnerService::class.java) == true)
            updateAppWidget(context, AppWidgetManager.getInstance(context), -1)
        else
            context?.let { updateRemoteView(null, it, AppWidgetManager.getInstance(context)) }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null) return

        if (intent?.action == "update_my_clock") {
            val data = intent.getIntExtra("data", 0)
            if (data == 1)
                updateAppWidget(context, AppWidgetManager.getInstance(context), -1)
            else
                updateRemoteView(null, context, AppWidgetManager.getInstance(context))
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        if (appWidgetId >= 0) return
        if (!::draw.isInitialized)
            draw = Draw(context)
        val isByTime = draw.init()
        if (isByTime) {
            draw.drawAnim { fraction ->
                updateRemoteView(draw.createTimeClockFrame(fraction), context, appWidgetManager)
            }
        } else {
            updateRemoteView(draw.createStaticTimeClock(), context, appWidgetManager)
        }
    }

    private fun updateRemoteView(
        b: Bitmap?,
        context: Context,
        appWidgetManager: AppWidgetManager,
    ) {
        val views = RemoteViews(context.packageName, R.layout.test_widget)
        if (b != null) {
            views.setViewVisibility(R.id.testText, View.GONE)
            views.setViewVisibility(R.id.image, View.VISIBLE)
            views.setImageViewBitmap(R.id.image, b)
        } else {
            views.setViewVisibility(R.id.testText, View.VISIBLE)
            views.setViewVisibility(R.id.image, View.GONE)
            views.setTextViewText(R.id.testText, "Open the App to Enable Widget.")
        }
        appWidgetManager.updateAppWidget(
            ComponentName(
                context.packageName,
                TestWidget::class.java.name
            ), views
        )
    }
}