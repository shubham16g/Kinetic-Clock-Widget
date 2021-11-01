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
import androidx.preference.PreferenceManager
import java.util.*


class Draw(private val context: Context) {

    companion object {
        const val BG_TOP_FACTOR = 13.2f
        const val BG_LEFT_FACTOR = 9.6f
        const val CLOCKS_HEIGHT_FACTOR = 12.35f
        const val CLOCKS_WIDTH_FACTOR = 8.35f
    }

    private lateinit var va: ValueAnimator
    private val sf = PreferenceManager.getDefaultSharedPreferences(context);
    private val irr = IntArray(4)
    private var color: Int = sf.getInt("pinsColor", Color.parseColor("#000000"))

    //    no settings
    private var size: Int = sf.getInt("size", dpToPx(15).toInt())

    //    no settings
    private val stroke: Float = sf.getFloat("stroke", size / 15f)

    private lateinit var baseBg: Bitmap

    @SuppressLint("Recycle")
    fun drawAnim(listener: (fraction: Float) -> Unit) {
        if (!::va.isInitialized) {
            va = ValueAnimator.ofFloat(0f, 1f)
            va.duration = 5000
            va.interpolator = AccelerateDecelerateInterpolator()
        }
        va.pause()
        va.removeAllUpdateListeners()
        va.addUpdateListener { animation ->
            listener(animation.animatedFraction)
            if (animation.animatedFraction == 1f) {
                va.removeAllUpdateListeners()
            }
        }
        va.start()
    }

    fun init(): Boolean {
        Log.d("Draw:", "init")

        buildBG()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val min = calendar.get(Calendar.MINUTE)
        val sec = calendar.get(Calendar.SECOND)
        irr[0] = (hour - hour % 10) / 10
        irr[1] = hour - (irr[0] * 10)
        if (irr[0] == 0 && irr[1] == 0) {
            irr[0] = 1
            irr[1] = 2
        }
        irr[2] = (min - min % 10) / 10
        irr[3] = (min - (irr[2] * 10))
        Log.d("Draw:", "Time: $hour:$min:$sec -> ${irr[0]} ${irr[1]} ${irr[2]} ${irr[3]}")
        if (sec <= 1)
            return true
        return false
    }

    private fun buildBG() {
        val height = (size * BG_TOP_FACTOR).toInt()
        val width = (size * BG_LEFT_FACTOR).toInt()

        baseBg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBg)

