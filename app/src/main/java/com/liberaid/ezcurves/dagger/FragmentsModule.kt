package com.liberaid.ezcurves.dagger

import androidx.fragment.app.FragmentManager
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.ui.fragments.ImportFragment
import com.liberaid.ezcurves.ui.fragments.selectfragment.SelectFragment
import dagger.Module
import dagger.Provides

@Module
class FragmentsModule(private val fragmentManager: FragmentManager) {

    @Provides
    fun provideImportFragment(): ImportFragment = fragmentManager.provideFragment(FragmentId.IMPORT_FRAGMENT)

    @Provides
    fun provideSelectFragment(): SelectFragment = fragmentManager.provideFragment(FragmentId.SELECT_FRAGMENT)

}

private inline fun <reified T: BaseFragment> FragmentManager.provideFragment(fragmentId: FragmentId): T = findFragmentByTag(fragmentId.toString()) as? T ?: T::class.java.newInstance()
