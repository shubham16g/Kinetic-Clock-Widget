package com.shubhamgupta16.advancedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class GIFWallpaperService extends WallpaperService {

    @Override
    public WallpaperService.Engine onCreateEngine() {
        return new GIFWallpaperEngine();
    }

    private class GIFWallpaperEngine extends WallpaperService.Engine {

        private SurfaceHolder holder;
        private final Draw draw;

        public GIFWallpaperEngine() {
            MainUtils mu = new MainUtils(getApplicationContext());
            draw = new Draw(mu);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Canvas c = surfaceHolder.getSurface().lockCanvas(null);
            this.holder = surfaceHolder;

        }
        private void draw() {
            draw.draw(fraction -> {
                Log.d("here", "i am");
                Canvas canvas = holder.lockCanvas();
                canvas.save();
                canvas.scale(1f, 1f);
                Paint p = new Paint();
                p.setColor(Color.GRAY);
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
                canvas.drawBitmap(draw.createTimeClock(fraction), 150, 400, null);
                canvas.restore();
                holder.unlockCanvasAndPost(canvas);
                return null;
            });

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            draw.setVisible(visible);
            BroadcastReceiver tickReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                        Log.d("tagtag", "visi" + visible);
                        draw();
                    }
                }
            };
            if (visible) {
                draw();
                registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
            } else {
//                unregisterReceiver(tickReceiver);
            }
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
//            handler.removeCallbacks(drawGIF);
        }
    }
}