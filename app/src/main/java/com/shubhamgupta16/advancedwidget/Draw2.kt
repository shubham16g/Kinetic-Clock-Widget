package com.shubhamgupta16.advancedwidget


import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
        val padding = sf.padding(widgetId) // 15
        val height = size * 12 + padding * 2
        val width = size * 8 + padding * 2

        val baseBg =
            Bitmap.createBitmap(width.roundToInt(), height.roundToInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBg)

        if (sf.showBackground(widgetId)) {

            if (sf.bgColor(widgetId) == Sf.BLUR_COLOR) {
                if (Utils.haveMangeStoragePermission(context)) {
                    try {
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
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                val solidPaint = Paint()
                solidPaint.color = sf.bgColor(widgetId)
                solidPaint.alpha = sf.bgOpacity(widgetId) * 5
                canvas.drawRect(
                    0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), solidPaint
                )
            }

        }

        if (sf.showClocks(widgetId)) {
            /*var singleClock = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.single_circle,
                BitmapFactory.Options().apply {
                    inScaled = false
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
            )
            singleClock = Bitmap.createScaledBitmap(
                singleClock,
                size.roundToInt(),
                size.roundToInt(),
                false
            )*/
            val singleClock = loadCircleAsBitmap(context, size.roundToInt(), size.roundToInt())
            val alphaPaint = Paint().apply {
                alpha = (sf.clocksOpacity(widgetId) * 2.55f).roundToInt()
            }
            for (i in 0..7) {
                for (j in 0..11) {
                    canvas.drawBitmap(
                        singleClock,
                        i * size + padding,
                        j * size + padding,
                        alphaPaint
                    )
                }
            }
//            val clocks = BitmapFactory.decodeResource(context.resources, R.drawable.clock_frames_1)
//
//            val frontBmp = Bitmap.createScaledBitmap(
//                clocks,
//                (size * 8 * 1.035).roundToInt(),
//                (size * 12 * 1.03).roundToInt(),
//                false
//            )
//            val alphaPaint = Paint()
//            alphaPaint.alpha = sf.clocksOpacity(widgetId) * 5
//            canvas.drawBitmap(frontBmp, padding * 0.68f, padding * 0.68f, alphaPaint)
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
        return mu.getBitmap(dpToPx(3))
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
        return mu.getBitmap(dpToPx(3))
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