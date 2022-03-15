package com.onepay.miura.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onepay.miura.R
import com.onepay.miura.common.PreferencesKeys
import com.onepay.miura.common.Utils
import com.onepay.miura.data.AppSharedPreferences
import java.util.*

class SplashActivity : AppCompatActivity() {
    var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)
        setSplashScreenTimer()
    }

    private fun setSplashScreenTimer() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                finish()
                if (AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.saveLogin))
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                else
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
            }
        }, 5 * 1000)
    }
}