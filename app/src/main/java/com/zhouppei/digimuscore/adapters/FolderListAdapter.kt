package com.zhouppei.digimuscore.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zhouppei.digimuscore.data.models.Folder
import com.zhouppei.digimuscore.databinding.ItemFolderBinding

class FolderListAdapter(private val folderClickListener: FolderClickListener) :
    ListAdapter<Folder, FolderListAdapter.ViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener {
                binding.folder?.let { folder ->
                    folderClickListener.onFolderClick(folder)
                }
            }
        }

        fun bind(item: Folder) {
            binding.apply {
                folder = item
                executePendingBindings()
            }
        }
    }
}

private class FolderDiffCallback : DiffUtil.ItemCallback<Folder>() {
    override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem == newItem
    }
}

interface FolderClickListener {
    fun onFolderClick(folder: Folder)
}

