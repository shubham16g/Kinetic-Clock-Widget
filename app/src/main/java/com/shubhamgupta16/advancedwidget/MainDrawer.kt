package com.shubhamgupta16.advancedwidget

import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.Log
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class MainDrawer(private val context: Context) {

    private fun dpToPx(dp: Int): Float {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    private val size: Int = dpToPx(15).roundToInt()
    private val stroke: Float = dpToPx(1)
    private val canvas: Canvas
    private val paint: Paint
    private val bg: Bitmap
    init {
        val top = stroke * 9.3f
        val left = stroke * 12f

        val bt = BitmapFactory.decodeResource(context.resources, R.drawable.multi_clock)
        bg = Bitmap.createScaledBitmap(
            bt, (size * 8 + left + stroke * 12).toInt(),
            (size * 12 + top + stroke * 11).toInt(), false
        )
        canvas = Canvas(bg)
        paint = Paint()
        paint.color = Color.parseColor("#555555")
        paint.strokeWidth = stroke
    }



    fun drawClocks(
        block1: Array<FloatArray>,
        block2: Array<FloatArray>,
        block3: Array<FloatArray>,
        block4: Array<FloatArray>
    ) {

        val top = stroke*9.3f + size/2f
        val left = stroke*12f + size/2f

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
            val pin = numberArray[i]
            drawLine(pin[0], size / 2f, left, top)
            drawLine(pin[1], size / 2.5f, left, top)
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





    fun getBitmap(): Bitmap {
        return bg
    }

//    ***********************************************************************************************************
}