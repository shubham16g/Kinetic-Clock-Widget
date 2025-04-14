package com.shubhamgupta16.advancedwidget.core

import android.content.Context

abstract class WidgetSharedPref(context: Context) {
    @PublishedApi
    internal val sf = context.getSharedPreferences("clock_widget_main_sf", 0)

    //    generic function:
    fun put(widgetId: Int, key: String, value: Any) {
        val editor = sf.edit()
        when (value) {
            is String -> editor.putString(key + "_$widgetId", value)
            is Int -> editor.putInt(key + "_$widgetId", value)
            is Boolean -> editor.putBoolean(key + "_$widgetId", value)
            is Float -> editor.putFloat(key + "_$widgetId", value)
            is Long -> editor.putLong(key + "_$widgetId", value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
        editor.apply()
    }

    inline fun <reified T> get(widgetId: Int, key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> sf.getString(key + "_$widgetId", defaultValue as String) as T
            Int::class -> sf.getInt(key + "_$widgetId", defaultValue as Int) as T
            Boolean::class -> sf.getBoolean(key + "_$widgetId", defaultValue as Boolean) as T
            Float::class -> sf.getFloat(key + "_$widgetId", defaultValue as Float) as T
            Long::class -> sf.getLong(key + "_$widgetId", defaultValue as Long) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}