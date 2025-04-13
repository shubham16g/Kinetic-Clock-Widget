package com.shubhamgupta16.advancedwidget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
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


class Draw(private val context: Context, private val widgetId: Int = 0) {

    companion object {
        const val BG_TOP_FACTOR = 13.2f
        const val BG_LEFT_FACTOR = 9.6f
        const val CLOCKS_HEIGHT_FACTOR = 12.35f
        const val CLOCKS_WIDTH_FACTOR = 8.35f
        const val ANIM_DURATION = 5000L
    }

    private val dateFormat = SimpleDateFormat("KKmm", Locale.getDefault())

    private lateinit var va: ValueAnimator
    private var currentSec = 1
    private lateinit var currentTime: IntArray
    private lateinit var futureTime: IntArray
    private val color: Int = context.getSharedPreferences("widget_$widgetId", 0)
        .getInt("pinsColor", Color.parseColor("#000000"))

    private val sf = context.getSharedPreferences("widget_$widgetId", 0)

    private val size: Int =
        context.getSharedPreferences("widget_$widgetId", 0).getInt("size", dpToPx(15).toInt())

    private val stroke: Float =
        context.getSharedPreferences("widget_$widgetId", 0).getFloat("stroke", size / 15f)

    private fun getPreference(key: String, defaultValue: Boolean): Boolean =
        context.getSharedPreferences("widget_$widgetId", 0).getBoolean(key, defaultValue)

    private fun getPreference(key: String, defaultValue: Int): Int =
        context.getSharedPreferences("widget_$widgetId", 0).getInt(key, defaultValue)

    private fun getPreference(key: String, defaultValue: String): String =
        context.getSharedPreferences("widget_$widgetId", 0).getString(key, defaultValue)
            ?: defaultValue

    private fun getPreference(key: String, defaultValue: Long): Long =
        context.getSharedPreferences("widget_$widgetId", 0).getLong(key, defaultValue)

    private lateinit var baseBg: Bitmap

    @SuppressLint("Recycle")
    fun drawAnim(listener: (fraction: Float) -> Unit) {
        val animProgress: Long = getPreference("animProgress", -1L)


        if (animProgress > 0 && animProgress + ANIM_DURATION > System.currentTimeMillis()) {
            return
        }
        if (!::va.isInitialized) {
            va = ValueAnimator.ofFloat(0f, 1f)
            va.duration = ANIM_DURATION
            va.interpolator = AccelerateDecelerateInterpolator()
        }
        va.pause()
        va.removeAllUpdateListeners()
        sf.edit().putLong("animProgress", System.currentTimeMillis()).apply()
        va.addUpdateListener { animation ->
            listener(animation.animatedFraction)
            if (animation.animatedFraction == 1f) {
                va.removeAllUpdateListeners()
                sf.edit().putLong("animProgress", -1).apply()

            }
        }
        va.start()
    }

    fun init(): Boolean {
        Log.d("Draw:", "init")

        buildBG()
        val calendar = Calendar.getInstance()
        currentTime = toTime(calendar.timeInMillis)
        futureTime = toTime(calendar.timeInMillis + (1000 * 60))
        currentSec = calendar.get(Calendar.SECOND)

        if (currentSec <= 1) return true
        return false
    }

    private fun toTime(mills: Long): IntArray {
        return dateFormat.format(mills).toList().map { it.toString().toInt() }.toIntArray()
    }

    private fun buildBG() {
        val height = (size * BG_TOP_FACTOR).toInt()
        val width = (size * BG_LEFT_FACTOR).toInt()

        baseBg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBg)

        if (getPreference("showBackground", true)) {

            when (getPreference("backgroundType", "solid_color")) {
                "wall_blur" -> {
                    if (Utils.haveMangeStoragePermission(context)) {
                        val wallpaperManager = WallpaperManager.getInstance(context)
                        val source = wallpaperManager.drawable?.toBitmap(300, 300) ?: return
                        val bgBitmap = Bitmap.createScaledBitmap(
                            fastBlur16(
                                Bitmap.createBitmap(
                                    source,
                                    source.width / 4,
                                    source.height / 6,
                                    source.width / 2,
                                    source.height / 3
                                ), 14, context
                            ), width, height, false
                        )
                        val alphaPaint = Paint()
                        alphaPaint.alpha = getPreference("bgOpacity", 100) * 5
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
                    solidPaint.color = getPreference("bgColor", Color.WHITE)
                    solidPaint.alpha = getPreference("bgOpacity", 100) * 5
                    canvas.drawRect(
                        0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), solidPaint
                    )
                }
            }
        }

