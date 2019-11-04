package com.liberaid.ezcurves.ui.fragments.selectfragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liberaid.ezcurves.R

class RVPhotosAdapter(private val listener: IListener, private val layoutID: Int) : RecyclerView.Adapter<RVPhotosAdapter.ViewHolder>(){

    /**
     * Contains default callbacks
     * */
    interface IListener {
        fun getItemCount(): Int
        fun onBindViewOnPosition(view: IElementView, position: Int)
    }

    /**
     * Actually view interface in MVP pattern
     *
     * This interface should contains methods to update views
     * in recyclerView's elements
     * */
    interface IElementView {
        fun setupImage(path: String, onClick: () -> Unit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutID, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = listener.getItemCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = listener.onBindViewOnPosition(holder, position)

    /**
     * Basic implementation of viewHolder
     * */
    class ViewHolder(private val mainView: View) : RecyclerView.ViewHolder(mainView), IElementView {

        private val ivPhoto = mainView.findViewById<ImageView>(R.id.ivPhoto)

        override fun setupImage(path: String, onClick: () -> Unit) {
            Glide.with(mainView)
                .load(path)
                .centerCrop()
                .into(ivPhoto)

            ivPhoto.setOnClickListener { onClick() }
        }

    }
}