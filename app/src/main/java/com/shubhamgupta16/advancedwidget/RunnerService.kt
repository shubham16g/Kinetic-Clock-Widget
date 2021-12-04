package com.shubhamgupta16.advancedwidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class RunnerService : Service() {

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private lateinit var receiver: BroadcastReceiver

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_TIME_TICK)
                    sendTimeTick(context, 1)
                else if (intent.action == STOP_FOREGROUND_ACTION) {
                    unregisterReceiver(this)
                    stopForeground(true)
                    stopSelfResult(startId)
                }
            }
        }

        sendTimeTick(applicationContext, 1)
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(STOP_FOREGROUND_ACTION)
        registerReceiver(receiver, filter)
        startForeground()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sendTimeTick(applicationContext, 0)
    }

    private fun startForeground() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        val contentView = RemoteViews(packageName, R.layout.custom_notification)
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher)
        contentView.setTextViewText(R.id.title, "${applicationContext.resources.getString(R.string.app_name)} is running in background!")
//        contentView.setTextViewText(R.id.text, "This is a custom layout")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setContent(contentView)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "my_service"
        val channelName = "My Background Service"
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        channel.lightColor = Color.BLUE
        channel.importance = NotificationManager.IMPORTANCE_NONE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }


}