        if (getPreference("showClocks", true)) {
            val clocks = BitmapFactory.decodeResource(context.resources, R.drawable.clock_frames_1)

            val frontBmp = Bitmap.createScaledBitmap(
                clocks,
                (size * CLOCKS_WIDTH_FACTOR).toInt(),
                (size * CLOCKS_HEIGHT_FACTOR).toInt(),
                false
            )
            val alphaPaint = Paint()
            alphaPaint.alpha = getPreference("clocksOpacity", 40) * 5
            canvas.drawBitmap(frontBmp, size * 0.587f, size * 0.49f, alphaPaint)
        }
    }

    fun createTimeClockFrame(fraction: Float, isReArrange: Boolean = false): Bitmap {
        val mu = CanvasMaker(baseBg, stroke, size, color)
        if (isReArrange) if (currentSec >= 55) {
            mu.drawClocks(
                getBlockReArranged(currentTime[0], futureTime[0], fraction),
                getBlockReArranged(currentTime[1], futureTime[1], fraction),
                getBlockReArranged(currentTime[2], futureTime[2], fraction),
                getBlockReArranged(currentTime[3], futureTime[3], fraction),
            )
        } else mu.drawClocks(
            getBlockReArranged(currentTime[0], currentTime[0], fraction),
            getBlockReArranged(currentTime[1], currentTime[1], fraction),
            getBlockReArranged(currentTime[2], currentTime[2], fraction),
            getBlockReArranged(currentTime[3], currentTime[3], fraction),
        )
        else mu.drawClocks(
            getBlockHour1(fraction),
            getBlockHour2(fraction),
            getBlockMin1(fraction),
            getBlockMin2(fraction)
        )
        return mu.getBitmap(dpToPx(8))
    }

    fun createStaticTimeClock(): Bitmap {
        val mu = CanvasMaker(baseBg, stroke, size, color)
        mu.drawClocks(
            ClockData.getLargeArray(currentTime[0]),
            ClockData.getLargeArray(currentTime[1]),
            ClockData.getLargeArray(currentTime[2]),
            ClockData.getLargeArray(currentTime[3]),
        )
        return mu.getBitmap(dpToPx(8))
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

    private fun getBlockMin2(fraction: Float): Array<FloatArray> {
        val arr = if (currentTime[3] == 0) ClockData.getLargeArray(9).clone()
        else ClockData.getLargeArray(currentTime[3] - 1).clone()
        translate(fraction, arr, ClockData.getLargeArray(currentTime[3]))
        return arr
    }

    private fun getBlockReArranged(num: Int, numAfter: Int, fraction: Float): Array<FloatArray> {
        val arr = ClockData.getLargeArray(num).clone()
        translate(fraction, arr, ClockData.getLargeArray(numAfter), true)
        return arr
    }

    private fun getBlockMin1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (currentTime[3] == 0 && currentTime[2] == 0) arr = ClockData.getLargeArray(5).clone()
        else if (currentTime[3] == 0) arr = ClockData.getLargeArray(currentTime[2] - 1).clone()
        else {
            arr = ClockData.getLargeArray(currentTime[2])
            isClone = false
        }
        if (isClone) translate(fraction, arr, ClockData.getLargeArray(currentTime[2]))
        return arr
    }

    private fun getBlockHour2(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (currentTime[0] == 1 && currentTime[1] == 2 && currentTime[2] == 0 && currentTime[2] == 0) arr =
            ClockData.getLargeArray(1).clone()
        else if (currentTime[0] == 0 && currentTime[1] == 1 && currentTime[2] == 0 && currentTime[2] == 0) arr =
            ClockData.getLargeArray(2).clone()
        else {
            arr = ClockData.getLargeArray(currentTime[1])
            isClone = false
        }
        if (isClone) translate(fraction, arr, ClockData.getLargeArray(currentTime[1]))
        return arr
    }

    private fun getBlockHour1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (currentTime[0] == 1 && currentTime[1] == 0 && currentTime[2] == 0 && currentTime[2] == 0) arr =
            ClockData.getLargeArray(0).clone()
        else if (currentTime[0] == 0 && currentTime[1] == 1 && currentTime[2] == 0 && currentTime[2] == 0) arr =
            ClockData.getLargeArray(1).clone()
        else {
            arr = ClockData.getLargeArray(currentTime[0])
            isClone = false
        }
        if (isClone) translate(fraction, arr, ClockData.getLargeArray(currentTime[0]))
        return arr
    }

    private fun dpToPx(dp: Int): Float {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
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