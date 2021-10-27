package com.example.socialgateway

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class SocialAppAdapter(
    private val context: Context,
    private val onClick: (Context, SocialApp) -> Unit
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val socialApp = SocialApps[position]
        val imageView = convertView as? ImageView ?: ImageView(context).apply {
            // otherwise icons will be far from each other
            adjustViewBounds = true
        }

        return imageView.apply {
            setImageResource(socialApp.imageId)
            setOnClickListener { onClick(context, socialApp) }
        }
    }

    override fun getItem(position: Int): Any {
        return SocialApps[position]
    }

    override fun getItemId(position: Int): Long {
        // why toLong()?
        return position.toLong()
    }

    override fun getCount(): Int {
        return SocialApps.size
    }
}