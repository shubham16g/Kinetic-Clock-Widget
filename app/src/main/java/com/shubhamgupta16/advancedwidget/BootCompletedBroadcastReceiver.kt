/*
 * BootCompletedBroadcastReceiver.java
 * - This file is part of the Android app RomanDigital
 *
 * Copyright 2025 David Yockey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.shubhamgupta16.advancedwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider.Companion.MINUTE_TICK
import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider.Companion.SETTINGS_KICK

class BootCompletedBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null && action == Intent.ACTION_BOOT_COMPLETED) {
            val kickstart = Intent(context, TestWidget::class.java)
            kickstart.setAction(SETTINGS_KICK)
            kickstart.setPackage(context.packageName)
            context.sendBroadcast(kickstart)
        }
    }
}
