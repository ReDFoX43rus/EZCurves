package com.liberaid.ezcurves.ui.fragments.editfragment

import android.graphics.Bitmap
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.liberaid.ezcurves.MyApp
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.ui.custom.CurveView
import com.liberaid.ezcurves.ui.fragments.selectfragment.SelectFragment
import com.liberaid.ezcurves.util.withUI
import com.liberaid.renderscripttest.ScriptC_curve
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber

class EditFragment : BaseFragment() {
    override val fragmentId = FragmentId.EDIT_FRAGMENT

    private var bitmap: Bitmap? = null
    private var rs: RenderScript? = null
    private var inAlloc: Allocation? = null
    private var outAlloc: Allocation? = null
    private var script: ScriptC_curve? = null
    private var redCurveAllocation: Allocation? = null
    private var greenCurveAllocation: Allocation? = null
    private var blueCurveAllocation: Allocation? = null

    private val curveBuffer = ByteArray(256)
    private val notifyChannel = Channel<Unit>(Channel.CONFLATED)

    private var handlerJob: Job? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val appComponent = (activity?.application as? MyApp?)?.appComponent
        if(appComponent == null){
            Timber.w("App component is null")
            return
        }

        rs = appComponent.getRenderScript()
        redCurveAllocation = Allocation.createSized(rs, Element.U8(rs), 256)
        greenCurveAllocation = Allocation.createSized(rs, Element.U8(rs), 256)
        blueCurveAllocation = Allocation.createSized(rs, Element.U8(rs), 256)

        script = appComponent.getScriptCurve().apply {
            bind_mapping_r(redCurveAllocation)
            bind_mapping_g(greenCurveAllocation)
            bind_mapping_b(blueCurveAllocation)
        }

        val imagePath = arguments?.getString(SelectFragment.IMAGE_PATH_KEY) ?: return

        val imageFuture = Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .submit()

        handlerJob?.cancel()
        handlerJob = setupByFuture(imageFuture)
    }

    private fun setupByFuture(imageFuture: FutureTarget<Bitmap>) = GlobalScope.launch {
        bitmap = imageFuture.get()
        withUI {
            ivPreview.setImageBitmap(bitmap)
        }

        inAlloc = Allocation.createFromBitmap(rs, bitmap)
        outAlloc = Allocation.createTyped(rs, inAlloc!!.type)

        launch { handleNotifyChannel() }

        withUI {
            curveView.curveChangedListener = object : CurveView.ICurveChangedListener {
                override fun onCurveChanged() {
                    runBlocking {
                        notifyChannel.send(Unit)
                    }
                }
            }
        }
    }

    private suspend fun handleNotifyChannel() = coroutineScope {
        for(notify in notifyChannel){
            curveView.fillCurve(curveBuffer)

            redCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)
            greenCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)
            blueCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)

            script?.forEach_apply(inAlloc, outAlloc)
            outAlloc?.copyTo(bitmap)

            withUI {
                ivPreview.setImageBitmap(bitmap)
            }
        }
    }

    override fun onDestroyView() {
        handlerJob?.cancel()
        handlerJob = null

        super.onDestroyView()
    }
}