package com.zhouppei.digimuscore.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zhouppei.digimuscore.data.models.SheetMusic
import com.zhouppei.digimuscore.databinding.ItemSheetMusicBinding
import com.zhouppei.digimuscore.ui.home.HomeFragmentDirections

class SheetMusicListAdapter(private val listener: ItemSheetMusicClickListener) :
    ListAdapter<SheetMusic, SheetMusicListAdapter.ViewHolder>(SheetMusicDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSheetMusicBinding.inflate(
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
        private val binding: ItemSheetMusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener {
                binding.sheetMusic?.let { sheetMusic ->
                    navigateToSheetMusic(sheetMusic, it)
                }
            }

            binding.setLongClickListener {
                binding.sheetMusic?.let { sheetmusic ->
                    listener.onLongClick(sheetmusic)
                }
                true
            }

            binding.buttonFavorite.setOnClickListener {
                binding.sheetMusic?.let {
                    listener.onFavoriteClicked(it)
                    notifyItemChanged(adapterPosition)
                }
            }
        }

        private fun navigateToSheetMusic(sheetMusic: SheetMusic, view: View) {
            val direction = HomeFragmentDirections.actionHomeFragmentToSheetMusicFragment(
                sheetMusic.id
            )
            Navigation.findNavController(view).navigate(direction)
        }

        fun bind(item: SheetMusic) {
            binding.apply {
                sheetMusic = item
                executePendingBindings()
            }
        }
    }

    companion object {
        private val TAG = SheetMusicListAdapter::class.qualifiedName
    }
}

private class SheetMusicDiffCallback : DiffUtil.ItemCallback<SheetMusic>() {
    override fun areItemsTheSame(oldItem: SheetMusic, newItem: SheetMusic): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SheetMusic, newItem: SheetMusic): Boolean {
        return oldItem == newItem
    }
}

interface ItemSheetMusicClickListener {
    fun onFavoriteClicked(sheetMusic: SheetMusic)
    fun onLongClick(sheetMusic: SheetMusic)
}

