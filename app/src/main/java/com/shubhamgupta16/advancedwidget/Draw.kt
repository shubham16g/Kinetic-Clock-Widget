package com.shubhamgupta16.advancedwidget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import com.shubhamgupta16.advancedwidget.MainUtils.Companion.m0
import java.util.*

class Draw(private val mu: MainUtils) {

    var visible = true
    var appWidgetId = 54
    lateinit var va: ValueAnimator

    @SuppressLint("Recycle")
    fun draw(listener: (fraction: Float) -> Unit) {
        if (visible) {
//                int[] timeZ = mu.getTime();
            if (!::va.isInitialized) {
                va = ValueAnimator.ofFloat(0f, 100f)
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

    var irr = IntArray(4)

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

    private fun getBlockMin2(fraction: Float): Array<FloatArray> {
        val arr = if (irr[3] == 0) mu.getNumberArray(9).clone()
        else mu.getNumberArray(irr[3] - 1).clone()
        mu.translate(fraction, arr, mu.getNumberArray(irr[3]))
        return arr
    }

    private fun getBlockMin1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[3] == 0 && irr[2] == 0)
            arr = mu.getNumberArray(5).clone()
        else if (irr[3] == 0)
            arr = mu.getNumberArray(irr[2] - 1).clone()
        else {
            arr = mu.getNumberArray(irr[2])
            isClone = false
        }
        if (isClone)
            mu.translate(fraction, arr, mu.getNumberArray(irr[2]))
        return arr
    }

    private fun getBlockHour2(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[0] == 1 && irr[1] == 2 &&irr[2] == 0 && irr[2] == 0)
            arr = mu.getNumberArray(1).clone()
        else if (irr[0] == 0 && irr[1] == 1 &&irr[2] == 0 && irr[2] == 0)
            arr = mu.getNumberArray(2).clone()
        else {
            arr = mu.getNumberArray(irr[1])
            isClone = false
        }
        if (isClone)
            mu.translate(fraction, arr, mu.getNumberArray(irr[1]))
        return arr
    }

    private fun getBlockHour1(fraction: Float): Array<FloatArray> {
        val arr: Array<FloatArray>
        var isClone = true
        if (irr[0] == 1 && irr[1] == 0 &&irr[2] == 0 && irr[2] == 0)
            arr = mu.getNumberArray(0).clone()
        else if (irr[0] == 0 && irr[1] == 1 &&irr[2] == 0 && irr[2] == 0)
            arr = mu.getNumberArray(1).clone()
        else {
            arr = mu.getNumberArray(irr[0])
            isClone = false
        }
        if (isClone)
            mu.translate(fraction, arr, mu.getNumberArray(irr[0]))
        return arr
    }

    fun createTimeClock(fraction: Float): Bitmap {
        return mu.drawUI(getBlockHour1(fraction), getBlockHour2(fraction), getBlockMin1(fraction), getBlockMin2(fraction))
    }
}