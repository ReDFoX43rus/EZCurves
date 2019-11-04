package com.liberaid.ezcurves.ui.fragments.selectfragment

import com.liberaid.ezcurves.util.BasePresenter

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
        val path = photoPaths[position]
        view.setupImage(path) {
            this.view?.navigateToEditFragment(path)
        }
    }
}