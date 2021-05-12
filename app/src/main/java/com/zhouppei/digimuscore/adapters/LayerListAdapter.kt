package com.zhouppei.digimuscore.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.notation.DrawLayer
import com.zhouppei.digimuscore.utils.BitmapUtil
import kotlinx.android.synthetic.main.item_layer.view.*

class LayerListAdapter(
    private val pageWidth: Int,
    private val pageHeight: Int,
    private var selectedLayerPos: Int,
    private val clickListener: LayerClickListener
) : ListAdapter<DrawLayer, LayerListAdapter.ViewHolder>(DrawLayerDiffCallback()) {


    companion object {
        private val TAG = LayerListAdapter::class.qualifiedName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layer, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.layerImageView.let {
            item.pageWidth = pageWidth
            item.pageHeight = pageHeight
            val bitmap = BitmapUtil.drawLayerToBitmap(item)

            it.setImageBitmap(bitmap)
            it.setOnClickListener {
                selectedLayerPos = position
                notifyDataSetChanged()
                clickListener.onClick(position)
            }
        }

        holder.layerName.apply {
            text = item.name
            setOnLongClickListener {
                clickListener.onLongClick(position)
                true
            }
        }
        holder.layerVisible.visibility = if (position == selectedLayerPos) View.VISIBLE else View.INVISIBLE
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layerImageView: ImageView = itemView.layer_imageView
        val layerName: TextView = itemView.layer_name
        val layerVisible: ImageView = itemView.layer_visible
    }
}

private class DrawLayerDiffCallback : DiffUtil.ItemCallback<DrawLayer>() {
    private val gson = Gson()
    override fun areItemsTheSame(oldItem: DrawLayer, newItem: DrawLayer): Boolean {
        return (oldItem.name == newItem.name && oldItem.layerOrder == newItem.layerOrder)
    }

    override fun areContentsTheSame(oldItem: DrawLayer, newItem: DrawLayer): Boolean {
        return gson.toJson(oldItem) == gson.toJson(newItem)
    }
}

interface LayerClickListener {
    fun onClick(position: Int)
    fun onLongClick(position: Int)
}