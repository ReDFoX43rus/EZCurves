package com.liberaid.ezcurves.util

interface IMVPView

interface IMVPPresenter <V : IMVPView> {
    fun attachView(view: V)
    fun detachView()
}

open class BasePresenter <V : IMVPView> : IMVPPresenter<V> {

    protected var view: V? = null

    override fun attachView(view: V) {
        this.view = view

        onViewAttached(view)
    }

    override fun detachView() {
        view = null

        onViewDetached()
    }

    protected open fun onViewAttached(view: V) {}
    protected open fun onViewDetached() {}
}