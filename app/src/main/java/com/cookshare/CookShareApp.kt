package com.cookshare

import android.app.Application
import com.cookshare.data.local.database.AppDatabase

class CookShareApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CookShareApp
            private set
    }
}