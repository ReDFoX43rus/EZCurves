package com.liberaid.ezcurves.ui.fragments.editfragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.renderscript.*
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.liberaid.ezcurves.MyApp
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.ui.custom.CurveView
import com.liberaid.ezcurves.ui.fragments.selectfragment.SelectFragment
import com.liberaid.ezcurves.util.withUI
import com.liberaid.renderscripttest.ScriptC_crop
import com.liberaid.renderscripttest.ScriptC_curve
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber

class EditFragment : BaseFragment(), View.OnClickListener {
    override val fragmentId = FragmentId.EDIT_FRAGMENT

    private var bitmap: Bitmap? = null
    private var rs: RenderScript? = null
    private var inAlloc: Allocation? = null
    private var outAlloc: Allocation? = null
    private var curveScript: ScriptC_curve? = null
    private var redCurveAllocation: Allocation? = null
    private var greenCurveAllocation: Allocation? = null
    private var blueCurveAllocation: Allocation? = null

    private val curveBuffer = ByteArray(256)
    private val identityCurve = ByteArray(256) { it.toByte() }

    private val notifyChannel = Channel<Unit>(Channel.CONFLATED)
    private var handlerJob: Job? = null

    private var colorState = ColorState.GENERAL

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

        curveScript = appComponent.getScriptCurve().apply {
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

        setState(ColorState.GENERAL)

        btnGeneralCurve.setOnClickListener(this)
        btnRedCurve.setOnClickListener(this)
        btnGreenCurve.setOnClickListener(this)
        btnBlueCurve.setOnClickListener(this)
    }

    private fun setupByFuture(imageFuture: FutureTarget<Bitmap>) = GlobalScope.launch {
        val srcBitmap = imageFuture.get()
        withUI {
            bitmap = srcBitmap

            val size = srcBitmap.width.toLong() * srcBitmap.height.toLong()
            if(size > 3000L * 4000L) {
                Timber.i("Image is too big, resizing...")
                resizeBitmap()
            }

            ivPreview.setImageBitmap(bitmap)
        }

        Timber.d("Setup allocations")
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
            withUI {
                curveView.fillCurve(curveBuffer)
            }

            redCurveAllocation?.copy1DRangeFrom(0, 256,
                if(colorState == ColorState.GENERAL || colorState == ColorState.RED)
                    curveBuffer
                else identityCurve
            )

            greenCurveAllocation?.copy1DRangeFrom(0, 256,
                if(colorState == ColorState.GENERAL || colorState == ColorState.GREEN)
                    curveBuffer
                else identityCurve
            )

            blueCurveAllocation?.copy1DRangeFrom(0, 256,
                if(colorState == ColorState.GENERAL || colorState == ColorState.BLUE)
                    curveBuffer
                else identityCurve
            )

            curveScript?.forEach_apply(inAlloc, outAlloc)
            outAlloc?.copyTo(bitmap)

            withUI {
                ivPreview.setImageBitmap(bitmap)
            }
        }
    }

    private fun resizeBitmap() {
        val bitmap = bitmap ?: return

        val newWidth = bitmap.width / 2
        val newHeight = bitmap.height / 2

        val outBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val outAlloc = Allocation.createFromBitmap(rs, outBitmap)

        val inAlloc = Allocation.createFromBitmap(rs, bitmap)

        val resizeScript = ScriptIntrinsicResize.create(rs)
        resizeScript.apply {
            setInput(inAlloc)
            forEach_bicubic(outAlloc)
        }

        outAlloc.copyTo(outBitmap)
        this.bitmap = outBitmap

        outAlloc.destroy()
        inAlloc.destroy()
        resizeScript.destroy()

        Timber.i("Resizing -- OK, width=$newWidth, height=$newHeight")
    }

    override fun onDestroyView() {
        handlerJob?.cancel()
        handlerJob = null

        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        Timber.d("OnClick")

        v ?: return

        when(v.id) {
            R.id.btnGeneralCurve -> setState(ColorState.GENERAL)
            R.id.btnRedCurve -> setState(ColorState.RED)
            R.id.btnGreenCurve -> setState(ColorState.GREEN)
            R.id.btnBlueCurve -> setState(ColorState.BLUE)
        }
    }

    private fun setState(state: ColorState) {
        colorState = state

        val color = when(state){
            ColorState.GENERAL -> Color.BLACK
            ColorState.RED -> Color.RED
            ColorState.GREEN -> Color.GREEN
            ColorState.BLUE -> Color.BLUE
        }

        curveView.curveColor = color
        curveView.circleColor = color

        Timber.d("New state: $state")
    }

    private enum class ColorState {
        GENERAL,
        RED,
        GREEN,
        BLUE,
    }

    companion object {
        private const val RESIZE_FACTOR = 2
    }
}