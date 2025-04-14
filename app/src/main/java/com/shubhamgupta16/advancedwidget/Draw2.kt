package com.shubhamgupta16.advancedwidget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class Sf(private val context: Context) {

    private val sf = context.getSharedPreferences("widget_x", 0)

    fun size(widgetId: Int): Float = sf.getFloat("size_$widgetId", dpToPx(30))

    fun padding(widgetId: Int): Float =
        (size(widgetId) / dpToPx(15)) * sf.getFloat("padding_$widgetId", dpToPx(8))

    fun stroke(widgetId: Int): Float = sf.getFloat("stroke_$widgetId", size(widgetId) / 15f)

    fun color(widgetId: Int): Int = sf.getInt("pinsColor_$widgetId", Color.BLACK)

    fun showBackground(widgetId: Int): Boolean = sf.getBoolean("showBackground_$widgetId", true)

    fun backgroundType(widgetId: Int): String =
        sf.getString("backgroundType_$widgetId", "solid_color") ?: "solid_color"

    fun bgOpacity(widgetId: Int): Int = sf.getInt("bgOpacity_$widgetId", 100)

    fun bgColor(widgetId: Int): Int = sf.getInt("bgColor_$widgetId", Color.WHITE)

    fun showClocks(widgetId: Int): Boolean = sf.getBoolean("showClocks_$widgetId", true)

    fun clocksOpacity(widgetId: Int): Int = sf.getInt("clocksOpacity_$widgetId", 40)


    fun getAnimProgress(widgetId: Int): Long {
        return sf.getLong("animProgress_$widgetId", -1L)
    }

    fun setAnimProgress(widgetId: Int, progress: Long) {
        sf.edit().putLong("animProgress_$widgetId", progress).apply()
    }


}

private fun dpToPx(dp: Int): Float {
    val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
    return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
}

object Draw2 {
    const val ANIM_DURATION = 5000L


    private val dateFormat = SimpleDateFormat("hhmm", Locale.ENGLISH)
    private fun toTime(mills: Long): IntArray {
        return dateFormat.format(mills).toList().map { it.toString().toInt() }.toIntArray()
    }

    private fun toTime(calendar: Calendar): IntArray {
        return dateFormat.format(calendar.time).toList().map { it.toString().toInt() }.toIntArray()
    }


    fun buildBG(context: Context, sf: Sf, widgetId: Int): Bitmap {
        val size = sf.size(widgetId) // 15
        val padding = sf.padding(widgetId) // 15
        val height = size * 12 + padding * 2
        val width = size * 8 + padding * 2

        val baseBg =
            Bitmap.createBitmap(width.roundToInt(), height.roundToInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBg)

        if (sf.showBackground(widgetId)) {

            when (sf.backgroundType(widgetId)) {
                "wall_blur" -> {
                    if (Utils.haveMangeStoragePermission(context)) {
                        val wallpaperManager = WallpaperManager.getInstance(context)
                        val source = wallpaperManager.drawable?.toBitmap(300, 300) ?: return baseBg
                        val bgBitmap = Bitmap.createScaledBitmap(
                            fastBlur16(
                                Bitmap.createBitmap(
                                    source,
                                    source.width / 4,
                                    source.height / 6,
                                    source.width / 2,
                                    source.height / 3
                                ), 14, context
                            ), width.roundToInt(), height.roundToInt(), false
                        )
                        val alphaPaint = Paint()
                        alphaPaint.alpha = sf.bgOpacity(widgetId) * 5
                        canvas.drawBitmap(bgBitmap, 0f, 0f, alphaPaint)
                        val overlayPaint = Paint()
                        overlayPaint.color = Color.WHITE
                        overlayPaint.alpha = 8
                        canvas.drawRect(
                            0f, 0f, baseBg.width.toFloat(), baseBg.height.toFloat(), overlayPaint
                        )
                    }
                }

                else -> {
                    val solidPaint = Paint()
                    solidPaint.color = sf.bgColor(widgetId)
                    solidPaint.alpha = sf.bgOpacity(widgetId) * 5
                    canvas.drawRect(
                        0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), solidPaint
                    )
                }
            }
        }

