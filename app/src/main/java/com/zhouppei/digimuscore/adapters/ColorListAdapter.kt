package com.zhouppei.digimuscore.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zhouppei.digimuscore.R
import kotlinx.android.synthetic.main.item_color_button.view.*

class ColorListAdapter(
    private val clickListener: ColorClickListener
) : ListAdapter<String, ColorListAdapter.ViewHolder>(ColorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color_button, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.button.apply {
            setBackgroundColor(if (item.isBlank()) Color.LTGRAY else Color.parseColor(item))
            isEnabled = item.isNotBlank()
            setOnClickListener { clickListener.onColorClick(item) }
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.color_button
    }
}

private class ColorDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

interface ColorClickListener {
    fun onColorClick(color: String)
}