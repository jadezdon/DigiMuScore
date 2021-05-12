package com.zhouppei.digimuscore.adapters

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.notation.DrawLayer
import com.zhouppei.digimuscore.utils.BitmapUtil

@BindingAdapter("imageFromUri")
fun bindImageFromUri(view: ImageView, imageUri: String?) {
    if (!imageUri.isNullOrEmpty()) {
        val bitmap = BitmapFactory.decodeFile(imageUri)
        view.setImageBitmap(bitmap)
    }
}

@BindingAdapter("colorString")
fun bindColorStringWithBackground(view: View, colorStr: String) {
    view.setBackgroundColor(Color.parseColor(colorStr))
}

@BindingAdapter("isFavorite")
fun bindIsFavorite(view: View, isFavorite: Boolean) {
    if (isFavorite) {
        (view as ImageView).setImageResource(R.drawable.ic_is_favorite)
    } else {
        (view as ImageView).setImageResource(R.drawable.ic_not_favorite)
    }
}

@BindingAdapter("drawLayerToBitmap")
fun bindDrawLayerToBitmap(view: ImageView, drawLayers: MutableList<DrawLayer>?) {
    drawLayers?.let {
        val bitmap = BitmapUtil.drawLayerToBitmap(it.first())
        view.setImageBitmap(bitmap)
    }
}

@BindingAdapter("isVisible")
fun bindIsVisible(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.VISIBLE else View.GONE
}

//@BindingAdapter(value = ["currentPageNumber", "maxPageNumber"])
//fun bindPrevAndNextPageFabVisibility(view: View, currentPageNumber: Int, totalPageNumber: Int) {
//    view.visibility = if (currentPageNumber > 0 || currentPageNumber + 1 < totalPageNumber) {
//        View.GONE
//    } else {
//        View.VISIBLE
//    }
//}