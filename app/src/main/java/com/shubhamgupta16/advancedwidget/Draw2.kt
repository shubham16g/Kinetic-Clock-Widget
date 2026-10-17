package com.shubhamgupta16.advancedwidget


import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object Draw2 {
    const val ANIM_DURATION = 5000L


    private val dateFormat = SimpleDateFormat("hhmm", Locale.ENGLISH)
    private fun toTime(mills: Long): IntArray {
        val intArr = IntArray(4)
        val stringTime = dateFormat.format(mills)
        for (i in 0..3) {
            intArr[i] = stringTime[i].toString().toInt()
        }
        return intArr
    }

    private fun toTime(calendar: Calendar): IntArray {
        val intArr = IntArray(4)
        val stringTime = dateFormat.format(calendar.time)
        for (i in 0..3) {
            intArr[i] = stringTime[i].toString().toInt()
        }
        return intArr
    }

    private fun loadCircleAsBitmap(context: Context, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.neomorphic_circle2)

        // Create a bitmap with the specified size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)

        // Set the bounds to the specified dimensions and draw
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)

        return bitmap
    }


    fun buildBG(context: Context, sf: Sf, widgetId: Int): Bitmap {
        val size = sf.size(widgetId) // 15
        val padding = sf.padding(widgetId) // inner padding
        val margin = sf.margin(widgetId) // outer margin
        val borderRadius = sf.borderRadius(widgetId)
        val isHorizontal = sf.isHorizontal(widgetId)
        val cols = if (isHorizontal) 16 else 8
        val rows = if (isHorizontal) 6 else 12

        val height = size * rows + padding * 2 + margin * 2
        val width = size * cols + padding * 2 + margin * 2

        val baseBg =
            Bitmap.createBitmap(width.roundToInt(), height.roundToInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBg)

        val rectF = RectF(margin, margin, width - margin, height - margin)

        if (sf.showBackground(widgetId)) {
            val solidPaint = Paint().apply {
                color = sf.bgColor(widgetId)
                alpha = (sf.bgOpacity(widgetId) * 2.55f).roundToInt().coerceIn(0, 255)
                isAntiAlias = true
            }
            canvas.drawRoundRect(rectF, borderRadius, borderRadius, solidPaint)
        }

        if (sf.showClocks(widgetId)) {
            val singleClock = loadCircleAsBitmap(context, size.roundToInt(), size.roundToInt())
            val alphaPaint = Paint().apply {
                alpha = (sf.clocksOpacity(widgetId) * 2.55f).roundToInt()
                isAntiAlias = true
            }

            val clipPath = Path().apply {
                addRoundRect(rectF, borderRadius, borderRadius, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(clipPath)

            for (i in 0 until cols) {
                for (j in 0 until rows) {
                    canvas.drawBitmap(
                        singleClock,
                        i * size + padding + margin,
                        j * size + padding + margin,
                        alphaPaint
                    )
                }
            }
            canvas.restore()
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
            sf.margin(widgetId),
            sf.borderRadius(widgetId),
            sf.color(widgetId)
        )
        val nowArr = toTime(currentTime)
        val isHorizontal = sf.isHorizontal(widgetId)

        if (isReArrange) {
            if (currentSec >= 55) {
                val futureArr = toTime(currentTime.timeInMillis + (1000 * 60))
                mu.drawClocks(
                    getBlock(fraction, nowArr[0], futureArr[0], true),
                    getBlock(fraction, nowArr[1], futureArr[1], true),
                    getBlock(fraction, nowArr[2], futureArr[2], true),
                    getBlock(fraction, nowArr[3], futureArr[3], true),
                    isHorizontal = isHorizontal
                )
            } else {
                mu.drawClocks(
                    getBlock(fraction, nowArr[0], nowArr[0], true),
                    getBlock(fraction, nowArr[1], nowArr[1], true),
                    getBlock(fraction, nowArr[2], nowArr[2], true),
                    getBlock(fraction, nowArr[3], nowArr[3], true),
                    isHorizontal = isHorizontal
                )
            }
        } else {
            val pastTime = toTime(currentTime.timeInMillis - (1000 * 60))
            mu.drawClocks(
                getBlock(fraction, pastTime[0], nowArr[0]),
                getBlock(fraction, pastTime[1], nowArr[1]),
                getBlock(fraction, pastTime[2], nowArr[2]),
                getBlock(fraction, pastTime[3], nowArr[3]),
                isHorizontal = isHorizontal
            )
        }
        return mu.getBitmap()
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
            sf.margin(widgetId),
            sf.borderRadius(widgetId),
            sf.color(widgetId)
        )
        val nowArr = toTime(currentTime)
        mu.drawClocks(
            ClockData.getLargeArray(nowArr[0]),
            ClockData.getLargeArray(nowArr[1]),
            ClockData.getLargeArray(nowArr[2]),
            ClockData.getLargeArray(nowArr[3]),
            isHorizontal = sf.isHorizontal(widgetId)
        )
        return mu.getBitmap()
    }



    private fun getBlock(
        fraction: Float,
        before: Int,
        after: Int,
        isReArrange: Boolean = false
    ): Array<FloatArray> {
        val arr = ClockData.getLargeArray(before).clone()
        if (fraction == 0f) return arr
        if (fraction == 1f) return ClockData.getLargeArray(after)
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