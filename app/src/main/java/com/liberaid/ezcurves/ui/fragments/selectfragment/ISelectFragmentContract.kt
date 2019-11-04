package com.liberaid.ezcurves.ui.fragments.selectfragment

import com.liberaid.ezcurves.util.IMVPPresenter
import com.liberaid.ezcurves.util.IMVPView

interface ISelectFragmentContract {

    interface IView : IMVPView {
        fun notifyRVAdapter()
        fun initPhotosLoader()

        interface IViewListener {
            fun onPhotosLoaded(paths: List<String>)
        }
    }

    interface IPresenter : IMVPPresenter<IView>, IView.IViewListener, RVPhotosAdapter.IListener

}