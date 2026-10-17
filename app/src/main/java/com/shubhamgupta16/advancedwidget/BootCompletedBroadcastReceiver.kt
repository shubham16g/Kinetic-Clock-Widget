package com.shubhamgupta16.advancedwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shubhamgupta16.advancedwidget.kinetic96.KineticVerticalClockWidget
import com.shubhamgupta16.advancedwidget.kinetic96.KineticHorizontalClockWidget
import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider.Companion.SETTINGS_KICK

class BootCompletedBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null && action == Intent.ACTION_BOOT_COMPLETED) {
            val kickstartVertical = Intent(context, KineticVerticalClockWidget::class.java).apply {
                setAction(SETTINGS_KICK)
                setPackage(context.packageName)
            }
            context.sendBroadcast(kickstartVertical)

            val kickstartHorizontal = Intent(context, KineticHorizontalClockWidget::class.java).apply {
                setAction(SETTINGS_KICK)
                setPackage(context.packageName)
            }
            context.sendBroadcast(kickstartHorizontal)
        }
    }
}
