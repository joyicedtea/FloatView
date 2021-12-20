package com.example.myapp

import android.app.Application

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FloatWindowManager.init(this)
    }
}