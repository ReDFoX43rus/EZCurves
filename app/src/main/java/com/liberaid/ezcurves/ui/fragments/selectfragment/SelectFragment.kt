package com.liberaid.ezcurves.ui.fragments.selectfragment

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import kotlinx.android.synthetic.main.fragment_select.*
import timber.log.Timber

class SelectFragment : BaseFragment(), ISelectFragmentContract.IView, LoaderManager.LoaderCallbacks<Cursor> {
    override val fragmentId = FragmentId.SELECT_FRAGMENT

    private lateinit var rvAdapter: RVPhotosAdapter
    private lateinit var presenter: ISelectFragmentContract.IPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ)
        } else setup()
    }

    private fun setup() {
        presenter = SelectFragmentPresenter()
        rvAdapter = RVPhotosAdapter(presenter, R.layout.card_photo)
        presenter.attachView(this)

        rvPhotos.apply {
            adapter = rvAdapter
            setHasFixedSize(true)

            val spanCount = if(context != null){
                val widthDp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
                (widthDp / 80f).toInt()
            } else 4

            layoutManager = GridLayoutManager(context, spanCount)
        }
    }

    override fun initPhotosLoader() {
        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    override fun notifyRVAdapter() = rvAdapter.notifyDataSetChanged()

    override fun navigateToEditFragment(imagePath: String) {
        findNavController().navigate(R.id.action_selectFragment_to_editFragment, bundleOf(IMAGE_PATH_KEY to imagePath))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode != REQUEST_READ)
            return

        if(permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED)
            setup()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        Timber.d("Create cursor with projection: $projection")

        return CursorLoader(context!!, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        data ?: return

        val photoPaths = mutableListOf<String>()

        data.moveToFirst()

        do {
            photoPaths.add(data.getString(0))
        } while(data.moveToNext())

        Timber.d("Loaded photos: $photoPaths")
        presenter.onPhotosLoaded(photoPaths)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Timber.d("Loader reset $loader")
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    companion object {
        private const val REQUEST_READ = 1

        const val IMAGE_PATH_KEY = "imagePath"
    }
}