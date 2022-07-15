package com.shubhamgupta16.advancedwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import java.util.*


open class TestWidget : AppWidgetProvider() {

    companion object{
        private const val SYNC_CLICKED = "automaticWidgetSyncButtonClick"
        const val PERMISSION_CLICKED = "automaticWidgetPermissionButtonClick"
        private const val ANIM_DEFAULT = -1
        private const val TAG = "TestWidget"
        private const val ANIM_REARRANGE = 1
    }

    private lateinit var draw: Draw

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        if (context == null) return
        tryToEnableWidget(context)

//        val timeReceiver = TimeReceiver()
//        timeReceiver.init(context)
        updateAppWidget(context, ANIM_DEFAULT)
        Log.d(TAG, "onEnabled")

    }

    override fun onDisabled(context: Context?) {
//        context?.sendBroadcast(Intent(TimeReceiver.STOP_FOREGROUND_ACTION))
        super.onDisabled(context)

    }

    private fun tryToEnableWidget(context: Context) {

        val h = Handler(Looper.getMainLooper())

        fun run(long: Long) {
            val r = {
                Log.d(TAG, "onEnabled: Waah modi ji waah")
                val sec = Calendar.getInstance().get(Calendar.SECOND)
                if (long == 1000L) {
                    if (sec == 0) {
                        updateAppWidget(context, ANIM_DEFAULT)
                        run(60000)
                    } else {
                        run(long)
                    }
                } else {
                    updateAppWidget(context, ANIM_DEFAULT)
                    run((60000 - sec).toLong())
                }

            }
            if (long == 1000L)
                h.removeCallbacks(r)
            h.postDelayed(r, long)
        }

        run(1000)

//        val isServiceRunning = context.isServiceRunning(RunnerService::class.java)
//        if (!isServiceRunning) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    updateRemoteView(null, context, data = -1)
//                    return
//                }
//            }
//            val i = Intent(context, RunnerService::class.java)
//            context.startService(i)
//        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: Called")
        if (context == null) return

        Log.d(TAG, "onReceive: Action = ${intent?.action}")

        if (intent?.action == "update_my_clock") {
            Log.d("here", "update_my_clock")
            val data = intent.getIntExtra("data", 0)
            Log.d(TAG, "onReceive: Data: $data")
            val sf = PreferenceManager.getDefaultSharedPreferences(context)
            if (sf.getBoolean("isAnimating", false)) return
            if (data == 1)
                updateAppWidget(context, ANIM_DEFAULT)
            else{
                tryToEnableWidget(context)
                updateAppWidget(context, ANIM_DEFAULT)
            }
//                updateRemoteView(null, context, AppWidgetManager.getInstance(context), data)
        } else if (intent?.action == SYNC_CLICKED) {
            tryToEnableWidget(context)
            Log.d("here", "anim_rearrange")
            val sf = PreferenceManager.getDefaultSharedPreferences(context)
            if (sf.getBoolean("isAnimating", false)) return
            updateAppWidget(context, ANIM_REARRANGE)
        } else if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            tryToEnableWidget(context)
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun updateAppWidget(
        context: Context,
        animType: Int
    ) {
        Log.d(TAG, "updateAppWidget: ")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        Log.d(TAG, "updateAppWidget: AnimType = $animType")
        if (!::draw.isInitialized)
            draw = Draw(context)
        val isByTime = draw.init()
        if (animType == ANIM_DEFAULT) {
            if (isByTime) {
                draw.drawAnim { fraction ->
                    if (fraction == 0f || fraction == 1f) {
                        editor.putBoolean("isAnimating", fraction == 0f).apply()
                    }
                    updateRemoteView(draw.createTimeClockFrame(fraction), context, appWidgetManager)
                }
            } else {
                updateRemoteView(draw.createStaticTimeClock(), context, appWidgetManager)
            }
        } else{
            Log.d("TAG", "updateAppWidget: Rearr")
            draw.drawAnim { fraction ->
                if (fraction == 0f || fraction == 1f) {
                    editor.putBoolean("isAnimating", fraction == 0f).apply()
                }
                updateRemoteView(draw.createTimeClockFrame(fraction, true), context, appWidgetManager)
            }
        }
    }

    private fun updateRemoteView(
        b: Bitmap?,
        context: Context,
        appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        data: Int = 1
    ) {
        val views = RemoteViews(context.packageName, R.layout.test_widget)
        if (b == null) {
            views.setViewVisibility(R.id.testText, View.VISIBLE)
            views.setViewVisibility(R.id.image, View.GONE)
            val message = when (data) {
                0 -> context.resources.getString(R.string.wait_automatically)
                -1 -> {
                    val intent = Intent(context, MainActivity::class.java)
                    val pi = PendingIntent.getActivity(context, 0, intent, 0)
                    intent.action = PERMISSION_CLICKED
                    views.setOnClickPendingIntent(R.id.testText, pi)
                    "Need Permission! Click to gave permissions."
                }
                else -> ""
            }
            views.setTextViewText(R.id.testText, message)

        } else {
            views.setViewVisibility(R.id.testText, View.GONE)
            views.setViewVisibility(R.id.image, View.VISIBLE)
            views.setOnClickPendingIntent(R.id.image, getPendingSelfIntent(context))
            views.setImageViewBitmap(R.id.image, b)
        }
        appWidgetManager.updateAppWidget(
            ComponentName(
                context.packageName,
                TestWidget::class.java.name
            ), views
        )
    }

    private fun getPendingSelfIntent(
        context: Context?,
        action: String = SYNC_CLICKED
    ): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }
}