package com.liberaid.ezcurves.ui.fragments

import android.os.Bundle
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.util.safeTransaction
import kotlinx.android.synthetic.main.fragment_import.*

class ImportFragment : BaseFragment() {
    override val fragmentId = FragmentId.IMPORT_FRAGMENT

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnImport.setOnClickListener {
            childFragmentManager.safeTransaction {
                val selectFragment = SelectFragment()

                add(R.id.clContainer, selectFragment, selectFragment.fragmentTag)
                addToBackStack(selectFragment.fragmentTag)
            }
        }
    }
}