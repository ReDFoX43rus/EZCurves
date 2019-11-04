package com.liberaid.ezcurves.dagger

import android.renderscript.RenderScript
import com.liberaid.ezcurves.MyApp
import com.liberaid.renderscripttest.ScriptC_curve
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, RenderScriptModule::class])
interface AppComponent {

    fun getApp(): MyApp

    fun getRenderScript(): RenderScript
    fun getScriptCurve(): ScriptC_curve

}