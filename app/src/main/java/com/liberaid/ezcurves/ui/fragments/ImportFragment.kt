package com.liberaid.ezcurves.ui.fragments

import android.os.Bundle
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.dagger.FragmentsComponent
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.util.safeTransaction
import kotlinx.android.synthetic.main.fragment_import.*

class ImportFragment : BaseFragment() {
    override val fragmentId = FragmentId.IMPORT_FRAGMENT

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val selectFragment = FragmentsComponent
            .instance(childFragmentManager)
            .getSelectFragment()

        btnImport.setOnClickListener {
            childFragmentManager.safeTransaction {
                add(R.id.clContainer, selectFragment, selectFragment.fragmentTag)
                addToBackStack(selectFragment.fragmentTag)
            }
        }
    }
}