package com.socialgateway.socialgateway

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
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val socialApp = SocialApps[position]
        val imageView = convertView as? ImageView ?: ImageView(context).apply {
            adjustViewBounds = true // otherwise icons will be far from each other
        }

        val isInstalled =
            context.packageManager.getLaunchIntentForPackage(socialApp.packageName) != null

        return imageView.apply {
            setImageResource(socialApp.imageId)
            scaleX = 0.9f
            scaleY = 0.9f
            if (!isInstalled) {
                alpha = 0.5f
            }
            this.setOnClickListener(null)
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.scaleY = 1f
                        v.scaleX = 1f
                        v.performClick()
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