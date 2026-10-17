package com.shubhamgupta16.advancedwidget


import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object Draw2 {
    const val EACH_CLOCK_DURATION = 3500L
    const val WAVE_DELAY_DURATION = 1000L
    const val ANIM_DURATION = EACH_CLOCK_DURATION + WAVE_DELAY_DURATION


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

    private val bitmapCache = HashMap<Int, Bitmap>()

    private fun getFrameBitmap(widgetId: Int, width: Int, height: Int): Bitmap {
        val existing = bitmapCache[widgetId]
        if (existing != null && !existing.isRecycled && existing.width == width && existing.height == height) {
            return existing
        }
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCache[widgetId] = newBitmap
        return newBitmap
    }

    fun createTimeClockFrame(
        baseBg: Bitmap,
        sf: Sf,
        widgetId: Int,
        currentTime: Calendar,
        fraction: Float,
        currentSec: Int,
        isReArrange: Boolean = false,
        eachClockDuration: Long = EACH_CLOCK_DURATION,
        waveDelayDuration: Long = WAVE_DELAY_DURATION
    ): Bitmap {
        val targetBitmap = getFrameBitmap(widgetId, baseBg.width, baseBg.height)
        val mu = CanvasMaker(
            baseBg,
            targetBitmap,
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
                    getBlock(fraction, nowArr[0], futureArr[0], true, 0, isHorizontal, eachClockDuration, waveDelayDuration),
                    getBlock(fraction, nowArr[1], futureArr[1], true, 1, isHorizontal, eachClockDuration, waveDelayDuration),
                    getBlock(fraction, nowArr[2], futureArr[2], true, 2, isHorizontal, eachClockDuration, waveDelayDuration),
                    getBlock(fraction, nowArr[3], futureArr[3], true, 3, isHorizontal, eachClockDuration, waveDelayDuration),
                    isHorizontal = isHorizontal
                )
            } else {
                mu.drawClocks(
                    getBlock(fraction, nowArr[0], nowArr[0], true, 0, isHorizontal, eachClockDuration, waveDelayDuration),
                    getBlock(fraction, nowArr[1], nowArr[1], true, 1, isHorizontal, eachClockDuration, waveDelayDuration),
                    getBlock(fraction, nowArr[2], nowArr[2], true, 2, isHorizontal, eachClockDuration, waveDelayDuration),
                    getBlock(fraction, nowArr[3], nowArr[3], true, 3, isHorizontal, eachClockDuration, waveDelayDuration),
                    isHorizontal = isHorizontal
                )
            }
        } else {
            val pastTime = toTime(currentTime.timeInMillis - (1000 * 60))
            mu.drawClocks(
                getBlock(fraction, pastTime[0], nowArr[0], false, 0, isHorizontal, eachClockDuration, waveDelayDuration),
                getBlock(fraction, pastTime[1], nowArr[1], false, 1, isHorizontal, eachClockDuration, waveDelayDuration),
                getBlock(fraction, pastTime[2], nowArr[2], false, 2, isHorizontal, eachClockDuration, waveDelayDuration),
                getBlock(fraction, pastTime[3], nowArr[3], false, 3, isHorizontal, eachClockDuration, waveDelayDuration),
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
        val targetBitmap = getFrameBitmap(widgetId, baseBg.width, baseBg.height)
        val mu = CanvasMaker(
            baseBg,
            targetBitmap,
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
        isReArrange: Boolean = false,
        blockIndex: Int = 0,
        isHorizontal: Boolean = true,
        eachClockDuration: Long = EACH_CLOCK_DURATION,
        waveDelayDuration: Long = WAVE_DELAY_DURATION
    ): Array<FloatArray> {
        val beforeArr = ClockData.getLargeArray(before)
        if (fraction == 0f) return beforeArr
        val afterArr = ClockData.getLargeArray(after)
        if (fraction == 1f) return afterArr

        val resultArr = Array(24) { FloatArray(2) }
        translate(
            fraction,
            beforeArr,
            afterArr,
            resultArr,
            isReArrange,
            blockIndex,
            isHorizontal,
            eachClockDuration,
            waveDelayDuration
        )
        return resultArr
    }

    private fun translate(
        r: Float,
        preArr: Array<FloatArray>,
        postArr: Array<FloatArray>,
        outArr: Array<FloatArray>,
        isReArrange: Boolean = false,
        blockIndex: Int = 0,
        isHorizontal: Boolean = true,
        eachClockDuration: Long = EACH_CLOCK_DURATION,
        waveDelayDuration: Long = WAVE_DELAY_DURATION
    ) {
        val maxCol = if (isHorizontal) 15 else 7
        val maxRow = if (isHorizontal) 5 else 11
        val dMax = (maxCol + maxRow).toFloat()
        val totalDuration = (eachClockDuration + waveDelayDuration).toFloat()
        val currentMs = r * totalDuration
        val clockDurationFloat = eachClockDuration.coerceAtLeast(1L).toFloat()

        for (i in preArr.indices) {
            val current = preArr[i]
            val after = postArr[i]

            val clockProgress = if (isReArrange) {
                val col = if (isHorizontal) {
                    blockIndex * 4 + (i % 4)
                } else {
                    (blockIndex % 2) * 4 + (i % 4)
                }
                val row = if (isHorizontal) {
                    i / 4
                } else {
                    (blockIndex / 2) * 6 + (i / 4)
                }
                val normalizedDist = (col + row) / dMax
                val startMs = normalizedDist * waveDelayDuration
                val endMs = startMs + eachClockDuration
                val linearProgress = when {
                    currentMs <= startMs -> 0f
                    currentMs >= endMs -> 1f
                    else -> (currentMs - startMs) / clockDurationFloat
                }
                // SmoothStep easing for fluid hand rotation
                linearProgress * linearProgress * (3f - 2f * linearProgress)
            } else {
                r
            }

            if (isReArrange) {
                outArr[i][0] = update(clockProgress, current[0], after[0] + 360f, true)
                outArr[i][1] = update(clockProgress, current[1] + 360f, after[1], false)
            } else {
                outArr[i][0] = update(clockProgress, current[0], after[0], true)
                outArr[i][1] = update(clockProgress, current[1], after[1], false)
            }
        }
    }

    private fun update(r: Float, _pre: Float, _post: Float, clockwise: Boolean): Float {
        var pre = _pre
        var post = _post
        if (clockwise && pre > post) post += 360f
        else if (!clockwise && post > pre) pre += 360f
        return (post - pre) * r + pre
    }
}