        if (sf.getBoolean("showBackground", true)) {

            when (sf.getString("backgroundType", "solid_color")) {
                "wall_blur" -> {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    val source = wallpaperManager.drawable.toBitmap(300, 300)
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
                    alphaPaint.alpha = sf.getInt("bgOpacity", 100) * 5
                    canvas.drawBitmap(bgBitmap, 0f, 0f, alphaPaint)
//                    overlay
                    val overlayPaint = Paint()
                    overlayPaint.color = Color.WHITE
                    overlayPaint.alpha = 8
                    canvas.drawRect(0f, 0f, baseBg.width.toFloat(), baseBg.height.toFloat(), overlayPaint)
                }
                else -> {
                    val solidPaint = Paint()
                    solidPaint.color = sf.getInt("bgColor", Color.WHITE)
                    solidPaint.alpha = sf.getInt("bgOpacity", 100) * 5
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), solidPaint)
                }
            }
        }

        if (sf.getBoolean("showClocks", true)) {
//            multiple clocks in future
            val clocks = BitmapFactory.decodeResource(context.resources, R.drawable.clock_frames_1)

            val frontBmp = Bitmap.createScaledBitmap(
                clocks, (size * CLOCKS_WIDTH_FACTOR).toInt(),
                (size * CLOCKS_HEIGHT_FACTOR).toInt(), false
            )
            val alphaPaint = Paint()
            alphaPaint.alpha = sf.getInt("clocksOpacity", 40) * 5
            canvas.drawBitmap(frontBmp, size * 0.587f, size * 0.49f, alphaPaint)
        }
    }

    fun createTimeClockFrame(fraction: Float, isReArrange: Boolean = false): Bitmap {
        val mu = CanvasMaker(baseBg, stroke, size, color)
        if (isReArrange)
            mu.drawClocks(
                getBlockReArranged(irr[0], fraction),
                getBlockReArranged(irr[1], fraction),
                getBlockReArranged(irr[2], fraction),
                getBlockReArranged(irr[3], fraction),
            )
        else
            mu.drawClocks(
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
            ClockData.getLargeArray(irr[0]),
            ClockData.getLargeArray(irr[1]),
            ClockData.getLargeArray(irr[2]),
            ClockData.getLargeArray(irr[3]),
        )
        return mu.getBitmap(dpToPx(8))
    }

    private fun fastBlur16(source: Bitmap, radius: Int, ctx: Context): Bitmap {
        val bitmap = source.copy(source.config, source.isMutable)
        val rs = RenderScript.create(ctx)
        val input = Allocation.createFromBitmap(
            rs,
            source,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
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
        val arr = if (irr[3] == 0) ClockData.getLargeArray(9).clone()
        else ClockData.getLargeArray(irr[3] - 1).clone()
        translate(fraction, arr, ClockData.getLargeArray(irr[3]))
        return arr
    }

    private fun getBlockReArranged(num: Int, fraction: Float): Array<FloatArray> {
        val arr = ClockData.getLargeArray(num).clone()
        translate(fraction, arr, ClockData.getLargeArray(num), true)
        return arr
    }

    private fun getBlockMin1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[3] == 0 && irr[2] == 0)//__:00
            arr = ClockData.getLargeArray(5).clone()
        else if (irr[3] == 0)//__:_0
            arr = ClockData.getLargeArray(irr[2] - 1).clone()
        else {
            arr = ClockData.getLargeArray(irr[2])
            isClone = false
        }
        if (isClone)
            translate(fraction, arr, ClockData.getLargeArray(irr[2]))
        return arr
    }

    private fun getBlockHour2(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[0] == 1 && irr[1] == 2 && irr[2] == 0 && irr[2] == 0)
            arr = ClockData.getLargeArray(1).clone()
        else if (irr[0] == 0 && irr[1] == 1 && irr[2] == 0 && irr[2] == 0)
            arr = ClockData.getLargeArray(2).clone()
        else {
            arr = ClockData.getLargeArray(irr[1])
            isClone = false
        }
        if (isClone)
            translate(fraction, arr, ClockData.getLargeArray(irr[1]))
        return arr
    }

    private fun getBlockHour1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[0] == 1 && irr[1] == 0 && irr[2] == 0 && irr[2] == 0)
            arr = ClockData.getLargeArray(0).clone()
        else if (irr[0] == 0 && irr[1] == 1 && irr[2] == 0 && irr[2] == 0)
            arr = ClockData.getLargeArray(1).clone()
        else {
            arr = ClockData.getLargeArray(irr[0])
            isClone = false
        }
        if (isClone)
            translate(fraction, arr, ClockData.getLargeArray(irr[0]))
        return arr
    }

    private fun dpToPx(dp: Int): Float {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun translate(
        r: Float, preArr: Array<FloatArray>,
        postArr: Array<FloatArray>,
        isReArrange: Boolean = false
    ): Array<FloatArray> {
        for (i in preArr.indices) {
            val current = preArr[i]
            val after = postArr[i]
            if (isReArrange){
                preArr[i] = floatArrayOf(
                    update(r, current[0], after[0] + 360, true),
                    update(r, current[1] + 360, after[1], false)
                )
            } else {
                preArr[i] = floatArrayOf(
                    update(r, current[0], after[0], true),
                    update(r, current[1], after[1], false)
                )
            }
        }

        return preArr
    }

    private fun update(r: Float, _pre: Float, _post: Float, clockwise: Boolean): Float {
        var pre = _pre;
        var post = _post;
        if (clockwise && pre > post)
            post += 360f;
        else if (!clockwise && post > pre)
            pre += 360f
        return (post - pre) * r + pre
    }
}