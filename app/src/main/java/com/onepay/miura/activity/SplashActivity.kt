package com.onepay.miura.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onepay.miura.R
import com.onepay.miura.common.Utils
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setSplashScreenTimer()
    }

    private fun setSplashScreenTimer() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                finish()
              //  if(App)
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
        }, 5 * 1000)
    }
}