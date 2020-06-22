package com.liad.coronaapp

import android.app.Application
import com.liad.coronaapp.di.appModule
import org.koin.core.context.startKoin

class CoronaApp : Application() {

    companion object {
        lateinit var instance: Application
            private set
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            modules(listOf(appModule))
        }
    }
}