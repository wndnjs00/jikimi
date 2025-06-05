package com.myapp.jikimi.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myapp.jikimi.databinding.ItemImageBinding

// 이미지 업로드를 위한 어댑터 (DiffUtil 적용)
class ImageAdapter(
    private val onDeleteClick: (Int) -> Unit
) : ListAdapter<ImageAdapter.ImageItem, RecyclerView.ViewHolder>(ImageDiffUtil) {

    // 이미지 아이템을 나타내는 sealed class
    sealed class ImageItem {
        data class LocalImage(val uri: Uri) : ImageItem()
        data class FirebaseImage(val url: String) : ImageItem()
    }

    companion object {
        private const val VIEW_TYPE_LOCAL = 0
        private const val VIEW_TYPE_FIREBASE = 1
        private const val MAX_IMAGES = 5

        // DiffUtil 구현
        object ImageDiffUtil : DiffUtil.ItemCallback<ImageItem>() {
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
    }

    fun addImage(uri: Uri) {
        if (currentList.size < MAX_IMAGES) {
            val newList = currentList.toMutableList().apply {
                add(ImageItem.LocalImage(uri))
            }
            submitList(newList)
        }
    }

    fun addFirebaseImage(url: String) {
        if (currentList.size < MAX_IMAGES) {
            val newList = currentList.toMutableList().apply {
                add(ImageItem.FirebaseImage(url))
            }
            submitList(newList)
        }
    }

    fun removeImage(position: Int) {
        if (position in 0 until currentList.size) {
            val newList = currentList.toMutableList().apply {
                removeAt(position)
            }
            submitList(newList)
        }
    }

    fun getImages(): List<Uri> = currentList.filterIsInstance<ImageItem.LocalImage>().map { it.uri }

    fun getFirebaseImages(): List<String> = currentList.filterIsInstance<ImageItem.FirebaseImage>().map { it.url }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return when (viewType) {
            VIEW_TYPE_LOCAL -> LocalImageViewHolder(binding)
            VIEW_TYPE_FIREBASE -> FirebaseImageViewHolder(binding)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ImageItem.LocalImage -> VIEW_TYPE_LOCAL
            is ImageItem.FirebaseImage -> VIEW_TYPE_FIREBASE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is LocalImageViewHolder -> {
                holder.bind(item as ImageItem.LocalImage)
            }
            is FirebaseImageViewHolder -> {
                holder.bind(item as ImageItem.FirebaseImage)
            }
        }
    }

    inner class LocalImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(position)
                }
            }
        }

        fun bind(item: ImageItem.LocalImage) {
            Glide.with(binding.root.context)
                .load(item.uri)
                .centerCrop()
                .into(binding.ivImage)
        }
    }

    inner class FirebaseImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(position)
                }
            }
        }

        fun bind(item: ImageItem.FirebaseImage) {
            Glide.with(binding.root.context)
                .load(item.url)
                .centerCrop()
                .into(binding.ivImage)
        }
    }
}