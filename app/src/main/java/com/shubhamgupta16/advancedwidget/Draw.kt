package com.shubhamgupta16.advancedwidget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.*
import kotlin.math.sin

class Draw() {

    companion object {
        private val hidden_____ = floatArrayOf(315f, 315f)
        private val bottomRight = floatArrayOf(0f, 90f)
        private val bottomLeft_ = floatArrayOf(0f, 270f)
        private val horizontal_ = floatArrayOf(270f, 90f)
        private val vertical___ = floatArrayOf(0f, 180f)
        private val topRight___ = floatArrayOf(90f, 180f)
        private val topLeft____ = floatArrayOf(270f, 180f)

        private val topLSide___ = floatArrayOf(315f, 180f)
        private val topRSide___ = floatArrayOf(45f, 180f)
        private val bottomLSide = floatArrayOf(225f, 0f)
        private val bottomRSide = floatArrayOf(135f, 0f)

        private val sideBottomR = floatArrayOf(90f, 315f)
        private val sideTopL___ = floatArrayOf(270f, 135f)

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
//    class system started here

    private fun getNumberArray(num: Int): Array<FloatArray> {
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
    private fun translate(
        r: Float, preArr: Array<FloatArray>,
        postArr: Array<FloatArray>,
    ): Array<FloatArray> {
        Log.d("TAG_BEFORE", "- $r = ${preArr[0][0]} = ${preArr[0][1]} ")
        for (i in preArr.indices) {
            val current = preArr[i]
            val after = postArr[i]
            preArr[i] = floatArrayOf(
                update(r, current[0], after[0], true),
                update(r, current[1], after[1], false)
            )
        }
        Log.d("TAG_AFTER", "- $r = ${preArr[0][0]} = ${preArr[0][1]}")
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

    var visible = true
    private lateinit var va: ValueAnimator
    private val irr = IntArray(4)

    @SuppressLint("Recycle")
    fun draw(listener: (fraction: Float) -> Unit) {
        if (visible) {
//                int[] timeZ = mu.getTime();
            if (!::va.isInitialized) {
                va = ValueAnimator.ofFloat(0f, 1f)
                va.duration = 5000
                va.interpolator = AccelerateDecelerateInterpolator()
            }
            va.removeAllUpdateListeners()
            va.addUpdateListener { animation ->
                listener(animation.animatedFraction)
                if (animation.animatedFraction == 1f) {
                    va.removeAllUpdateListeners()
                }
            }
            va.start()
        }
    }

    fun init() {
        Log.d("Draw:", "init")
        val calendar = Calendar.getInstance();
        val hour = calendar.get(Calendar.HOUR)
        val min = calendar.get(Calendar.MINUTE)
        irr[0] = (hour - hour % 10) / 10
        irr[1] = hour - (irr[0] * 10)
        irr[2] = (min - min % 10) / 10
        irr[3] = (min - (irr[2] * 10))
        Log.d("Draw:", "Time: ${irr[0]} ${irr[1]} ${irr[2]} ${irr[3]}")
    }



    fun createTimeClock(context: Context, fraction: Float): Bitmap {
        val mu = MainDrawer(context)
        mu.drawClocks(
            getBlockHour1(fraction),
            getBlockHour2(fraction),
            getBlockMin1(fraction),
            getBlockMin2(fraction)
        )
        /*mu.drawClocks(
            getNumberArray(0),
            getNumberArray(1),
            getNumberArray(2),
            getNumberArray(3),
        )*/
        return mu.getBitmap()
    }


//    fun createTimeClock(fraction: Float): Bitmap {
//       return mu.drawUI(getBlockHour1(fraction), getBlockHour2(fraction), getBlockMin1(fraction), getBlockMin2(fraction))
//    }

    private fun getBlockMin2(fraction: Float): Array<FloatArray> {
        val arr = if (irr[3] == 0) getNumberArray(9).clone()
        else getNumberArray(irr[3] - 1).clone()
        translate(fraction, arr, getNumberArray(irr[3]))
        return arr
    }

    private fun getBlockMin1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[3] == 0 && irr[2] == 0)
            arr = getNumberArray(5).clone()
        else if (irr[3] == 0)
            arr = getNumberArray(irr[2] - 1).clone()
        else {
            arr = getNumberArray(irr[2])
            isClone = false
        }
        if (isClone)
            translate(fraction, arr, getNumberArray(irr[2]))
        return arr
    }

    private fun getBlockHour2(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[0] == 1 && irr[1] == 2 && irr[2] == 0 && irr[2] == 0)
            arr = getNumberArray(1).clone()
        else if (irr[0] == 0 && irr[1] == 1 && irr[2] == 0 && irr[2] == 0)
            arr = getNumberArray(2).clone()
        else {
            arr = getNumberArray(irr[1])
            isClone = false
        }
        if (isClone)
            translate(fraction, arr, getNumberArray(irr[1]))
        return arr
    }

    private fun getBlockHour1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[0] == 1 && irr[1] == 0 && irr[2] == 0 && irr[2] == 0)
            arr = getNumberArray(0).clone()
        else if (irr[0] == 0 && irr[1] == 1 && irr[2] == 0 && irr[2] == 0)
            arr = getNumberArray(1).clone()
        else {
            arr = getNumberArray(irr[0])
            isClone = false
        }
        if (isClone)
            translate(fraction, arr, getNumberArray(irr[0]))
        return arr
    }
}