package com.shubhamgupta16.advancedwidget

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity.POWER_SERVICE

object Utils {
    fun hasIgnoreBatteryOptimizationPermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm: PowerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(activity.packageName)) {
                return false
            }
        }
        return true
    }

    fun requestIgnoreBatteryOptimization(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun dp(dp: Float): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics
    ).toInt()

    fun radioDrawable(bg: Int): Drawable {
        val normalDrawable = unSelectedRadioDrawable(bg)
        val selectedDrawable = selectedDrawable(bg)
        // === State selector ===
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), selectedDrawable)
            addState(intArrayOf(), normalDrawable)
        }
    }

    fun selectedDrawable(bg: Int): Drawable {
        val sizeOuter = dp(48f) // slightly larger to avoid stroke cutoff
        val exSizeOuter = dp(55f) // slightly larger to avoid stroke cutoff
        val sizeInner = dp(36f)
        val strokeWidth = dp(3f)
        val inset = dp(7f) // (50 - 36) / 2

        // === Not selected: colored flat circle ===


        // === Selected: outer gray ring + inner colored circle ===
        val outerCircle = ShapeDrawable(OvalShape()).apply {
            intrinsicWidth = sizeOuter
            intrinsicHeight = sizeOuter
            paint.color = Color.TRANSPARENT
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth.toFloat()
            paint.color = Color.parseColor("#B0BEC5")
        }

        val extraOuterCircle = ShapeDrawable(OvalShape()).apply {
            intrinsicWidth = exSizeOuter
            intrinsicHeight = exSizeOuter
            paint.color = Color.TRANSPARENT
        }

        val innerCircle = ShapeDrawable(OvalShape()).apply {
            intrinsicWidth = sizeInner
            intrinsicHeight = sizeInner
            paint.color = bg
        }

        val imd = LayerDrawable(arrayOf(extraOuterCircle, outerCircle)).apply {
            val dp3 = dp(2f)
            setLayerInset(1, dp3, dp3, dp3, dp3)
        }
        return LayerDrawable(arrayOf(imd, innerCircle)).apply {
            setLayerInset(1, inset, inset, inset, inset)
        }
    }

    fun unSelectedRadioDrawable(bg: Int): Drawable {
        val exSizeOuter = dp(55f) // slightly larger to avoid stroke cutoff
        val sizeInner = dp(36f)
        val inset = dp(7f)
        val n = ShapeDrawable(OvalShape()).apply {
            intrinsicWidth = sizeInner
            intrinsicHeight = sizeInner
            paint.color = bg
        }

        val extraOuterCircle = ShapeDrawable(OvalShape()).apply {
            intrinsicWidth = exSizeOuter
            intrinsicHeight = exSizeOuter
            paint.color = Color.TRANSPARENT
        }
        return LayerDrawable(arrayOf(extraOuterCircle, n)).apply {
            setLayerInset(1, inset, inset, inset, inset)
        }
    }


}

 fun dpToPx(dp: Int): Float {
     return TypedValue.applyDimension(
         TypedValue.COMPLEX_UNIT_DIP,
         dp.toFloat(),
         Resources.getSystem().displayMetrics
     )
}