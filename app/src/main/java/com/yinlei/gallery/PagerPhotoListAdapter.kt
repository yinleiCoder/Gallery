package com.yinlei.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.pager_photo_view.view.*

/**
 * ViewPager的适配器
 */
class PagerPhotoListAdapter : ListAdapter<PhotoItem, PagerPhotoListAdapter.PagerPhotoViewHolder>(DiffCallback) {

    object DiffCallback:DiffUtil.ItemCallback<PhotoItem>(){
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

    }

    /**
     * 点击事件写在onCreateViewHolder里，避免性能的开销。
     * 一般不要写在onBindViewHolder里
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerPhotoViewHolder {
        LayoutInflater.from(parent.context).inflate(R.layout.pager_photo_view,parent,false).apply {
            return PagerPhotoViewHolder(this)
        }
    }

    override fun onBindViewHolder(holder: PagerPhotoViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(getItem(position).previewUrl)
            .placeholder(R.drawable.photo_placeholder)
            .into(holder.itemView.pagerPhoto)
    }

    inner class  PagerPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}