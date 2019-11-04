package com.liberaid.ezcurves

import android.app.Application
import com.liberaid.ezcurves.dagger.AppModule
import com.liberaid.ezcurves.dagger.DaggerAppComponent
import timber.log.Timber

class MyApp : Application() {

    val appComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}