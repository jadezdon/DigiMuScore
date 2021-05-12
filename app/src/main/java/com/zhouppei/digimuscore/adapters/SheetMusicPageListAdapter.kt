package com.zhouppei.digimuscore.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import com.zhouppei.digimuscore.databinding.ItemSheetMusicPageBinding
import com.zhouppei.digimuscore.ui.sheetmusic.SheetMusicFragmentDirections

class SheetMusicPageListAdapter :
    ListAdapter<SheetMusicPage, SheetMusicPageListAdapter.PageViewHolder>(
        SheetMusicPageDiffCallback()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageViewHolder {
        return PageViewHolder(
            ItemSheetMusicPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PageViewHolder(
        private val binding: ItemSheetMusicPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.setClickListener {
                binding.sheetMusicPage?.let { sheetMusicPage ->
                    navigateToSheetMusicNotation(sheetMusicPage, it)
                }
            }
        }

        private fun navigateToSheetMusicNotation(sheetMusicPage: SheetMusicPage, view: View) {
            val direction =
                SheetMusicFragmentDirections.actionSheetMusicFragmentToSheetMusicPageFragment(
                    sheetMusicPage.sheetMusicId,
                    sheetMusicPage.id
                )
            Navigation.findNavController(view).navigate(direction)
        }

        fun bind(item: SheetMusicPage) {
            binding.apply {
                sheetMusicPage = item
                executePendingBindings()
            }
        }
    }
}

private class SheetMusicPageDiffCallback : DiffUtil.ItemCallback<SheetMusicPage>() {
    override fun areItemsTheSame(oldItem: SheetMusicPage, newItem: SheetMusicPage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SheetMusicPage, newItem: SheetMusicPage): Boolean {
        return oldItem == newItem
    }
}