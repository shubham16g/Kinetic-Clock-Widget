package com.shubhamgupta16.advancedwidget

import android.appwidget.AppWidgetManager
import android.content.*

class TimeReceiver : BroadcastReceiver() {

    companion object {
        const val STOP_FOREGROUND_ACTION = "STOP_RRR"

        fun sendTimeTick(context: Context, data: Int) {
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(
                    context,
                    TestWidget::class.java
                )
            )
            if (ids.isNotEmpty()) {
                val i = Intent(context, TestWidget::class.java).setAction("update_my_clock")
                i.putExtra("data", data)
                context.sendBroadcast(i)
            }
        }
    }

    fun init(context: Context) {
        sendTimeTick(context, 1)
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(STOP_FOREGROUND_ACTION)
        context.applicationContext.registerReceiver(this, filter)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent == null || context == null) return
        if (intent.action == Intent.ACTION_TIME_TICK)
            sendTimeTick(context, 1)
        else if (intent.action == STOP_FOREGROUND_ACTION) {
            sendTimeTick(context, 0)
            context.unregisterReceiver(this)
        }
    }
}