package com.liberaid.ezcurves.dagger

import com.liberaid.ezcurves.MyApp
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val app: MyApp) {

    @Provides
    fun provideApp() = app

    @Provides
    fun provideAppContext() = app.applicationContext

}