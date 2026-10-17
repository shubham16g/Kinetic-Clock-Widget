package com.shubhamgupta16.advancedwidget

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

interface ClockAnimation {
    val name: String
    val durationMs: Long

    fun getClockProgress(
        fraction: Float,
        col: Int,
        row: Int,
        maxCol: Int,
        maxRow: Int
    ): Float

    fun getClockPins(
        fraction: Float,
        currentPin1: Float,
        currentPin2: Float,
        afterPin1: Float,
        afterPin2: Float,
        col: Int,
        row: Int,
        maxCol: Int,
        maxRow: Int
    ): FloatArray {
        val p = getClockProgress(fraction, col, row, maxCol, maxRow)
        return floatArrayOf(
            update(p, currentPin1, afterPin1 + 360f, true),
            update(p, currentPin2 + 360f, afterPin2, false)
        )
    }

    companion object {
        fun update(r: Float, _pre: Float, _post: Float, clockwise: Boolean): Float {
            var pre = _pre
            var post = _post
            if (clockwise && pre > post) post += 360f
            else if (!clockwise && post > pre) pre += 360f
            return (post - pre) * r + pre
        }
    }

    object Wave : ClockAnimation {
        override val name: String = "Wave"
        const val EACH_CLOCK_DURATION = 3500L
        const val WAVE_DELAY_DURATION = 1000L
        override val durationMs: Long = EACH_CLOCK_DURATION + WAVE_DELAY_DURATION

        override fun getClockProgress(
            fraction: Float,
            col: Int,
            row: Int,
            maxCol: Int,
            maxRow: Int
        ): Float {
            val dMax = (maxCol + maxRow).toFloat()
            val totalDuration = durationMs.toFloat()
            val currentMs = fraction * totalDuration
            val clockDurationFloat = EACH_CLOCK_DURATION.toFloat()
            val normalizedDist = (col + row) / dMax
            val startMs = normalizedDist * WAVE_DELAY_DURATION
            val endMs = startMs + EACH_CLOCK_DURATION
            val linearProgress = when {
                currentMs <= startMs -> 0f
                currentMs >= endMs -> 1f
                else -> (currentMs - startMs) / clockDurationFloat
            }
            return linearProgress * linearProgress * (3f - 2f * linearProgress)
        }
    }

    object Simultaneous : ClockAnimation {
        override val name: String = "Simultaneous"
        override val durationMs: Long = 4000L

        override fun getClockProgress(
            fraction: Float,
            col: Int,
            row: Int,
            maxCol: Int,
            maxRow: Int
        ): Float {
            val linearProgress = fraction.coerceIn(0f, 1f)
            return linearProgress * linearProgress * (3f - 2f * linearProgress)
        }
    }

    object DirectionalWave : ClockAnimation {
        override val name: String = "Directional Wave"
        const val EACH_PHASE_CLOCK_DURATION = 1800L
        const val EACH_PHASE_WAVE_DELAY = 700L
        const val EACH_PHASE_TOTAL = EACH_PHASE_CLOCK_DURATION + EACH_PHASE_WAVE_DELAY
        override val durationMs: Long = EACH_PHASE_TOTAL * 2L
        private const val TARGET_ANGLE = 225f

        override fun getClockProgress(
            fraction: Float,
            col: Int,
            row: Int,
            maxCol: Int,
            maxRow: Int
        ): Float {
            val dMax = (maxCol + maxRow).toFloat()
            val normalizedDist = (col + row) / dMax
            val phaseFraction = if (fraction < 0.5f) fraction * 2f else (fraction - 0.5f) * 2f
            val currentMs = phaseFraction * EACH_PHASE_TOTAL.toFloat()
            val startMs = normalizedDist * EACH_PHASE_WAVE_DELAY.toFloat()
            val endMs = startMs + EACH_PHASE_CLOCK_DURATION.toFloat()
            val linearProgress = when {
                currentMs <= startMs -> 0f
                currentMs >= endMs -> 1f
                else -> (currentMs - startMs) / EACH_PHASE_CLOCK_DURATION.toFloat()
            }
            return linearProgress * linearProgress * (3f - 2f * linearProgress)
        }

