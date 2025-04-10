package com.shubhamgupta16.advancedwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import java.util.Calendar

/** @noinspection SpellCheckingInspection
 */

private const val TAG = "TestWidget"

class TestWidget : AppWidgetProvider() {
    private var draw: Draw? = null

    private fun onTick(context: Context, action: String, appWidgetIds: IntArray?) {
        var appWidgetIds = appWidgetIds
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetName = ComponentName(context.packageName, TestWidget::class.java.name)

        // SETTINGS_KICK may be received without any extra appWidgetIds array;
        // if so, get the array of current appWidgetIds
        if (appWidgetIds == null) {
            appWidgetIds = appWidgetManager.getAppWidgetIds(widgetName)
        }

        for (appWidgetId in appWidgetIds!!) {
            Log.d(TAG, "onTick: appWidgetId = $appWidgetId")
            updateAppWidget(context, appWidgetManager, action, appWidgetId)
        }

        val allAppWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(widgetName)
        alarmPendingIntent = getPendingIntent(context, allAppWidgetIds, MINUTE_TICK)
        setAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        if (action != null &&
            (action == MINUTE_TICK || action == SETTINGS_KICK || action == WIDGET_CLICK || action == Intent.ACTION_TIMEZONE_CHANGED || action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_DATE_CHANGED ||  //
                    // The following AlarmManager intent is only sent when the permission is granted,
                    // not when the permission is revoked, and should only occur in Android 12 or 12L
                    // due to use of USE_EXACT_ALARM permission.
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
                    )
        ) {
            var appWidgetIds: IntArray? = null
            if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)) {
                appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            }

            // Treating a change in any of exact alarm permission, system time, or system timezone
            // as a minute tick insures immediate update of time display on such changes
            onTick(context, action, appWidgetIds)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "onUpdate: appWidgetId = $appWidgetId")
            
            // Launch settings activity for configuration
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(SettingsActivity.EXTRA_WIDGET_ID, appWidgetId)
                // Add this flag to ensure a new instance is created each time
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // First update the widget with default settings
            updateAppWidget(context, appWidgetManager, SETTINGS_KICK, appWidgetId)
            
            // Start the configuration activity
            context.startActivity(intent)
        }
    }

    override fun onEnabled(context: Context) {
        setAlarm(context, Calendar.getInstance().timeInMillis)
    }

    override fun onDisabled(context: Context) {
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager!!.cancel(alarmPendingIntent!!)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int, newOptions: Bundle
    ) {
        Log.d(TAG, "onAppWidgetOptionsChanged: Called")
        if (draw == null) draw = Draw(context)
        draw!!.init()
        val views = updateRemoteView(context, appWidgetId, draw!!.createStaticTimeClock())
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        var alarmManager: AlarmManager? = null
        var alarmPendingIntent: PendingIntent? = null

        const val MINUTE_TICK: String = "com.shubhamgupta16.advancedwidget.MINUTE_TICK"
        const val SETTINGS_KICK: String = "com.shubhamgupta16.advancedwidget.SETTINGS_KICK"
        const val WIDGET_CLICK: String = "com.shubhamgupta16.advancedwidget.WIDGET_CLICK"

        fun configureWidget(context: Context, appWidgetId: Int) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, SETTINGS_KICK, appWidgetId)
        }

        private fun getPendingIntent(
            context: Context,
            appWidgetIds: IntArray,
            action: String
        ): PendingIntent {
            val tickIntent = Intent(context, TestWidget::class.java)
            tickIntent.setAction(action)
            tickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            return PendingIntent.getBroadcast(
                context,
                0,
                tickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun setAlarm(context: Context) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MINUTE, 1)
            cal[Calendar.SECOND] = 0
            val t = cal.timeInMillis
            setAlarm(context, t)
        }

        private fun setAlarm(context: Context, targetTime: Long) {
            if (alarmManager == null) {
                alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R || alarmManager!!.canScheduleExactAlarms()) {
                alarmManager!!.setExact(AlarmManager.RTC, targetTime, alarmPendingIntent!!)
            } else {
                alarmManager!![AlarmManager.RTC, targetTime] = alarmPendingIntent!!
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            action: String,
            appWidgetId: Int
        ) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            Log.d(TAG, "updateAppWidget: ")
            Log.d(TAG, "updateAppWidget: animType")
            val draw = Draw(context)
            val isByTime = draw.init()
            if (action == WIDGET_CLICK) {
                draw.drawAnim { fraction ->
                    if (fraction == 0f || fraction == 1f) {
                        sp.edit().putBoolean("isAnimating", fraction == 0f).apply()
                    }
                    appWidgetManager.updateAppWidget(
                        appWidgetId,
                        updateRemoteView(
                            context,
                            appWidgetId,
                            draw.createTimeClockFrame(fraction, isReArrange = true)
                        )
                    )
                }
            } else
                if (isByTime) {
                    draw.drawAnim { fraction ->
                        if (fraction == 0f || fraction == 1f) {
                            sp.edit().putBoolean("isAnimating", fraction == 0f).apply()
                        }
                        appWidgetManager.updateAppWidget(
                            appWidgetId,
                            updateRemoteView(
                                context,
                                appWidgetId,
                                draw.createTimeClockFrame(fraction)
                            )
                        )
                    }
                } else {
                    appWidgetManager.updateAppWidget(
                        appWidgetId,
                        updateRemoteView(context, appWidgetId, draw.createStaticTimeClock())
                    )
                }
        }


        private fun updateRemoteView(
            context: Context,
            id: Int,
            b: Bitmap?,
        ): RemoteViews {
            Log.d(TAG, "updateRemoteView: Called")
            val views = RemoteViews(context.packageName, R.layout.test_widget)
            if (b == null) {
                views.setViewVisibility(R.id.testText, View.VISIBLE)
                views.setViewVisibility(R.id.image, View.GONE)
            } else {
                views.setViewVisibility(R.id.testText, View.GONE)
                views.setViewVisibility(R.id.image, View.VISIBLE)
                views.setOnClickPendingIntent(
                    R.id.image,
                    getPendingIntent(context, intArrayOf(id), WIDGET_CLICK)
                )
                views.setImageViewBitmap(R.id.image, b)
            }
            return views
        }
    }
}