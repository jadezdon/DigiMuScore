package com.zhouppei.digimuscore.adapters

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zhouppei.digimuscore.R
import kotlin.math.max

class SwipeAndDragHandler(
    context: Context,
    private val actionCompletionContract: ActionCompletionContract
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT,
    ItemTouchHelper.LEFT
) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragDirs = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        val swipeDirs = ItemTouchHelper.LEFT
        return makeMovementFlags(dragDirs, swipeDirs)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        actionCompletionContract.onViewMoved(
            viewHolder.adapterPosition,
            target.adapterPosition
        )
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        actionCompletionContract.onViewSwiped(viewHolder.adapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        actionCompletionContract.clearView()
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                clearCanvas(c, itemView, dX)
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                return
            }

            // draw red delete background
            background.color = backgroundColor
            background.setBounds(
                max(itemView.left, itemView.right + dX.toInt()),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)


            if (deleteIcon != null) {
                deleteIcon.setTint(Color.WHITE)

                val intrinsicWidth = deleteIcon.intrinsicWidth
                val intrinsicHeight = deleteIcon.intrinsicHeight
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 4

                deleteIcon.setBounds(
                    itemView.right - deleteIconMargin / 2 - intrinsicWidth,
                    itemView.top + deleteIconMargin * 2,
                    itemView.right - deleteIconMargin / 2,
                    itemView.top + deleteIconMargin * 2 + intrinsicHeight
                )

                deleteIcon.draw(c)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, itemView: View, dX: Float) {
        c?.drawRect(
            itemView.right + dX,
            itemView.top.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat(),
            clearPaint
        )
    }

}

interface ActionCompletionContract {
    fun onViewMoved(oldPosition: Int, newPosition: Int)
    fun onViewSwiped(position: Int)
    fun clearView()
}
