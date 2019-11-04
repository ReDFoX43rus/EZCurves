package com.liberaid.ezcurves.ui.fragments.editfragment

import android.os.Bundle
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.liberaid.ezcurves.R
import com.liberaid.ezcurves.ui.BaseFragment
import com.liberaid.ezcurves.ui.FragmentId
import com.liberaid.ezcurves.ui.fragments.selectfragment.SelectFragment
import kotlinx.android.synthetic.main.fragment_edit.*

class EditFragment : BaseFragment() {
    override val fragmentId = FragmentId.EDIT_FRAGMENT

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val imagePath = arguments?.getString(SelectFragment.IMAGE_PATH_KEY) ?: return

        Glide.with(this)
            .load(imagePath)
            .centerCrop()
            .into(ivPreview)

        btnExport.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_editFragment_to_importFragment))
    }
}