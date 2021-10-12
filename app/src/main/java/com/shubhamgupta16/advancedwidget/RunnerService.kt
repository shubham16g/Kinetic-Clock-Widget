package com.shubhamgupta16.advancedwidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*
import android.widget.RemoteViews
import android.content.BroadcastReceiver

import android.content.IntentFilter




class RunnerService : Service() {

    companion object {
        const val START_FOREGROUND_ACTION = "START_RRR"
        const val STOP_FOREGROUND_ACTION = "STOP_RRR"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("tag,start", "${intent?.action}")
        if (intent?.action.equals(START_FOREGROUND_ACTION)) {

            val filter = IntentFilter()
            filter.addAction("SOME_ACTION")
            filter.addAction("SOME_OTHER_ACTION")
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val i = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    sendBroadcast(i)
                }
            }
            registerReceiver(receiver, filter)

            /*val handler = Handler(Looper.getMainLooper())
            val timer = Timer()
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    handler.post {
                        val i = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        sendBroadcast(i)
                    }
                }
            }
            timer.scheduleAtFixedRate(task, 0, 60000)*/

            startForeground()
        }
        else if (intent?.action.equals(STOP_FOREGROUND_ACTION)) {
            stopForeground(true)
            stopSelfResult(startId)
        }
        return START_NOT_STICKY
    }

    private fun startForeground() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        val contentView = RemoteViews(packageName, R.layout.custom_notification)
//        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher)
        contentView.setTextViewText(R.id.title, "Multi Clock Widget is running in background!")
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
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}