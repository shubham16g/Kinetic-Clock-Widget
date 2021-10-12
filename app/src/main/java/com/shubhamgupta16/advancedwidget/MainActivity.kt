package com.shubhamgupta16.advancedwidget

import android.app.WallpaperManager
import android.content.*
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val image = findViewById<ImageView>(R.id.image)
//        val mu = MainUtils(this)

        /*image.setImageBitmap(mu.drawUI(MainUtils.m0, MainUtils.m1, MainUtils.m1, MainUtils.m0))

        val va = ValueAnimator.ofFloat(0f, 100f)
        va.duration = 3000 //in millis
        va.addUpdateListener { animation ->
            Log.d("cas", "${animation.animatedValue}")
            val arr = mu.translate(animation.animatedFraction, MainUtils.m0, MainUtils.m1)
            image.setImageBitmap(mu.drawUI(MainUtils.m0, MainUtils.m1, MainUtils.m1, arr))
        }
        va.start()*/

        /*val tickReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action!!.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    Log.v("Karl", "tick tock tick tock...")
                }
            }
        }
        registerReceiver(tickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))*/

//        val i = Intent(this, RunnerService::class.java)
//        startService(i)
    }

    fun onClick(view: View?) {
        val intent = Intent(
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(this, GIFWallpaperService::class.java)
        )
        startActivity(intent)
    }

    fun ballAnim(view: View?) {
        val intent = Intent(this, SurfaceViewActivity::class.java)
        startActivity(intent)
    }
}