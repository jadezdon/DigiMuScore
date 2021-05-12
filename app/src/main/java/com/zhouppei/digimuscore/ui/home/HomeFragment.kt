package com.zhouppei.digimuscore.ui.home

import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zhouppei.digimuscore.adapters.*
import com.zhouppei.digimuscore.data.models.Folder
import com.zhouppei.digimuscore.data.models.SheetMusic
import com.zhouppei.digimuscore.databinding.FragmentHomeBinding
import com.zhouppei.digimuscore.ui.home.dialog.*
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.utils.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val mViewModel by viewModels<HomeViewModel>()

    private lateinit var mAddSheetMusicDialog: AddSheetMusicDialog
    private lateinit var mAddFolderDialog: AddFolderDialog
    private lateinit var mChooseFolderDialog: ChooseFolderDialog

    private lateinit var mSheetMusicListAdapter: SheetMusicListAdapter
    private lateinit var mFolderListAdapter: FolderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        context ?: return binding.root

        setupRecyclerViews(binding)

        initViewModel(mFolderListAdapter, mSheetMusicListAdapter)

        return binding.root
    }

    private fun setupRecyclerViews(binding: FragmentHomeBinding) {
        mFolderListAdapter = FolderListAdapter(getFolderListener())
        binding.homeRecyclerViewFolderList.adapter = mFolderListAdapter
        val folderHandler = getFolderHandler(mFolderListAdapter)
        val folderItemTouchHelper = ItemTouchHelper(folderHandler)
        folderItemTouchHelper.attachToRecyclerView(binding.homeRecyclerViewFolderList)

        mSheetMusicListAdapter = SheetMusicListAdapter(getSheetMusicListener())
        binding.homeRecyclerViewSheetMusicList.adapter = mSheetMusicListAdapter
        val sheetMusicHandler = getSheetMusicHandler(mSheetMusicListAdapter)
        val sheetMusicItemTouchHelper = ItemTouchHelper(sheetMusicHandler)
        sheetMusicItemTouchHelper.attachToRecyclerView(binding.homeRecyclerViewSheetMusicList)
    }

    private fun getFolderListener() = object : FolderClickListener {
        override fun onFolderClick(folder: Folder) {
            mViewModel.sheetMusics.value?.let {
                mSheetMusicListAdapter.submitList(it.filter { sheetMusic -> sheetMusic.folderId == folder.id })
            }
            mFolderListAdapter.submitList(emptyList())
            home_fab_addFolder.visibility = View.GONE
            home_main_topBar.visibility = View.GONE
            home_folder_detail_topBar.visibility = View.VISIBLE
            home_text_folderName.text = folder.name
            mViewModel.currentFolder = folder
        }
    }

    private fun getSheetMusicListener() = object : ItemSheetMusicClickListener {
        override fun onFavoriteClicked(sheetMusic: SheetMusic) {
            sheetMusic.isFavorite = !sheetMusic.isFavorite
            mViewModel.updateSheetMusic(sheetMusic)
        }

        override fun onLongClick(sheetMusic: SheetMusic) {
            mViewModel.folders.value?.let {
                mChooseFolderDialog = ChooseFolderDialog(
                    requireContext(),
                    object : FolderClickListener {
                        override fun onFolderClick(folder: Folder) {
                            sheetMusic.folderId = folder.id
                            mViewModel.updateSheetMusic(sheetMusic)
                        }
                    },
                    it.filter { folder -> folder.id != sheetMusic.folderId }
                )
                mChooseFolderDialog.show()
            }
        }
    }

    private fun getFolderHandler(adapter: FolderListAdapter) = SwipeAndDragHandler(
        requireContext(),
        object : ActionCompletionContract {
            override fun onViewMoved(oldPosition: Int, newPosition: Int) {
                val tempList = adapter.currentList.toMutableList()
                Collections.swap(tempList, oldPosition, newPosition)
                adapter.submitList(tempList)
            }

            override fun onViewSwiped(position: Int) {
                context?.let { c ->
                    MaterialAlertDialogBuilder(c)
                        .setTitle("Delete folder")
                        .setMessage("Are you sure you want to delete ${adapter.currentList[position].name} folder?")
                        .setNegativeButton("No") { dialog, which ->
                            adapter.notifyItemChanged(position)
                        }
                        .setPositiveButton("Yes") { dialog, which ->
                            mViewModel.deleteFolder(adapter.currentList[position])
                            adapter.notifyDataSetChanged()
                        }
                        .show()
                }
            }

            override fun clearView() {
                mViewModel.updateAllFolder(adapter.currentList)
            }
        }
    )

    private fun getSheetMusicHandler(adapter: SheetMusicListAdapter) = SwipeAndDragHandler(
        requireContext(),
        object : ActionCompletionContract {
            override fun onViewMoved(oldPosition: Int, newPosition: Int) {

            }

            override fun onViewSwiped(position: Int) {
                context?.let { c ->
                    MaterialAlertDialogBuilder(c)
                        .setTitle("Delete sheet music")
                        .setMessage("Are you sure you want to delete ${adapter.currentList[position].title} sheet music?")
                        .setNegativeButton("No") { dialog, which ->
                            adapter.notifyItemChanged(position)
                        }
                        .setPositiveButton("Yes") { dialog, which ->
                            val sheetMusic = adapter.currentList[position]
                            FileUtil.deleteAllRelatedFiles(
                                "${context!!.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.path}/Sheetmusics/sheetmusic-${sheetMusic.id}"
                            )
                            mViewModel.deleteSheetMusic(sheetMusic)
                            adapter.notifyDataSetChanged()
                        }
                        .show()
                }
            }

            override fun clearView() {
                mViewModel.updateAllSheetMusic(adapter.currentList)
            }
        }
    )

    private fun initViewModel(
        folderListAdapter: FolderListAdapter,
        sheetMusicListAdapter: SheetMusicListAdapter
    ) {
        mViewModel.currentFolder = null

        mViewModel.folders.observe(viewLifecycleOwner, Observer { folders ->
            folderListAdapter.submitList(folders.filter { folder -> folder.name != Constants.DEFAULT_FOLDER_NAME })
        })

        mViewModel.sheetMusics.observe(viewLifecycleOwner, Observer { sheetMusics ->
            if (mViewModel.folders.value == null) {
                sheetMusicListAdapter.submitList(sheetMusics.filter { sheetMusic -> sheetMusic.folderId == 1 })
            }
            mViewModel.folders.value?.let {
                it.find { folder ->
                    (mViewModel.currentFolder == null && folder.name == Constants.DEFAULT_FOLDER_NAME)
                            || (mViewModel.currentFolder != null && folder.id == mViewModel.currentFolder!!.id)
                }?.let { folder ->
                    sheetMusicListAdapter.submitList(sheetMusics.filter { sheetMusic -> sheetMusic.folderId == folder.id })
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        home_button_all.setOnClickListener { showMainPageContent() }

        home_button_favorite.setOnClickListener {
            mViewModel.sheetMusics.value?.let {
                mSheetMusicListAdapter.submitList(it.filter { sheetMusic -> sheetMusic.isFavorite })
            }
            mFolderListAdapter.submitList(emptyList())
            home_fab_addFolder.visibility = View.GONE
        }

        home_fab_addSheetMusic.setOnClickListener {
            mAddSheetMusicDialog =
                AddSheetMusicDialog(
                    requireContext(),
                    object :
                        AddSheetMusicDialogListener {
                        override fun onAddButtonClicked(
                            author: String,
                            title: String
                        ) {
                            val folderId = if (mViewModel.currentFolder == null) 1 else mViewModel.currentFolder!!.id
                            val sheetMusic = SheetMusic(
                                author = author,
                                title = title,
                                folderId = folderId,
                                isFavorite = false
                            )
                            mViewModel.addSheetMusic(sheetMusic)
                        }
                    })
            mAddSheetMusicDialog.show()
        }

        home_fab_addFolder.setOnClickListener {
            mAddFolderDialog = AddFolderDialog(
                requireContext(),
                object : AddFolderDialogListener {
                    override fun onAddFolderButtonClicked(folderName: String) {
                        mViewModel.addFolder(Folder(name = folderName))
                    }
                })
            mAddFolderDialog.show()
        }

        home_button_back.setOnClickListener { showMainPageContent() }

        registerForContextMenu(home_button_optionMenu)
        home_button_optionMenu.setOnClickListener { it.showContextMenu() }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        optionMenuItems.forEach { menu.add(0, v.id, 0, it) }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.title) {
            "Settings" -> {
                val direction = HomeFragmentDirections.actionHomeFragmentToSettingsActivity()
                findNavController().navigate(direction)
                true
            }
            else -> false
        }
    }

    private fun showMainPageContent() {
        mViewModel.folders.value?.let { folders ->
            mViewModel.sheetMusics.value?.let { sheetMusics ->
                folders.find { folder -> folder.name == Constants.DEFAULT_FOLDER_NAME }?.let { defaultFolder ->
                    mSheetMusicListAdapter.submitList(sheetMusics.filter { sheetMusic -> sheetMusic.folderId == defaultFolder.id })
                    mFolderListAdapter.submitList(folders.filter { folder -> folder.id != defaultFolder.id })
                }
            }
        }
        home_fab_addFolder.visibility = View.VISIBLE
        home_main_topBar.visibility = View.VISIBLE
        home_folder_detail_topBar.visibility = View.GONE
        mViewModel.currentFolder = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mAddSheetMusicDialog.isInitialized)
            mAddSheetMusicDialog.dismiss()

        if (this::mAddFolderDialog.isInitialized)
            mAddFolderDialog.dismiss()

        if (this::mChooseFolderDialog.isInitialized)
            mChooseFolderDialog.dismiss()
    }

    companion object {
        private val TAG = HomeFragment::class.qualifiedName
        private val optionMenuItems = listOf("Settings")
    }
}