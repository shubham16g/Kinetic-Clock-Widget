package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.RemoteViews

class TestWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        val i = Intent(context, RunnerService::class.java)
        i.action = RunnerService.START_FOREGROUND_ACTION
        context.startService(i)
    }

    override fun onDisabled(context: Context) {
        val i = Intent(context, RunnerService::class.java)
        i.action = RunnerService.STOP_FOREGROUND_ACTION
        context.startService(i)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context != null)
            if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
                updateAppWidget(context, AppWidgetManager.getInstance(context), -1)
            }

    }

    private lateinit var draw: Draw

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        if (!::draw.isInitialized) {
            draw = Draw()
        }
        draw.init()
        draw.draw { fraction ->
            if (appWidgetId < 0) {
                var b: Bitmap? = draw.createTimeClock(context, fraction)
                val views = RemoteViews(context.packageName, R.layout.test_widget)
                views.setImageViewBitmap(R.id.image, b)
//                appWidgetManager.updateAppWidget(appWidgetId, views)
                appWidgetManager.updateAppWidget(
                    ComponentName(
                        context.packageName,
                        TestWidget::class.java.name
                    ), views
                )
                b = null
            }
        }


    }


}