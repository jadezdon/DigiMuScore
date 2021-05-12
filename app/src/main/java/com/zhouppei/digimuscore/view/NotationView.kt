package com.zhouppei.digimuscore.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.notation.DrawLayer
import com.zhouppei.digimuscore.notation.DrawText
import com.zhouppei.digimuscore.notation.TextStyle
import com.zhouppei.digimuscore.utils.FontUtil


class NotationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private var mImageView: ImageView = ImageView(getContext())
    private var mPaintView: PaintView = PaintView(getContext())
    private val mImgViewId = 1
    private val mPaintViewId = 2

    private var mDrawingMode = DrawingMode.SELECT

    private val mEditTexts = mutableListOf<EditText>()

    private val mAddedTexts: MutableList<EditText> = mutableListOf()
    private val mDeletedTexts: MutableList<EditText> = mutableListOf()

    private var mTextPaint = Paint()
    private var mTextFontFamily = "OpenSans"
    private var mTextStyle = TextStyle.NORMAL

    private var mTouchedText: TextView? = null
    private var mTouchedX = -1f
    private var mTouchedY = -1f
    private var mIsDragging = false

    private lateinit var mNotationViewListener: NotationViewListener

    companion object {
        private val TAG = NotationView::class.qualifiedName
    }

    init {
        mImageView.id = mImgViewId
        mImageView.adjustViewBounds = true
        mImageView.isFocusable = true
        mImageView.isFocusableInTouchMode = true
        val imgViewParam = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imgViewParam.addRule(CENTER_IN_PARENT, TRUE)

        mImageView.setOnFocusChangeListener { v, hasFocus ->
            Log.d(TAG, "----------- imageview focus = $hasFocus")
        }

        mPaintView.id = mPaintViewId
        mPaintView.visibility = GONE
        val paintViewParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        paintViewParams.addRule(CENTER_IN_PARENT, TRUE)
        paintViewParams.addRule(ALIGN_TOP, mImgViewId)
        paintViewParams.addRule(ALIGN_BOTTOM, mImgViewId)
        paintViewParams.addRule(ALIGN_LEFT, mImgViewId)
        paintViewParams.addRule(ALIGN_RIGHT, mImgViewId)

        addView(mImageView, imgViewParam)
        addView(mPaintView, paintViewParams)

        setTypeface()
    }

    fun setImageBitmap(bitmap: Bitmap) {
        mImageView.setImageBitmap(bitmap)
    }

    fun setNotationViewListener(listener: NotationViewListener) {
        mNotationViewListener = listener
    }

    fun setDrawLayer(drawLayer: DrawLayer) {
        mPaintView.setDraws(drawLayer.drawPaths)
        mEditTexts.forEach { removeView(it) }
        mEditTexts.clear()
        drawLayer.drawTexts.forEach { drawText ->
            createEditText(drawText, drawText.coordinateX, drawText.coordinateY).let { editText ->
                editText.setText(drawText.content)
                mEditTexts.add(editText)
                addView(editText)
                editText.clearFocus()
            }
        }
        mImageView.requestFocus()
        Log.d(TAG, "setDrawLayer: added view number = ${childCount - 2}")

        // reset variables
        resetTouchedText()
        mAddedTexts.clear()
        mDeletedTexts.clear()
    }

    /**
     * @param drawLayer save drawn objects to it
     */
    fun getDrawLayer(drawLayer: DrawLayer) {
        drawLayer.apply {
            drawPaths.clear()
            drawTexts.clear()
            drawPaths.addAll(mPaintView.getDrawPaths())
            mEditTexts.forEach {
                if (it.text.toString().isNotBlank()) {
                    val drawText = DrawText(it.text.toString(), it.x, it.y).apply {
                        size = it.paint.textSize / resources.displayMetrics.density
                        textColor = it.paint.color
                        typefaceName = FontUtil.getTypefaceName(it.typeface)
                        density = resources.displayMetrics.density
                    }
                    drawTexts.add(drawText)
                }
            }
        }
    }

    fun setDrawingMode(drawingMode: DrawingMode) {
        mDrawingMode = drawingMode
        mPaintView.setDrawingMode(drawingMode)
        resetTouchedText()

        if (mDrawingMode != DrawingMode.TEXT) {
            mImageView.requestFocus()
        }
        if (mDrawingMode == DrawingMode.DRAW || mDrawingMode == DrawingMode.ERASER) {
            bringChildToFront(mPaintView)
        }

        mEditTexts.forEach {
            setEnableEditText(it, mDrawingMode == DrawingMode.TEXT)
        }
    }

    private fun resetTouchedText() {
        mTouchedText?.let {
            it.background = null
        }
        mTouchedText = null
        if (this::mNotationViewListener.isInitialized) {
            mNotationViewListener.onTextTouched(mTouchedText)
        }
        mIsDragging = false
    }

    private fun setEnableEditText(editText: EditText, enable: Boolean) {
        editText.isFocusable = enable
        editText.isFocusableInTouchMode = enable
    }

    fun getPaintView(): PaintView {
        return mPaintView
    }

    fun undo(): Boolean {
        mPaintView.undo()?.let { action ->
            when (action) {
                UndoAction.ADD_TEXT -> {
                    mAddedTexts.removeAt(mAddedTexts.size - 1).let {
                        mEditTexts.remove(it)
                        removeView(it)
                    }
                }
                UndoAction.REMOVE_TEXT -> {
                    mDeletedTexts.removeAt(mDeletedTexts.size - 1).let {
                        mEditTexts.add(it)
                        addView(it)
                    }
                }
                else -> {
                }
            }
        }
        return false
    }

    fun setTextSize(size: Float) {
        mTextPaint.textSize = size
    }

    fun changeTextSize(size: Int) {
        mTouchedText?.let {
            it.textSize = size.toFloat()
        }
    }

    fun setTextColor(color: Int) {
        mTextPaint.color = color
    }

    fun setTextFontFamily(value: String) {
        mTextFontFamily = value
        setTypeface()
    }

    fun setTextStyle(style: TextStyle) {
        mTextStyle = style
        setTypeface()
    }

    private fun setTypeface() {
        val typefaceName = "${mTextFontFamily}-${mTextStyle.str}.ttf"
        mTextPaint.typeface = FontUtil.getTypeface(typefaceName)
    }

    /*
        handle edittext dragging action in onInterceptTouchEvent method, because
        it is excuted before edittext click events
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { event ->
            if (event.pointerCount == 2) return false

            if (mDrawingMode != DrawingMode.DRAW && mDrawingMode != DrawingMode.ERASER) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        resetTouchedText()

                        getTouchedText(event.x, event.y)?.let {
                            mTouchedText = it
                            it.background = ContextCompat.getDrawable(context, R.drawable.text_box_background)
                            if (this::mNotationViewListener.isInitialized) {
                                mNotationViewListener.onTextTouched(mTouchedText)
                            }
                        }
                        mTouchedX = event.x
                        mTouchedY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mTouchedText?.let { editText ->
                            val dx = event.x - mTouchedX
                            val dy = event.y - mTouchedY
                            editText.x += dx
                            editText.y += dy
                            mIsDragging = true
                        }
                        mTouchedX = event.x
                        mTouchedY = event.y
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        mIsDragging = false
                    }
                    else -> {
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun getTouchedText(x: Float, y: Float): EditText? {
        for (editText in mEditTexts) {
            val textBounds = RectF(
                editText.x,
                editText.y,
                editText.x + editText.width,
                editText.y + editText.height
            )
            if (textBounds.contains(x, y)) return editText
        }
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { event ->
            if (event.pointerCount == 2) return false

            if (event.action == MotionEvent.ACTION_DOWN) {
                if (mDrawingMode == DrawingMode.TEXT) {
                    createEditText(null, event.x, event.y).let {
                        mEditTexts.add(it)
                        addView(it)
                        it.requestFocus()

                        mAddedTexts.add(it)
                        mPaintView.addLastAction(UndoAction.ADD_TEXT)
                    }
                    return true
                } else if (mDrawingMode == DrawingMode.SELECT) {
                    mImageView.requestFocus()
                    resetTouchedText()
                }
            }
        }
        return false
    }

    fun addMusicNote(musicNote: DrawText) {
        createEditText(musicNote).let {
            it.setText(musicNote.content)
            mEditTexts.add(it)
            addView(it)

            mAddedTexts.add(it)
            mPaintView.addLastAction(UndoAction.ADD_TEXT)
            setEnableEditText(it, false)
        }
    }

    private fun createEditText(drawText: DrawText?, posX: Float = 0f, posY: Float = 0f): EditText {
        val textBox = EditText(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            x = drawText?.coordinateX ?: posX
            y = drawText?.coordinateY ?: posY
            isFocusable = true
            isFocusableInTouchMode = true
            isLongClickable = true
            background = null
            minWidth = 10
            setPadding(0, 10, 0, 10)

            setTextColor(drawText?.textColor ?: mTextPaint.color)
            textSize = drawText?.size ?: mTextPaint.textSize

            typeface = if (drawText == null) mTextPaint.typeface else FontUtil.getTypeface(drawText.typefaceName)
        }

        textBox.let {
            it.setOnClickListener(null)

            it.setOnFocusChangeListener { v, hasFocus ->
                Log.d(TAG, "----------------- edittext focus = $hasFocus")
                if (!hasFocus) {
                    if (it.text.toString().isBlank()) {
                        if (mEditTexts.remove(it)) {
                            mDeletedTexts.add(it)
                            mPaintView.addLastAction(UndoAction.REMOVE_TEXT)
                        }
                        removeView(it)
                    } else if (mDrawingMode == DrawingMode.SELECT) {
                        setEnableEditText(it, false)
                    }
                    it.background = null
                    hideKeyboard(context, v)
                } else {
                    it.background = ContextCompat.getDrawable(context, R.drawable.text_box_background)
                    showKeyboard(context, v)
                    it.setSelection(it.text.length)
                }
            }

            it.setOnLongClickListener { view ->
                Log.d(TAG, "----------------- long click")
                if (mIsDragging) return@setOnLongClickListener false

                if (mDrawingMode == DrawingMode.SELECT) {
                    setEnableEditText(it, true)
                    view.requestFocus()
                }
                true
            }
        }
        return textBox
    }

    // https://rmirabelle.medium.com/close-hide-the-soft-keyboard-in-android-db1da22b09d2
    private fun hideKeyboard(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }
}

interface NotationViewListener {
    fun onTextTouched(textView: TextView?)
}