package com.liberaid.ezcurves.dagger

import androidx.fragment.app.FragmentManager
import com.liberaid.ezcurves.ui.fragments.ImportFragment
import com.liberaid.ezcurves.ui.fragments.editfragment.EditFragment
import com.liberaid.ezcurves.ui.fragments.selectfragment.SelectFragment
import dagger.Component

@Component(modules = [FragmentsModule::class])
interface FragmentsComponent {

    fun getImportFragment(): ImportFragment
    fun getSelectFragment(): SelectFragment
    fun getEditFragment(): EditFragment

    companion object {
        fun instance(fragmentManager: FragmentManager): FragmentsComponent =
            DaggerFragmentsComponent
                .builder()
                .fragmentsModule(FragmentsModule(fragmentManager))
                .build()
    }

}