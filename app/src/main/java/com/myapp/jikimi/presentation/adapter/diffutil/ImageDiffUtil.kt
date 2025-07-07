package com.myapp.jikimi.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.myapp.jikimi.presentation.adapter.ImageAdapter.ImageItem

class ImageDiffUtil : DiffUtil.ItemCallback<ImageItem>() {
    override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
        return when {
            oldItem is ImageItem.LocalImage && newItem is ImageItem.LocalImage ->
                oldItem.uri == newItem.uri
            oldItem is ImageItem.FirebaseImage && newItem is ImageItem.FirebaseImage ->
                oldItem.url == newItem.url
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }
}