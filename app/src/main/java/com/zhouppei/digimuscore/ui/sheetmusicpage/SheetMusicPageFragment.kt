package com.zhouppei.digimuscore.ui.sheetmusicpage

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.adapters.OnSwipeListener
import com.zhouppei.digimuscore.databinding.FragmentSheetMusicPageBinding
import kotlinx.android.synthetic.main.fragment_sheet_music_page.*
import javax.inject.Inject

@AndroidEntryPoint
class SheetMusicPageFragment : Fragment() {

    private val mArgs: SheetMusicPageFragmentArgs by navArgs()

    @Inject
    lateinit var mViewModelFactory: SheetMusicPageViewModel.SheetMusicPageAssistedFactory
    private val mViewModel: SheetMusicPageViewModel by viewModels {
        SheetMusicPageViewModel.provideFactory(
            mViewModelFactory,
            mArgs.sheetMusicId,
            mArgs.currentPageId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSheetMusicPageBinding>(
            inflater,
            R.layout.fragment_sheet_music_page,
            container,
            false
        ).apply {
            viewmodel = mViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        mViewModel.sheetMusicPages.observe(viewLifecycleOwner, Observer {
            val page = it.find { page ->
                page.id == mArgs.currentPageId
            }
            mViewModel.currentPage.postValue(page)
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        registerForContextMenu(sheetMusicPage_button_menu)
        sheetMusicPage_button_menu.setOnClickListener {
            it.showContextMenu()
        }

        sheetMusicPage_button_back.setOnClickListener {
            findNavController().navigateUp()
        }

        sheetMusicPage_image_page.setOnTouchListener(swipePageListener)
    }

    private val swipePageListener = object : OnSwipeListener(context) {
        override fun onSwipeRight() {
            showPrevPage()
        }

        override fun onSwipeLeft() {
            showNextPage()
        }
    }

    private fun showNextPage() {
        mViewModel.sheetMusicPages.value?.let { pageList ->
            mViewModel.currentPage.value?.let { page ->
                pageList.find {
                    it.pageNumber == (page.pageNumber + 1)
                }?.let {
                    mViewModel.currentPage.postValue(it)
                }
            }
        }
    }

    private fun showPrevPage() {
        mViewModel.sheetMusicPages.value?.let { pageList ->
            mViewModel.currentPage.value?.let { page ->
                pageList.find {
                    it.pageNumber == (page.pageNumber - 1)
                }?.let {
                    mViewModel.currentPage.postValue(it)
                }
            }
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
            "Annotate" -> {
                mViewModel.currentPage.value?.let {
                    val direction =
                        SheetMusicPageFragmentDirections.actionSheetMusicPageFragmentToNotationFragment(it.id)
                    findNavController().navigate(direction)
                }
                true
            }
            "Play mode" -> {
                val direction =
                    SheetMusicPageFragmentDirections.actionSheetMusicPageFragmentToPageTurnerFragment(mArgs.sheetMusicId)
                findNavController().navigate(direction)
                true
            }
            else -> false
        }
    }

    companion object {
        private val TAG = SheetMusicPageFragment::class.qualifiedName
        private val optionMenuItems = listOf("Annotate", "Play mode")
    }

}