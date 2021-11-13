package com.shubhamgupta16.advancedwidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import com.shubhamgupta16.advancedwidget.MainActivity.Companion.isServiceRunning
import android.app.PendingIntent
import android.util.Log
import androidx.preference.PreferenceManager


open class TestWidget : AppWidgetProvider() {

    companion object{
        private const val SYNC_CLICKED = "automaticWidgetSyncButtonClick"
        private const val ANIM_DEFAULT = -1
        private const val ANIM_REARRANGE = 1
    }
    private lateinit var draw: Draw

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        if (context?.isServiceRunning(RunnerService::class.java) == true)
            updateAppWidget(context, AppWidgetManager.getInstance(context), ANIM_DEFAULT)
        else
            context?.let { updateRemoteView(null, it, AppWidgetManager.getInstance(context)) }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null) return


        if (intent?.action == "update_my_clock") {
            Log.d("here", "update_my_clock")
            val sf = PreferenceManager.getDefaultSharedPreferences(context)
            if (sf.getBoolean("isAnimating", false)) return
            val data = intent.getIntExtra("data", 0)
            if (data == 1)
                updateAppWidget(context, AppWidgetManager.getInstance(context), ANIM_DEFAULT)
            else
                updateRemoteView(null, context, AppWidgetManager.getInstance(context))
        } else if (intent?.action == SYNC_CLICKED){
            Log.d("here", "anim_rearrange")
            val sf = PreferenceManager.getDefaultSharedPreferences(context)
            if (sf.getBoolean("isAnimating", false)) return
            updateAppWidget(context, AppWidgetManager.getInstance(context), ANIM_REARRANGE)
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        animType: Int
    ) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        Log.d("here2", "here2")
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
        appWidgetManager: AppWidgetManager,
    ) {
        val views = RemoteViews(context.packageName, R.layout.test_widget)
        if (b != null) {
            views.setViewVisibility(R.id.testText, View.GONE)
            views.setViewVisibility(R.id.image, View.VISIBLE)
            views.setOnClickPendingIntent(R.id.image, getPendingSelfIntent(context));
            views.setImageViewBitmap(R.id.image, b)
        } else {
            views.setViewVisibility(R.id.testText, View.VISIBLE)
            views.setViewVisibility(R.id.image, View.GONE)
            views.setTextViewText(R.id.testText, "Open the App to Enable Widget.")
        }
        appWidgetManager.updateAppWidget(
            ComponentName(
                context.packageName,
                TestWidget::class.java.name
            ), views
        )
    }

    private fun getPendingSelfIntent(context: Context?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = SYNC_CLICKED
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }
}