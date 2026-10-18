package com.shubhamgupta16.advancedwidget

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin


class CanvasMaker(
    baseBg: Bitmap,
    targetBitmap: Bitmap = Bitmap.createBitmap(baseBg.width, baseBg.height, Bitmap.Config.ARGB_8888),
    private val stroke: Float,
    private val size: Float,
    private val padding: Float,
    private val margin: Float,
    private val borderRadius: Float,
    color: Int
) {

    private val bg: Bitmap = targetBitmap
    private val canvas: Canvas = Canvas(bg)
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private val clipPath = Path()

    init {
        bg.eraseColor(Color.TRANSPARENT)
        if (!baseBg.isRecycled) {
            canvas.drawBitmap(baseBg, 0f, 0f, null)
        }

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
        block4: Array<FloatArray>,
        isHorizontal: Boolean = false
    ) {

        val top = margin + padding + size / 2f
        val left = margin + padding + size / 2f

        rectF.set(margin, margin, bg.width.toFloat() - margin, bg.height.toFloat() - margin)
        clipPath.reset()
        clipPath.addRoundRect(rectF, borderRadius, borderRadius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)

        if (isHorizontal) {
            drawBlock(block1, left, top)
            drawBlock(block2, left + size * 4f, top)
            drawBlock(block3, left + size * 8f, top)
            drawBlock(block4, left + size * 12f, top)
        } else {
            drawBlock(block1, left, top)
            drawBlock(block2, left + size * 4f, top)
            drawBlock(block3, left, top + size * 6f)
            drawBlock(block4, left + size * 4f, top + size * 6f)
        }
        canvas.restore()
    }

    private fun drawBlock(
        numberArray: Array<FloatArray>,
        baseX: Float,
        baseY: Float
    ) {
        var top = baseY
        var left = baseX
        for (i in numberArray.indices) {
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
        val rad = angle * 0.017453292f
        val toX = x + radius * sin(rad)
        val toY = y + radius * cos(rad)
        canvas.drawLine(x, y, toX, toY, paint)
    }


    fun getBitmap(): Bitmap {
        return bg
    }
}
