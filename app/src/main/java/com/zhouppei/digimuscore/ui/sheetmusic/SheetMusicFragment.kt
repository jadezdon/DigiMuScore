package com.zhouppei.digimuscore.ui.sheetmusic

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Size
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import dagger.hilt.android.AndroidEntryPoint
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.adapters.ActionCompletionContract
import com.zhouppei.digimuscore.adapters.SheetMusicPageListAdapter
import com.zhouppei.digimuscore.adapters.SwipeAndDragHandler
import com.zhouppei.digimuscore.databinding.FragmentSheetMusicBinding
import com.zhouppei.digimuscore.ui.scanner.ScannerActivity
import com.zhouppei.digimuscore.utils.BitmapUtil
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.utils.FileUtil
import com.zhouppei.digimuscore.utils.autoFitColumns
import kotlinx.android.synthetic.main.fragment_sheet_music.*
import java.io.File
import java.util.*
import javax.inject.Inject

// https://github.com/ajay-dewari/Animating-FAB

@AndroidEntryPoint
class SheetMusicFragment : Fragment() {

    private val mArgs: SheetMusicFragmentArgs by navArgs()

    @Inject
    lateinit var mSheetMusicViewModelFactory: SheetMusicViewModel.AssistedFactory

    private val mViewModel: SheetMusicViewModel by viewModels {
        SheetMusicViewModel.provideFactory(
            mSheetMusicViewModelFactory,
            mArgs.sheetMusicId
        )
    }

