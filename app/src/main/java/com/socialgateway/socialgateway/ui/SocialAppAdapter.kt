package com.socialgateway.socialgateway.ui

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.socialgateway.socialgateway.data.model.SocialApp
import com.socialgateway.socialgateway.data.model.SocialApps


class SocialAppAdapter(
    private val context: Context,
    private val onClick: (Context, SocialApp) -> Unit
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val socialApp = SocialApps[position]
        val imageView = convertView as? ImageView ?: ImageView(context).apply {
            adjustViewBounds = true // otherwise icons will be far from each other
        }

        return imageView.apply {
            setImageResource(socialApp.icon)
            scaleX = 0.9f
            scaleY = 0.9f
            if (!socialApp.isInstalled()) {
                alpha = 0.5f
            }
            this.setOnClickListener(null)
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.scaleY = 1f
                        v.scaleX = 1f
                        v.performClick()
                        if (socialApp.isInstalled())
                            onClick(context, socialApp)
                    }
                    MotionEvent.ACTION_UP -> {
                        v.scaleY = 0.9f
                        v.scaleX = 0.9f
                    }
                }
                false
            }
        }
    }

    override fun getItem(position: Int): Any {
        return SocialApps[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return SocialApps.size
    }
}