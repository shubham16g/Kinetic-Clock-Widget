package com.shubhamgupta16.advancedwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class CanvasUtils {
    public static Canvas buildCanvas(Context context,Canvas canvas){
        Rect rect = new Rect();
        rect.set(100, 100, 300, 300);

        ClockView view = new ClockView(context);
        view.setBackgroundColor(Color.WHITE);
        view.setGravity(Gravity.CENTER);
        view.rotate(90f, 190);

        int widthSpec = View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);

        view.layout(0, 0, rect.width(), rect.height());

        canvas.save();
        canvas.translate(rect.left, rect.top);
        view.draw(canvas);
        canvas.restore();

        return canvas;
    }
}
