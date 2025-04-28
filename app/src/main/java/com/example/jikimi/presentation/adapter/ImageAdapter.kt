package com.example.jikimi.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jikimi.databinding.ItemImageBinding

// 이미지 업로드를 위한 어댑터
class ImageAdapter(
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 로컬 Uri 이미지
    private val images = mutableListOf<Uri>()
    // Firebase URL 이미지 (수정 모드에서 사용)
    private val firebaseImages = mutableListOf<String>()

    companion object {
        private const val VIEW_TYPE_LOCAL = 0
        private const val VIEW_TYPE_FIREBASE = 1
    }

    fun addImage(uri: Uri) {
        if (images.size < 5) { // 최대 5장 제한
            images.add(uri)
            notifyItemInserted(images.size - 1)
        }
    }

    fun addFirebaseImage(url: String) {
        if (getItemCount() < 5) { // 최대 5장 제한
            firebaseImages.add(url)
            notifyItemInserted(getItemCount() - 1)
        }
    }


    fun removeImage(position: Int) {
        val firebaseSize = firebaseImages.size

        if (position < firebaseSize) {
            // Firebase 이미지 삭제(수정모드에서 사용)
            firebaseImages.removeAt(position)
        } else {
            // 로컬 이미지 삭제
            images.removeAt(position - firebaseSize)
        }

        notifyItemRemoved(position)
        notifyItemRangeChanged(position, getItemCount())
    }

    fun getImages(): List<Uri> = images
    fun getFirebaseImages(): List<String> = firebaseImages

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

    override fun getItemCount() = firebaseImages.size + images.size

    override fun getItemViewType(position: Int): Int {
        return if (position < firebaseImages.size) {
            VIEW_TYPE_FIREBASE
        } else {
            VIEW_TYPE_LOCAL
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LocalImageViewHolder -> {
                val localPosition = position - firebaseImages.size
                holder.bind(images[localPosition])
            }
            is FirebaseImageViewHolder -> {
                holder.bind(firebaseImages[position])
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

        fun bind(uri: Uri) {
            Glide.with(binding.root.context)
                .load(uri)
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

        fun bind(url: String) {
            Glide.with(binding.root.context)
                .load(url)
                .centerCrop()
                .into(binding.ivImage)
        }
    }
}