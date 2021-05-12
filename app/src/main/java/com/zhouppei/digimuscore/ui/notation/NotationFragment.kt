package com.zhouppei.digimuscore.ui.notation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import dagger.hilt.android.AndroidEntryPoint
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.adapters.ActionCompletionContract
import com.zhouppei.digimuscore.adapters.LayerClickListener
import com.zhouppei.digimuscore.adapters.LayerListAdapter
import com.zhouppei.digimuscore.adapters.SwipeAndDragHandler
import com.zhouppei.digimuscore.databinding.FragmentNotationBinding
import com.zhouppei.digimuscore.notation.*
import com.zhouppei.digimuscore.ui.notation.dialogs.AddLayerDialog
import com.zhouppei.digimuscore.ui.notation.dialogs.AddLayerDialogListener
import com.zhouppei.digimuscore.utils.BitmapUtil
import com.zhouppei.digimuscore.view.*
import kotlinx.android.synthetic.main.fragment_notation.*
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class NotationFragment : Fragment() {

    private val mArgs: NotationFragmentArgs by navArgs()

    @Inject
    lateinit var mViewModelFactory: NotationViewModel.NotationAssistedFactory
    private val mViewModel: NotationViewModel by viewModels {
        NotationViewModel.provideFactory(
            mViewModelFactory,
            mArgs.sheetMusicPageId
        )
    }
    private val mSharedViewModel by activityViewModels<SharedViewModel>()

    private lateinit var mEditor: Editor
    private lateinit var mPageBitmap: Bitmap

    private lateinit var mActionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var mLayerListAdapter: LayerListAdapter

    private lateinit var mAddLayerDialog: AddLayerDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentNotationBinding>(
            inflater,
            R.layout.fragment_notation,
            container,
            false
        ).apply {
            viewmodel = mSharedViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel.sheetMusicPage.observe(
            viewLifecycleOwner,
            Observer { sheetMusicImg ->

                mSharedViewModel.pageNumber.postValue(sheetMusicImg.pageNumber)

                val bitmap = BitmapFactory.decodeFile(sheetMusicImg.contentUri)
                val pageSize = BitmapUtil.getPageSize(bitmap)
                mPageBitmap = Bitmap.createScaledBitmap(bitmap, pageSize.first, pageSize.second, true)
                notation_notationView.setImageBitmap(mPageBitmap)

                Log.d(TAG, "[IMG] w: ${mPageBitmap.width}, h: ${mPageBitmap.height}")

                val currentLayerIndex =
                    if (mViewModel.currentLayerIndex >= 0 && mViewModel.currentLayerIndex < sheetMusicImg.drawLayers.size)
                        mViewModel.currentLayerIndex else 0
                mViewModel.currentLayer.postValue(sheetMusicImg.drawLayers[currentLayerIndex])
                mViewModel.currentLayerIndex = currentLayerIndex
                setupLayerSideBar(pageSize.first, pageSize.second, sheetMusicImg.drawLayers, currentLayerIndex)
                notation_bottomNav.selectedItemId = R.id.notation_action_select
            }
        )

        setupEditor()
        setupTopBarToolsListeners()
        setupBottomBarToolsListeners()
        setupLayerSideBarListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.currentLayer.value?.let {
            mEditor.getDrawLayer(it)
        }
    }

    private fun setupLayerSideBar(
        pageWidth: Int,
        pageHeight: Int,
        drawLayers: MutableList<DrawLayer>,
        currentLayerIndex: Int
    ) {
        val layerClickListener = object : LayerClickListener {
            override fun onClick(position: Int) {
                mViewModel.currentLayer.postValue(drawLayers[position])
                mViewModel.currentLayerIndex = position
                notation_drawerLayout.closeDrawer(Gravity.RIGHT)
            }

            override fun onLongClick(position: Int) {
                mAddLayerDialog = AddLayerDialog(
                    requireContext(),
                    drawLayers[position].name,
                    object : AddLayerDialogListener {
                        override fun onSaveButtonClicked(layerName: String) {
                            mViewModel.sheetMusicPage.value?.let {
                                it.drawLayers[position].name = layerName
                                mViewModel.updateSheetMusicPage()
                            }
                        }
                    }
                )
                mAddLayerDialog.show()
            }
        }
        mLayerListAdapter = LayerListAdapter(
            pageWidth,
            pageHeight,
            currentLayerIndex,
            layerClickListener
        )
        mLayerListAdapter.submitList(drawLayers)
        notation_recyclerview_layers.adapter = mLayerListAdapter

        val itemTouchHelper = ItemTouchHelper(getLayerSwipeAndDragHandler())
        itemTouchHelper.attachToRecyclerView(notation_recyclerview_layers)

        mActionBarDrawerToggle =
            ActionBarDrawerToggle(activity, notation_drawerLayout, R.string.openDrawer, R.string.closeDrawer)
        notation_drawerLayout.addDrawerListener(mActionBarDrawerToggle)
        mActionBarDrawerToggle.syncState()
    }

    private fun getLayerSwipeAndDragHandler() = SwipeAndDragHandler(
        requireContext(),
        object : ActionCompletionContract {
            override fun onViewMoved(oldPosition: Int, newPosition: Int) {
                mViewModel.sheetMusicPage.value?.let {
                    if (oldPosition < newPosition) {
                        for (i in oldPosition until newPosition) {
                            Collections.swap(it.drawLayers, i, i + 1)
                            it.drawLayers[i].layerOrder = i
                            it.drawLayers[i + 1].layerOrder = i + 1
                        }
                    } else {
                        for (i in oldPosition downTo newPosition + 1) {
                            Collections.swap(it.drawLayers, i, i - 1)
                            it.drawLayers[i].layerOrder = i
                            it.drawLayers[i - 1].layerOrder = i - 1
                        }
                    }
                    mLayerListAdapter.notifyItemMoved(oldPosition, newPosition)
                    mLayerListAdapter.submitList(it.drawLayers)
                }
            }

            override fun onViewSwiped(position: Int) {
                mViewModel.sheetMusicPage.value?.let {
                    if (it.drawLayers.size == 1) {
                        Toast.makeText(context, "There must be at least one layer", Toast.LENGTH_SHORT).show()
                        mLayerListAdapter.notifyItemChanged(position)
                        return
                    }

                    it.drawLayers.removeAt(position)
                    it.drawLayers.forEachIndexed { index, drawLayer ->
                        drawLayer.layerOrder = index
                    }
                    mLayerListAdapter.notifyDataSetChanged()
                    mViewModel.currentLayer.postValue(it.drawLayers[0])
                    mViewModel.currentLayerIndex = 0
                }
            }

            override fun clearView() {
                mViewModel.updateSheetMusicPage()
            }
        }
    )

    private fun setupTopBarToolsListeners() {
        notation_button_back.setOnClickListener {
            mEditor.getDrawLayer(mViewModel.currentLayer.value!!)
            mViewModel.updateSheetMusicPage()
            findNavController().navigateUp()
        }

        notation_button_undo.setOnClickListener {
            if (this::mEditor.isInitialized) mEditor.undo()
        }

        notation_button_save.setOnClickListener {
            mEditor.getDrawLayer(mViewModel.currentLayer.value!!)
            mViewModel.updateSheetMusicPage()
            Toast.makeText(context, "Saved ✓", Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        }
    }

    private fun setupBottomBarToolsListeners() {
        notation_slider_scale.addOnChangeListener { _, value, _ ->
            if (this::mEditor.isInitialized) mEditor.changeTextSize(value.toInt())
        }

        notation_bottomNav.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.notation_action_select -> {
                    mSharedViewModel.currentAction.postValue(DrawingMode.SELECT)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.notation_action_text -> {
                    if (mSharedViewModel.currentAction.value!! != DrawingMode.TEXT) {
                        mSharedViewModel.currentAction.postValue(DrawingMode.TEXT)
                    } else {
                        findNavController().navigate(
                            NotationFragmentDirections.notationFragmentOpenTextBottomSheetFragment()
                        )
                    }
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.notation_action_draw -> {
                    if (mSharedViewModel.currentAction.value!! != DrawingMode.DRAW) {
                        mSharedViewModel.currentAction.postValue(DrawingMode.DRAW)
                    } else {
                        findNavController().navigate(
                            NotationFragmentDirections.notationFragmentOpenDrawBottomSheetFragment()
                        )
                    }
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.notation_action_eraser -> {
                    if (mSharedViewModel.currentAction.value!! != DrawingMode.ERASER) {
                        mSharedViewModel.currentAction.postValue(DrawingMode.ERASER)
                    } else {
                        findNavController().navigate(NotationFragmentDirections.notationFragmentOpenEraserBottomSheetFragment())
                    }
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.notation_action_note -> {
                    mSharedViewModel.currentAction.postValue(DrawingMode.SELECT)
                    findNavController().navigate(
                        NotationFragmentDirections.notationFragmentOpenNoteBottomSheetFragment()
                    )
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
    }

    private fun setupLayerSideBarListeners() {
        notation_button_layers.setOnClickListener {
            if (notation_drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                notation_drawerLayout.closeDrawer(Gravity.RIGHT)
            } else {
                mEditor.getDrawLayer(mViewModel.currentLayer.value!!)
                mLayerListAdapter.submitList(mViewModel.sheetMusicPage.value!!.drawLayers)
                notation_drawerLayout.openDrawer(Gravity.RIGHT)
            }
        }

        notation_button_addLayer.setOnClickListener {
            mAddLayerDialog = AddLayerDialog(
                requireContext(),
                "",
                object : AddLayerDialogListener {
                    override fun onSaveButtonClicked(layerName: String) {
                        mViewModel.addNewLayer(layerName)
                    }
                }
            )
            mAddLayerDialog.show()
        }
    }

    private fun setupEditor() {
        mEditor = Editor(context, notation_notationView)

        mViewModel.currentLayer.observe(viewLifecycleOwner, Observer { drawLayer ->
            mEditor.setDrawLayer(drawLayer)
        })

        mEditor.setNotationViewListener(object : NotationViewListener {
            override fun onTextTouched(textView: TextView?) {
                if (textView != null) {
                    notation_slider_scale.visibility = View.VISIBLE
                    notation_slider_scale.value = textView.textSize / resources.displayMetrics.density
                } else {
                    notation_slider_scale.visibility = View.INVISIBLE
                }
            }
        })

        setupTextTool()
        setupPenTool()
        setupEraserTool()
        setupMusicNoteTool()

        mSharedViewModel.currentAction.observe(viewLifecycleOwner, Observer { mode ->
            mEditor.setDrawingMode(mode)
        })
    }

    private fun setupTextTool() {
        mSharedViewModel.apply {
            textColor.observe(viewLifecycleOwner, Observer {
                mEditor.setTextColor(Color.parseColor(it))
            })
            textFontFamily.observe(viewLifecycleOwner, Observer {
                mEditor.setTextFontFamily(it)
            })
            textSize.observe(viewLifecycleOwner, Observer {
                mEditor.setTextSize(it.toFloat())
            })
            textStyle.observe(viewLifecycleOwner, Observer {
                mEditor.setTextStyle(it)
            })
        }
    }

    private fun setupPenTool() {
        mSharedViewModel.apply {
            penColor.observe(viewLifecycleOwner, Observer {
                mEditor.setBrushColor(Color.parseColor(it))
            })
            penType.observe(viewLifecycleOwner, Observer {
                mEditor.setOpacity(if (it == PenType.PEN) 100 else 30)
            })
            penSize.observe(viewLifecycleOwner, Observer {
                mEditor.setBrushSize(it.toFloat())
            })
        }

    }

    private fun setupEraserTool() {
        mSharedViewModel.apply {
            eraserSize.observe(viewLifecycleOwner, Observer {
                mEditor.setEraserSize(it)
            })
            eraserMode.observe(viewLifecycleOwner, Observer {
                mEditor.setEraserMode(it)
            })
        }
    }

    private fun setupMusicNoteTool() {
        mSharedViewModel.musicNoteString.observe(viewLifecycleOwner, Observer {
            if (it.isNotBlank()) {
                val drawText = DrawText(it, 100f, 100f)
                drawText.size = 20f
                drawText.textColor = Color.parseColor(mSharedViewModel.textColor.value)
                mEditor.addMusicNote(drawText)
                mSharedViewModel.musicNoteString.postValue("")
            }
        })
    }

    companion object {
        private val TAG = NotationFragment::class.qualifiedName
    }
}