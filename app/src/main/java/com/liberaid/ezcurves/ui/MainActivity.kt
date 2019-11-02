package com.liberaid.ezcurves.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import com.liberaid.ezcurves.ui.custom.CurveView
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.fragments.ImportFragment
import com.liberaid.ezcurves.util.safeTransaction
import com.liberaid.ezcurves.util.withUI
import com.liberaid.renderscripttest.ScriptC_curve
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var rs: RenderScript
    private lateinit var inAlloc: Allocation
    private lateinit var outAlloc: Allocation
    private lateinit var redCurveAlloc: Allocation
    private lateinit var script: ScriptC_curve

    private val notifyChannel = Channel<Unit>(Channel.CONFLATED)
    private var handlerJob: Job? = null

    private val redCurve = ByteArray(256)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val importFragment = ImportFragment()

        supportFragmentManager.safeTransaction {
            replace(R.id.mainContainer, importFragment, importFragment.fragmentTag)
        }
    }

    private fun setup() {
        bitmap = try {
            BitmapFactory.decodeStream(assets.open("me_and_wall.jpg"))
        } catch (e: Exception) {
            Timber.d("Cannot open bitmap")
            e.printStackTrace()
            return
        }

        rs = RenderScript.create(this)

        inAlloc = Allocation.createFromBitmap(rs, bitmap)
        outAlloc = Allocation.createTyped(rs, inAlloc.type)

        redCurveAlloc = Allocation.createSized(rs, Element.U8(rs), 256)

        script = ScriptC_curve(rs).apply {
            bind_mapping_r(redCurveAlloc)
            bind_mapping_g(redCurveAlloc)
            bind_mapping_b(redCurveAlloc)
        }

        curveView.curveChangedListener = object :
            CurveView.ICurveChangedListener {
            override fun onCurveChanged() {
                runBlocking {
                    notifyChannel.send(Unit)
                }
            }
        }

        handlerJob?.cancel()
        handlerJob = GlobalScope.launch { handleNotifyChannel() }
    }

    private suspend fun handleNotifyChannel() = coroutineScope {
        for(notify in notifyChannel){
            curveView.fillCurve(redCurve)

            redCurveAlloc.copy1DRangeFrom(0, 256, redCurve)

            script.forEach_apply(inAlloc, outAlloc)
            outAlloc.copyTo(bitmap)

            withUI {
                ivPreview.setImageBitmap(bitmap)
            }
        }
    }

    override fun onDestroy() {
        handlerJob?.cancel()
        handlerJob = null
        super.onDestroy()
    }
}