    private val fabRotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_rotate_open_anim) }
    private val fabRotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.fab_rotate_close_anim
        )
    }
    private val fabFromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_from_bottom_anim) }
    private val fabToBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_to_bottom_anim) }
    private var isAddFabClicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSheetMusicBinding>(
            inflater,
            R.layout.fragment_sheet_music,
            container,
            false
        ).apply {
            viewmodel = mViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        val adapter = SheetMusicPageListAdapter()
        binding.sheetMusicRecyclerview.adapter = adapter
        val swipeHandler = getSheetMusicPageHandler(adapter)
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.sheetMusicRecyclerview)

        binding.sheetMusicRecyclerview.autoFitColumns(200)

        initViewModel(adapter)

        return binding.root
    }

    private fun getSheetMusicPageHandler(adapter: SheetMusicPageListAdapter) = SwipeAndDragHandler(
        requireContext(),
        object : ActionCompletionContract {
            override fun onViewMoved(oldPosition: Int, newPosition: Int) {
                val tempList = adapter.currentList.toMutableList()
                // pageNumber start with 1
                Collections.swap(tempList, oldPosition, newPosition)
                adapter.submitList(tempList)
            }

            override fun onViewSwiped(position: Int) {
                val tempList = adapter.currentList.toMutableList()
                mViewModel.deletePage(tempList.removeAt(position))
                adapter.submitList(tempList)
            }

            override fun clearView() {
                adapter.currentList.forEachIndexed { index, sheetMusicPage ->
                    sheetMusicPage.pageNumber = index + 1
                }
                mViewModel.updateAllPage(adapter.currentList)
            }
        }
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetMusic_button_back.setOnClickListener {
            findNavController().navigateUp()
        }

        setAddButtonsClickListener()

        registerForContextMenu(sheetMusic_button_menu)
        sheetMusic_button_menu.setOnClickListener {
            it.showContextMenu()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        optionMenuItems.forEach {
            menu.add(0, v.id, 0, it)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.title) {
            "Play mode" -> {
                val direction =
                    SheetMusicFragmentDirections.actionSheetMusicFragmentToPageTurnerFragment(mArgs.sheetMusicId)
                findNavController().navigate(direction)
                true
            }
            else -> false
        }
    }

    private fun setAddButtonsClickListener() {
        sheetMusic_fab_add.setOnClickListener {
            toggleFabAdd()
        }

        sheetMusic_fab_add_img.setOnClickListener {
            toggleFabAdd()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select image file"),
                Constants.REQUESTCODE_SELECT_IMAGE
            )
        }

        sheetMusic_fab_add_pdf.setOnClickListener {
            toggleFabAdd()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(intent, "Select pdf file"),
                Constants.REQUESTCODE_SELECT_PDF
            )
        }

        sheetMusic_fab_add_scan.setOnClickListener {
            toggleFabAdd()
            val intent = Intent(context, ScannerActivity::class.java).apply {
                putExtra("sheetMusicId", mArgs.sheetMusicId)
            }
            startActivityForResult(intent, Constants.REQUESTCODE_SCAN_DOCUMENT)
        }
    }

    private fun toggleFabAdd() {
        setVisibility(isAddFabClicked)
        setAnimation(isAddFabClicked)
        isAddFabClicked = !isAddFabClicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            sheetMusic_fab_add_scan.startAnimation(fabFromBottom)
            sheetMusic_fab_add_img.startAnimation(fabFromBottom)
            sheetMusic_fab_add_pdf.startAnimation(fabFromBottom)
            sheetMusic_fab_add.startAnimation(fabRotateOpen)
        } else {
            sheetMusic_fab_add_scan.startAnimation(fabToBottom)
            sheetMusic_fab_add_img.startAnimation(fabToBottom)
            sheetMusic_fab_add_pdf.startAnimation(fabToBottom)
            sheetMusic_fab_add.startAnimation(fabRotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        sheetMusic_fab_add_scan.visibility = if (!clicked) View.VISIBLE else View.GONE
        sheetMusic_fab_add_img.visibility = if (!clicked) View.VISIBLE else View.GONE
        sheetMusic_fab_add_pdf.visibility = if (!clicked) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Constants.REQUESTCODE_SELECT_IMAGE -> {
                    data?.data?.let { uri ->
                        val contentUri = FileUtil.copyFile(
                            uri,
                            requireContext(),
                            FileUtil.getFileNameNoExt()
                        ).path.toString()
                        val bitmap = BitmapFactory.decodeFile(contentUri)
                        mViewModel.addPage(contentUri, Size(bitmap.width, bitmap.height))
                    }
                }
                Constants.REQUESTCODE_SELECT_PDF -> {
                    data?.data?.let { uri ->
                        val contentUri = FileUtil.copyFile(
                            uri,
                            requireContext(),
                            FileUtil.getFileNameNoExt()
                        ).path.toString()
                        val tempFile = File(contentUri)
                        val bitmaps = BitmapUtil.pdfToBitmap(tempFile)
                        tempFile.delete()

                        val contentUris = arrayListOf<String>()
                        val imageSizes = arrayListOf<Size>()
                        bitmaps?.forEach { bitmap ->
                            val destinationFile = FileUtil.createOrGetFile(
                                requireContext(),
                                "Sheetmusics",
                                "${FileUtil.getFileNameNoExt()}.png"
                            )

                            BitmapUtil.saveBitmapToFile(bitmap, destinationFile.path.toString())
                            contentUris.add(destinationFile.path.toString())
                            imageSizes.add(Size(bitmap.width, bitmap.height))
                        }
                        mViewModel.addPages(contentUris, imageSizes)
                    }
                }
                Constants.REQUESTCODE_SCAN_DOCUMENT -> {
                    data?.let {
                        it.getStringExtra("contentUri")?.let { contentUri ->
                            val bitmap = BitmapFactory.decodeFile(contentUri)
                            mViewModel.addPage(contentUri, Size(bitmap.width, bitmap.height))
                        }
                    }
                }
            }
        }
    }

    private fun initViewModel(adapter: SheetMusicPageListAdapter) {
        mViewModel.sheetMusicPages.observe(
            viewLifecycleOwner,
            Observer { sheetMusicImgs ->
                adapter.submitList(sheetMusicImgs)
            })
    }

    companion object {
        private val TAG = SheetMusicFragment::class.qualifiedName
        private val optionMenuItems = listOf("Play mode")
    }
}