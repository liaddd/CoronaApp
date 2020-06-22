package com.liad.coronaapp.activities

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.liad.coronaapp.R
import com.liad.coronaapp.utils.extension.changeActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        moveToMain()
    }

    // Navigate to MainActivity
    private fun moveToMain() {
        Handler().postDelayed({
            changeActivity(MainActivity::class.java, true)
        }, 2000)
    }
}