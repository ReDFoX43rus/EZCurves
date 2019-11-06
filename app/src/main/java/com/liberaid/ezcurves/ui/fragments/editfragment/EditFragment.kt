package com.liberaid.ezcurves.ui.fragments.editfragment

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicResize
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.liberaid.ezcurves.MyApp
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.ui.custom.CurveView
import com.liberaid.ezcurves.ui.fragments.selectfragment.SelectFragment
import com.liberaid.ezcurves.util.launchUICatching
import com.liberaid.ezcurves.util.withUI
import com.liberaid.renderscripttest.ScriptC_curve
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class EditFragment : BaseFragment(), View.OnClickListener, CoroutineScope {
    override val fragmentId = FragmentId.EDIT_FRAGMENT

    private var bitmap: Bitmap? = null
    private var rs: RenderScript? = null
    private var inAlloc: Allocation? = null
    private var tmpAlloc: Allocation? = null
    private var outAlloc: Allocation? = null
    private var curveScript: ScriptC_curve? = null
    private var redCurveAllocation: Allocation? = null
    private var greenCurveAllocation: Allocation? = null
    private var blueCurveAllocation: Allocation? = null

    private val curveBuffer = ByteArray(256)

    private val notifyChannel = Channel<Unit>(Channel.CONFLATED)
    private var job: Job? = Job()
    override val coroutineContext: CoroutineContext
        get() = job!! + Dispatchers.Default

    private var colorState = ColorState.GENERAL

    private var imageName: String? = null

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
        imageName = imagePath.substringAfterLast('/')
            .substringBeforeLast('.')

        val imageFuture = Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .submit()

        job?.cancel()
        job = Job()
        setupByFuture(imageFuture)

        setState(ColorState.GENERAL)

        btnGeneralCurve.setOnClickListener(this)
        btnRedCurve.setOnClickListener(this)
        btnGreenCurve.setOnClickListener(this)
        btnBlueCurve.setOnClickListener(this)

        btnExport.setOnClickListener(this)
    }

    private fun setupByFuture(imageFuture: FutureTarget<Bitmap>) = launch {
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
        tmpAlloc = Allocation.createTyped(rs, inAlloc!!.type)
        outAlloc = Allocation.createTyped(rs, inAlloc!!.type)

        launch { handleNotifyChannel() }

        withUI {
            val changeListener = object : CurveView.ICurveChangedListener {
                override fun onCurveChanged() {
                    runBlocking {
                        notifyChannel.send(Unit)
                    }
                }
            }

            curveGeneral.curveChangedListener = changeListener
            curveRed.curveChangedListener = changeListener
            curveGreen.curveChangedListener = changeListener
            curveBlue.curveChangedListener = changeListener
        }
    }

    private suspend fun handleNotifyChannel() = coroutineScope {
        for(notify in notifyChannel){
            withUI {
                curveGeneral.fillCurve(curveBuffer)
            }

            /* Proceed general curve */
            redCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)
            greenCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)
            blueCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)

            curveScript?.forEach_apply(inAlloc, tmpAlloc)

            /* Proceed colored curves */
            withUI { curveRed.fillCurve(curveBuffer) }
            redCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)

            withUI { curveGreen.fillCurve(curveBuffer) }
            greenCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)

            withUI { curveBlue.fillCurve(curveBuffer) }
            blueCurveAllocation?.copy1DRangeFrom(0, 256, curveBuffer)

            curveScript?.forEach_apply(tmpAlloc, outAlloc)
            outAlloc?.copyTo(bitmap)

            withUI {
                ivPreview.setImageBitmap(bitmap)
            }
        }
    }

    private fun resizeBitmap() {
        val bitmap = bitmap ?: return

        val newWidth = bitmap.width / RESIZE_FACTOR
        val newHeight = bitmap.height / RESIZE_FACTOR

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
        inAlloc?.destroy()
        tmpAlloc?.destroy()
        outAlloc?.destroy()
        redCurveAllocation?.destroy()
        greenCurveAllocation?.destroy()
        blueCurveAllocation?.destroy()

        notifyChannel.close()

        job?.cancel()
        job = null

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
            R.id.btnExport -> checkPermissionsAndExport()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode != REQUEST_WRITE)
            return

        if(permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED)
            exportImage()
    }

    private fun checkPermissionsAndExport() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE
            )
        } else exportImage()
    }

    private fun setState(newColorState: ColorState) {
        colorState = newColorState

        curveGeneral.visibility = View.INVISIBLE
        curveRed.visibility = View.INVISIBLE
        curveGreen.visibility = View.INVISIBLE
        curveBlue.visibility = View.INVISIBLE

        val visibleCurve = when(newColorState) {
            ColorState.GENERAL -> curveGeneral
            ColorState.RED -> curveRed
            ColorState.GREEN -> curveGreen
            ColorState.BLUE -> curveBlue
        }

        visibleCurve.visibility = View.VISIBLE

        Timber.d("New newColorState: $newColorState")
    }

    private fun exportImage() = launchUICatching({
        val bitmap = bitmap ?: throw NullPointerException("Bitmap is null")

        val resolver = context?.contentResolver ?: throw NullPointerException("Cannot get content resolver duo to null context")
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageName ?: "curvedBitmap")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?:
                throw RuntimeException("Cannot insert bitmap, values=$values")

        val os = resolver.openOutputStream(uri) ?:
            throw RuntimeException("Cannot open output stream")

        withContext(Dispatchers.Default) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        }

        Timber.i("Image exported")
        withUI {
            Toast.makeText(context, "Image Exported", Toast.LENGTH_SHORT).show()
        }
    }, { _, t ->
        Timber.e(t, "Error exporting image")
    })

    private enum class ColorState {
        GENERAL,
        RED,
        GREEN,
        BLUE;
    }

    companion object {
        private const val RESIZE_FACTOR = 2
        private const val REQUEST_WRITE = 2
    }
}