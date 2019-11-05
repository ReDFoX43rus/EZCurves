package com.liberaid.ezcurves.dagger

import android.content.Context
import android.renderscript.RenderScript
import com.liberaid.renderscripttest.ScriptC_crop
import com.liberaid.renderscripttest.ScriptC_curve
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RenderScriptModule {

    @Singleton
    @Provides
    fun provideRenderScript(appContext: Context): RenderScript = RenderScript.create(appContext)

    @Singleton
    @Provides
    fun provideCurveScript(rs: RenderScript) = ScriptC_curve(rs)

    @Singleton
    @Provides
    fun provideCropScript(rs: RenderScript) = ScriptC_crop(rs)

}