package com.shubhamgupta16.advancedwidget.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import java.util.Calendar

abstract class ClockWidgetProvider : AppWidgetProvider() {

    enum class UpdateType {
        TIME_TICK,
        SETTINGS,
        USER_CLICK
    }

    abstract fun getClass(): Class<out ClockWidgetProvider>

    private fun onTick(context: Context, action: UpdateType, appWidgetIdsA: IntArray?) {
        var appWidgetIds = appWidgetIdsA
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetName = ComponentName(context.packageName, getClass().name)

        // SETTINGS_KICK may be received without any extra appWidgetIds array;
        // if so, get the array of current appWidgetIds
        if (appWidgetIds == null) {
            appWidgetIds = appWidgetManager.getAppWidgetIds(widgetName)
        }

        for (appWidgetId in appWidgetIds!!) {
            Log.d(TAG, "onTick: appWidgetId = $appWidgetId")

            updateAppWidget(context, appWidgetManager, action, appWidgetId)

            // Set an individual alarm for this specific widget
            val pendingIntent = getPendingIntent(context, intArrayOf(appWidgetId), MINUTE_TICK)
            alarmPendingIntents[appWidgetId] = pendingIntent
            setAlarmForWidget(context, appWidgetId)
        }
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

            val updateType = when (action) {
                SETTINGS_KICK -> UpdateType.SETTINGS
                Intent.ACTION_TIMEZONE_CHANGED -> UpdateType.SETTINGS
                MINUTE_TICK -> UpdateType.TIME_TICK
                WIDGET_CLICK -> UpdateType.USER_CLICK
                else -> UpdateType.TIME_TICK
            }

            // Treating a change in any of exact alarm permission, system time, or system timezone
            // as a minute tick insures immediate update of time display on such changes
            onTick(context, updateType, appWidgetIds)
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
//            val intent = Intent(context, SettingsActivity::class.java).apply {
//                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//                // Add this flag to ensure a new instance is created each time
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            }

            // First update the widget with default settings
            updateAppWidget(context, appWidgetManager, UpdateType.SETTINGS, appWidgetId)

            // Create a specific alarm for this widget
            val pendingIntent = getPendingIntent(context, intArrayOf(appWidgetId), MINUTE_TICK)
            alarmPendingIntents[appWidgetId] = pendingIntent
            setAlarmForWidget(context, appWidgetId)

            // Start the configuration activity
//            context.startActivity(intent)
        }
    }

    override fun onEnabled(context: Context) {
        // This is called when the first widget is added
        // No need to set an alarm here as it will be set in onUpdate
    }

    override fun onDisabled(context: Context) {
        // This is called when the last widget is removed
        // Cancel all alarms
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (pendingIntent in alarmPendingIntents.values) {
            alarmMgr.cancel(pendingIntent)
        }
        alarmPendingIntents.clear()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Called when widgets are deleted
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (appWidgetId in appWidgetIds) {
            alarmPendingIntents[appWidgetId]?.let {
                alarmMgr.cancel(it)
                alarmPendingIntents.remove(appWidgetId)
            }
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int, newOptions: Bundle
    ) {
        updateAppWidget(context, appWidgetManager, UpdateType.SETTINGS, appWidgetId)
    }


    companion object {
        private const val TAG = "Marval"

        // Use a map to store widget-specific pending intents
        private val alarmPendingIntents = HashMap<Int, PendingIntent>()

        const val MINUTE_TICK: String = "com.shubhamgupta16.advancedwidget.MINUTE_TICK"
        const val SETTINGS_KICK: String = "com.shubhamgupta16.advancedwidget.SETTINGS_KICK"
        const val WIDGET_CLICK: String = "com.shubhamgupta16.advancedwidget.WIDGET_CLICK"


        private fun setAlarmForWidget(context: Context, appWidgetId: Int) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MINUTE, 1)
            cal[Calendar.SECOND] = 0
            val t = cal.timeInMillis

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = alarmPendingIntents[appWidgetId] ?: return

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R || alarmMgr.canScheduleExactAlarms()) {
                alarmMgr.setExact(AlarmManager.RTC, t, pendingIntent)
            } else {
                alarmMgr[AlarmManager.RTC, t] = pendingIntent
            }
        }
    }

    abstract fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        updateType: UpdateType,
        appWidgetId: Int
    )

    internal fun getPendingIntent(
        context: Context,
        appWidgetIds: IntArray,
        action: String
    ): PendingIntent {
        val tickIntent = Intent(context, getClass())
        tickIntent.setAction(action)
        tickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)

        // Using the first widget ID as the request code to ensure uniqueness
        // for multiple widget instances
        val requestCode = if (appWidgetIds.isNotEmpty()) appWidgetIds[0] else 0

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            tickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}