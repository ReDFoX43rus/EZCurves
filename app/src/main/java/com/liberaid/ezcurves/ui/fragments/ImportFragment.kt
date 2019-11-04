package com.liberaid.ezcurves.ui.fragments

import android.os.Bundle
import androidx.navigation.Navigation
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import kotlinx.android.synthetic.main.fragment_import.*

class ImportFragment : BaseFragment() {
    override val fragmentId = FragmentId.IMPORT_FRAGMENT

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnImport.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_importFragment_to_selectFragment))
    }
}