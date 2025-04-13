package com.shubhamgupta16.advancedwidget

import com.shubhamgupta16.advancedwidget.core.ClockWidgetProvider

class TestWidget : KineticClockWidget() {
    override fun getWidgetClass(): Class<out ClockWidgetProvider> = TestWidget::class.java
}