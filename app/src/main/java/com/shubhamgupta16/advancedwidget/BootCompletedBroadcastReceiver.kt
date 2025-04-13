package com.shubhamgupta16.advancedwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider.Companion.SETTINGS_KICK

class BootCompletedBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null && action == Intent.ACTION_BOOT_COMPLETED) {
            val kickstart = Intent(context, KineticClockWidget::class.java)
            kickstart.setAction(SETTINGS_KICK)
            kickstart.setPackage(context.packageName)
            context.sendBroadcast(kickstart)
        }
    }
}
