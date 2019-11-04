package com.liberaid.ezcurves.ui.fragments.selectfragment

import com.liberaid.ezcurves.util.BasePresenter
import timber.log.Timber

class SelectFragmentPresenter : BasePresenter<ISelectFragmentContract.IView>(), ISelectFragmentContract.IPresenter {

    private val photoPaths = mutableListOf<String>()

    override fun onViewAttached(view: ISelectFragmentContract.IView) {
        photoPaths.clear()

        view.notifyRVAdapter()
        view.initPhotosLoader()
    }

    override fun onPhotosLoaded(paths: List<String>) {
        photoPaths.clear()
        photoPaths.addAll(paths)

        view?.notifyRVAdapter()
    }

    override fun getItemCount() = photoPaths.size

    override fun onBindViewOnPosition(view: RVPhotosAdapter.IElementView, position: Int) {
        view.setupImage(photoPaths[position]) {
            Timber.d("Image clicked at position $position")
        }
    }
}