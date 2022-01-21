package com.socialgateway.socialgateway

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView


class SocialAppAdapter(
    private val context: Context,
    private val onClick: (Context, SocialApp) -> Unit
) : BaseAdapter() {
    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val socialApp = SocialApps[position]
        val imageView = convertView as? ImageView ?: ImageView(context).apply {
            adjustViewBounds = true // otherwise icons will be far from each other
        }

        return imageView.apply {
            setImageResource(socialApp.imageId)
            scaleX = 0.9f
            scaleY = 0.9f
            setOnClickListener {
                onClick(context, socialApp)
            }
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.scaleY = 1f
                        v.scaleX = 1f
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