package com.shubhamgupta16.advancedwidget

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin


class CanvasMaker(baseBg: Bitmap, private val stroke: Float, private val size: Float, private val padding: Float, color: Int) {

    private val bg:Bitmap = baseBg.copy(baseBg.config!!, baseBg.isMutable)
    private val canvas: Canvas = Canvas(bg)
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.style = Paint.Style.STROKE
        paint.isDither = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        paint.color = color
        paint.strokeWidth = stroke

        circlePaint.style = Paint.Style.FILL
        circlePaint.isDither = true
        circlePaint.strokeJoin = Paint.Join.ROUND
        circlePaint.strokeCap = Paint.Cap.ROUND
        circlePaint.color = 0x00282E41.toInt()
    }

    fun drawClocks(
        block1: Array<FloatArray>,
        block2: Array<FloatArray>,
        block3: Array<FloatArray>,
        block4: Array<FloatArray>
    ) {

        val top = padding + size/2f
        val left = padding + size/2f

        drawBlock(block1, left, top)
        drawBlock(block2, left + size * 4f, top)
        drawBlock(block3, left, top + size * 6f)
        drawBlock(block4, left + size * 4f, top + size * 6f)
    }

    private fun drawBlock(
        numberArray: Array<FloatArray>,
        baseX: Float,
        baseY: Float
    ) {
        var top = baseY
        var left = baseX
        for (i in numberArray.indices) {
            drawCircle(left, top)
            val pin = numberArray[i]
            drawLine(pin[0], size / 2.4f, left, top)
            drawLine(pin[1], size / 2.8f, left, top)
            if ((i + 1) % 4 == 0) {
                top += size
                left = baseX
            } else
                left += size
        }
    }

    private fun drawLine(angle: Float, radius: Float, x: Float, y: Float) {
        val toX = x + radius * sin(Math.toRadians(angle.toDouble()))
        val toY = y + radius * cos(Math.toRadians(angle.toDouble()))
        canvas.drawLine(x, y, toX.toFloat(), toY.toFloat(), paint)
    }

    private fun drawCircle(x: Float, y: Float) {
        canvas.drawCircle(x, y, size / 2f, circlePaint)
    }


    fun getBitmap(roundPx: Float): Bitmap {
        return roundCornered(bg, roundPx)
    }

    private fun roundCornered(bitmap: Bitmap, roundPx: Float): Bitmap {
        val result = Bitmap.createBitmap(
            bitmap.width, bitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLUE
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return result
    }
}