package com.zhouppei.digimuscore.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zhouppei.digimuscore.R
import kotlinx.android.synthetic.main.item_music_note.view.*

class MusicNoteListAdapter(
    private val listItems: List<String>,
    private val clickListener: MusicNoteClickListener
) : ListAdapter<String, MusicNoteListAdapter.ViewHolder>(MusicNoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_note, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItems[position]
        holder.icon.text = item
        holder.icon.setOnClickListener { clickListener.onNoteClicked(item) }
    }

    override fun getItemCount(): Int = listItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.musicNote_button
    }
}

private class MusicNoteDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

interface MusicNoteClickListener {
    fun onNoteClicked(item: String)
}