        if (sf.showClocks(widgetId)) {
            val clocks = BitmapFactory.decodeResource(context.resources, R.drawable.clock_frames_1)

            val frontBmp = Bitmap.createScaledBitmap(
                clocks,
                (size * 8).roundToInt(),
                (size * 12).roundToInt(),
                false
            )
            val alphaPaint = Paint()
            alphaPaint.alpha = sf.clocksOpacity(widgetId) * 5
            canvas.drawBitmap(frontBmp, padding, padding, alphaPaint)
        }
        return baseBg
    }

    fun createTimeClockFrame(
        baseBg: Bitmap,
        sf: Sf,
        widgetId: Int,
        currentTime: Calendar,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean = false
    ): Bitmap {
        val mu = CanvasMaker(
            baseBg,
            sf.stroke(widgetId),
            sf.size(widgetId),
            sf.padding(widgetId),
            sf.color(widgetId)
        )
        val nowArr = toTime(currentTime)

        if (isReArrange) {
            if (currentSec >= 55) {
                val futureArr = toTime(currentTime.timeInMillis + (1000 * 60))
                mu.drawClocks(
                    getBlock(fraction, nowArr[0], futureArr[0], true),
                    getBlock(fraction, nowArr[1], futureArr[1], true),
                    getBlock(fraction, nowArr[2], futureArr[2], true),
                    getBlock(fraction, nowArr[3], futureArr[3], true),
                )
            } else {
                mu.drawClocks(
                    getBlock(fraction, nowArr[0], nowArr[0], true),
                    getBlock(fraction, nowArr[1], nowArr[1], true),
                    getBlock(fraction, nowArr[2], nowArr[2], true),
                    getBlock(fraction, nowArr[3], nowArr[3], true),
                )
            }
        } else {
            val pastTime = toTime(currentTime.timeInMillis - (1000 * 60))
            mu.drawClocks(
                getBlock(fraction, pastTime[0], nowArr[0]),
                getBlock(fraction, pastTime[1], nowArr[1]),
                getBlock(fraction, pastTime[2], nowArr[2]),
                getBlock(fraction, pastTime[3], nowArr[3]),
            )
        }
        return mu.getBitmap(dpToPx(18))
    }

    fun createStaticTimeClock(
        baseBg: Bitmap,
        sf: Sf,
        widgetId: Int,
        currentTime: Calendar
    ): Bitmap {
        val mu = CanvasMaker(
            baseBg,
            sf.stroke(widgetId),
            sf.size(widgetId),
            sf.padding(widgetId),
            sf.color(widgetId)
        )
        val nowArr = toTime(currentTime)
        mu.drawClocks(
            ClockData.getLargeArray(nowArr[0]),
            ClockData.getLargeArray(nowArr[1]),
            ClockData.getLargeArray(nowArr[2]),
            ClockData.getLargeArray(nowArr[3]),
        )
        return mu.getBitmap(dpToPx(18))
    }

    private fun fastBlur16(source: Bitmap, radius: Int, ctx: Context): Bitmap {
        val bitmap = source.copy(source.config!!, source.isMutable)
        val rs = RenderScript.create(ctx)
        val input = Allocation.createFromBitmap(
            rs, source, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT
        )
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius.toFloat())
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)
        return bitmap
    }

    private fun getBlock(
        fraction: Float,
        before: Int,
        after: Int,
        isReArrange: Boolean = false
    ): Array<FloatArray> {
        val arr = ClockData.getLargeArray(before).clone()
        translate(fraction, arr, ClockData.getLargeArray(after), isReArrange)
        return arr
    }

    private fun translate(
        r: Float,
        preArr: Array<FloatArray>,
        postArr: Array<FloatArray>,
        isReArrange: Boolean = false
    ): Array<FloatArray> {
        for (i in preArr.indices) {
            val current = preArr[i]
            val after = postArr[i]
            if (isReArrange) {
                preArr[i] = floatArrayOf(
                    update(r, current[0], after[0] + 360, true),
                    update(r, current[1] + 360, after[1], false)
                )
            } else {
                preArr[i] = floatArrayOf(
                    update(r, current[0], after[0], true), update(r, current[1], after[1], false)
                )
            }
        }

        return preArr
    }

    private fun update(r: Float, _pre: Float, _post: Float, clockwise: Boolean): Float {
        var pre = _pre
        var post = _post
        if (clockwise && pre > post) post += 360f
        else if (!clockwise && post > pre) pre += 360f
        return (post - pre) * r + pre
    }
}