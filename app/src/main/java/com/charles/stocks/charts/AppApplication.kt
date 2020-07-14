package com.charles.stocks.charts

import android.app.Application

class AppApplication : Application() {
    companion object {
        private lateinit var sInstance: Application
        fun get(): Application {
            return sInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
    }
}