package com.shubhamgupta16.advancedwidget

import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import kotlin.math.roundToInt


class MainUtils(private val context: Context) {
    private val size: Int;
    private val stroke: Float;

    init {
        size = dpToPx(60).roundToInt();
        stroke = dpToPx(4);
    }

    private fun dpToPx(dp: Int): Float {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getNumberArray(num: Int): Array<FloatArray> {
        return when (num) {
            0 -> m0
            1 -> m1
            2 -> m2
            3 -> m3
            4 -> m4
            5 -> m5
            6 -> m6
            7 -> m7
            8 -> m8
            9 -> m9
            else -> m0
        }
    }

    companion object {
        private val hidden_____ = floatArrayOf(225f, 225f)
        private val bottomRight = floatArrayOf(180f, 90f)
        private val bottomLeft_ = floatArrayOf(180f, 270f)
        private val horizontal_ = floatArrayOf(270f, 90f)
        private val vertical___ = floatArrayOf(180f, 0f)
        private val topRight___ = floatArrayOf(90f, 0f)
        private val topLeft____ = floatArrayOf(270f, 0f)

        private val topLSide___ = floatArrayOf(225f, 0f)
        private val topRSide___ = floatArrayOf(135f, 0f)
        private val bottomLSide = floatArrayOf(315f, 180f)
        private val bottomRSide = floatArrayOf(45f, 180f)

        private val sideBottomR = floatArrayOf(90f, 225f)
        private val sideTopL___ = floatArrayOf(270f, 45f)

        val m0 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            vertical___, vertical___, vertical___, vertical___,
            vertical___, vertical___, vertical___, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m1 = arrayOf(
            bottomRight, horizontal_, bottomLeft_, hidden_____,
            topRight___, bottomLeft_, vertical___, hidden_____,
            hidden_____, vertical___, vertical___, hidden_____,
            hidden_____, vertical___, vertical___, hidden_____,
            bottomRight, topLeft____, topRight___, bottomLeft_,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m2 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, sideBottomR, topLeft____, topLSide___,
            bottomRSide, bottomRight, sideTopL___, hidden_____,
            vertical___, topRight___, horizontal_, bottomLeft_,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m3 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, bottomRight, topLeft____, topLSide___,
            hidden_____, topRight___, bottomLeft_, bottomLSide,
            bottomRight, horizontal_, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m4 = arrayOf(
            bottomRight, bottomLeft_, bottomRight, bottomLeft_,
            vertical___, vertical___, vertical___, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, hidden_____, vertical___, vertical___,
            hidden_____, hidden_____, topRight___, topLeft____,
        )

        val m5 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, horizontal_, topLeft____,
            vertical___, topRight___, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            bottomRight, horizontal_, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m6 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, horizontal_, topLeft____,
            vertical___, topRight___, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m7 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, hidden_____, topLSide___, topLSide___,
            hidden_____, bottomRSide, bottomRSide, hidden_____,
            hidden_____, vertical___, vertical___, hidden_____,
            hidden_____, topRight___, topLeft____, hidden_____,
        )
        val m8 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            topRSide___, topRight___, topLeft____, topLSide___,
            bottomRSide, bottomRight, bottomLeft_, bottomLSide,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        val m9 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, bottomLeft_, vertical___,
            bottomRight, horizontal_, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
    }

    fun translate(
        r: Float, preArr: Array<FloatArray>,
        postArr: Array<FloatArray>,
    ): Array<FloatArray> {
        for (i in preArr.indices) {
            val current = preArr[i]
            val after = postArr[i]
            preArr[i] = floatArrayOf(
                update(r, current[0], after[0], true),
                update(r, current[1], after[1], true)
            )
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

    fun drawUI(
        block1: Array<FloatArray>,
        block2: Array<FloatArray>,
        block3: Array<FloatArray>,
        block4: Array<FloatArray>
    ): Bitmap {
        val top = stroke*9.3f
        val left = stroke*12f

        var bg = BitmapFactory.decodeResource(context.resources, R.drawable.multi_clock)
        bg = Bitmap.createScaledBitmap(
            bg, (size * 8 + left + stroke * 12).toInt(),
            (size * 12 + top + stroke * 11).toInt(), false
        )
        val canvas = Canvas(bg)


        createBlock(canvas, block1, left, top)
        createBlock(canvas, block2, left + size * 4f, top)
        createBlock(canvas, block3, left, top + size * 6f)
        createBlock(canvas, block4, left + size * 4f, top + size * 6f)

        return bg
    }

    private fun createBlock(
        canvas: Canvas,
        numberArray: Array<FloatArray>,
        baseX: Float,
        baseY: Float
    ) {
        var top = baseY
        var left = baseX
        for (i in numberArray.indices) {
            val pin = numberArray[i]
            canvas.drawBitmap(drawClock(pin[0], pin[1]), left, top, null)
            if ((i + 1) % 4 == 0) {
                top += size
                left = baseX
            } else
                left += size
        }
    }

    private fun drawClock(hour: Float, min: Float): Bitmap {

        val bgCircle = Paint()
        bgCircle.color = Color.WHITE

        val paint = Paint()
        paint.color = Color.parseColor("#333333")
        paint.strokeWidth = stroke

        val bg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bg)
//        can be replaced with original background
//        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgCircle)
        canvas.drawCircle(size / 2f, size / 2f, stroke / 2, paint)
        canvas.save()
        canvas.rotate(hour, size / 2f, size / 2f)
        canvas.drawLine(size / 2f, size / 2f, size / 2f, stroke * 1.6f, paint)
        canvas.restore()
        canvas.save()
        canvas.rotate(min, size / 2f, size / 2f)
        canvas.drawLine(size / 2f, size / 2f, size / 2f, stroke * 0.7f, paint)
        canvas.restore()
        return bg
    }
}