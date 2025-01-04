package com.example.timer2

import android.os.PowerManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.timer2.adapters.ViewPagerAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // PowerManager 및 WakeLock 설정
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ViewPager 설정
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        // 앱이 백그라운드로 갈 때 WakeLock 해제
        releaseWakeLock()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 앱 종료 시 WakeLock 해제
        releaseWakeLock()
    }

    private fun releaseWakeLock() {
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
