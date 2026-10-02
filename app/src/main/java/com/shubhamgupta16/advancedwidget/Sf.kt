package com.shubhamgupta16.advancedwidget

import android.content.Context
import android.graphics.Color
import com.shubhamgupta16.advancedwidget.core.WidgetSharedPref


class Sf(context: Context) : WidgetSharedPref(context) {

    fun size(widgetId: Int): Float = get(widgetId, "size", dpToPx(20))

    fun setSize(widgetId: Int, size: Float) {
        put(widgetId, "size", size)
    }

    fun padding(widgetId: Int): Float =
        (size(widgetId) / dpToPx(15)) * get(widgetId, "padding", dpToPx(8))

    fun setPadding(widgetId: Int, padding: Float) {
        put(widgetId, "padding", padding)
    }

    fun stroke(widgetId: Int): Float = get(widgetId, "stroke", size(widgetId) / 15f)

    fun setStroke(widgetId: Int, stroke: Float) {
        put(widgetId, "stroke", stroke)
    }

    fun color(widgetId: Int): Int = get(widgetId, "pinsColor", Color.BLACK)

    fun setColor(widgetId: Int, color: Int) {
        put(widgetId, "pinsColor", color)
    }

    fun showBackground(widgetId: Int): Boolean = get(widgetId, "showBackground", true)

    fun setShowBackground(widgetId: Int, showBackground: Boolean) {
        put(widgetId, "showBackground", showBackground)
    }

    fun bgOpacity(widgetId: Int): Int = get(widgetId, "bgOpacity", 100)

    fun setBgOpacity(widgetId: Int, opacity: Int) {
        put(widgetId, "bgOpacity", opacity)
    }

    fun bgColor(widgetId: Int): Int = get(widgetId, "bgColor", Color.WHITE)

    fun setBgColor(widgetId: Int, color: Int) {
        put(widgetId, "bgColor", color)
    }

    fun showClocks(widgetId: Int): Boolean = get(widgetId, "showClocks", true)

    fun setShowClocks(widgetId: Int, showClocks: Boolean) {
        put(widgetId, "showClocks", showClocks)
    }

    fun clocksOpacity(widgetId: Int): Int = get(widgetId, "clocksOpacity", 40)

    fun setClocksOpacity(widgetId: Int, opacity: Int) {
        put(widgetId, "clocksOpacity", opacity)
    }

}