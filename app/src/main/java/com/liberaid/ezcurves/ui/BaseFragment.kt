package com.liberaid.ezcurves.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.liberaid.ezcurves.ui.FragmentId

abstract class BaseFragment : Fragment(){
    abstract val fragmentId: FragmentId

    val fragmentTag: String
        get() = fragmentId.toString()

    protected var mainView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(fragmentId.layoutId, container, false)
        return mainView
    }
}