        override fun getClockPins(
            fraction: Float,
            currentPin1: Float,
            currentPin2: Float,
            afterPin1: Float,
            afterPin2: Float,
            col: Int,
            row: Int,
            maxCol: Int,
            maxRow: Int
        ): FloatArray {
            val progress = getClockProgress(fraction, col, row, maxCol, maxRow)

            val endPhase1Hand1 = update(1f, currentPin1, TARGET_ANGLE, true)
            val endPhase1Hand2 = update(1f, currentPin2, TARGET_ANGLE, false)

            return if (fraction < 0.5f) {
                floatArrayOf(
                    update(progress, currentPin1, TARGET_ANGLE, true),
                    update(progress, currentPin2, TARGET_ANGLE, false)
                )
            } else {
                floatArrayOf(
                    update(progress, endPhase1Hand1, afterPin1, true),
                    update(progress, endPhase1Hand2, afterPin2, false)
                )
            }
        }
    }
}

object Draw2 {
    const val EACH_CLOCK_DURATION = ClockAnimation.Wave.EACH_CLOCK_DURATION
    const val WAVE_DELAY_DURATION = ClockAnimation.Wave.WAVE_DELAY_DURATION
    const val ANIM_DURATION = ClockAnimation.Wave.EACH_CLOCK_DURATION + ClockAnimation.Wave.WAVE_DELAY_DURATION

    private val animationsList = mutableListOf<ClockAnimation>(
        ClockAnimation.Wave,
        ClockAnimation.Simultaneous,
        ClockAnimation.DirectionalWave
    )

    val animations: List<ClockAnimation>
        get() = animationsList.toList()

    fun registerAnimation(animation: ClockAnimation) {
        if (!animationsList.contains(animation)) {
            animationsList.add(animation)
        }
    }

    fun getRandomAnimation(): ClockAnimation {
        return animationsList.random()
    }

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
        animation: ClockAnimation = ClockAnimation.Wave
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
                    getBlock(fraction, nowArr[0], futureArr[0], true, 0, isHorizontal, animation),
                    getBlock(fraction, nowArr[1], futureArr[1], true, 1, isHorizontal, animation),
                    getBlock(fraction, nowArr[2], futureArr[2], true, 2, isHorizontal, animation),
                    getBlock(fraction, nowArr[3], futureArr[3], true, 3, isHorizontal, animation),
                    isHorizontal = isHorizontal
                )
            } else {
                mu.drawClocks(
                    getBlock(fraction, nowArr[0], nowArr[0], true, 0, isHorizontal, animation),
                    getBlock(fraction, nowArr[1], nowArr[1], true, 1, isHorizontal, animation),
                    getBlock(fraction, nowArr[2], nowArr[2], true, 2, isHorizontal, animation),
                    getBlock(fraction, nowArr[3], nowArr[3], true, 3, isHorizontal, animation),
                    isHorizontal = isHorizontal
                )
            }
        } else {
            val pastTime = toTime(currentTime.timeInMillis - (1000 * 60))
            mu.drawClocks(
                getBlock(fraction, pastTime[0], nowArr[0], false, 0, isHorizontal, animation),
                getBlock(fraction, pastTime[1], nowArr[1], false, 1, isHorizontal, animation),
                getBlock(fraction, pastTime[2], nowArr[2], false, 2, isHorizontal, animation),
                getBlock(fraction, pastTime[3], nowArr[3], false, 3, isHorizontal, animation),
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
        animation: ClockAnimation = ClockAnimation.Wave
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
            animation
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
        animation: ClockAnimation = ClockAnimation.Wave
    ) {
        val maxCol = if (isHorizontal) 15 else 7
        val maxRow = if (isHorizontal) 5 else 11

        for (i in preArr.indices) {
            val current = preArr[i]
            val after = postArr[i]

            if (isReArrange) {
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
                val pins = animation.getClockPins(
                    r,
                    current[0],
                    current[1],
                    after[0],
                    after[1],
                    col,
                    row,
                    maxCol,
                    maxRow
                )
                outArr[i][0] = pins[0]
                outArr[i][1] = pins[1]
            } else {
                outArr[i][0] = update(r, current[0], after[0], true)
                outArr[i][1] = update(r, current[1], after[1], false)
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