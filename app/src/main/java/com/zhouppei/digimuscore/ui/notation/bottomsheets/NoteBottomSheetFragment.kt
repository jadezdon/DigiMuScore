package com.zhouppei.digimuscore.ui.notation.bottomsheets

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouppei.digimuscore.adapters.MusicNoteClickListener
import com.zhouppei.digimuscore.adapters.MusicNoteListAdapter
import com.zhouppei.digimuscore.databinding.BottomsheetNotationNoteBinding
import com.zhouppei.digimuscore.ui.notation.SharedViewModel
import com.zhouppei.digimuscore.utils.Constants
import kotlinx.android.synthetic.main.bottomsheet_notation_note.*


class NoteBottomSheetFragment : BottomSheetDialogFragment() {

    private val mSharedViewModel by activityViewModels<SharedViewModel>()

    private lateinit var mMusicNoteListAdapter: MusicNoteListAdapter
    private var mMusicNotes = Constants.default_musicNotes
    private lateinit var mSharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomsheetNotationNoteBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.sharedViewModel = mSharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSharedPreferences =
            activity?.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE) ?: return

        if (mSharedPreferences.contains(Constants.SHARED_PREF_KEY_MUSICNOTE)) {
            val str = mSharedPreferences.getString(Constants.SHARED_PREF_KEY_MUSICNOTE, "")
            val itemType = object : TypeToken<MutableList<String>>() {}.type
            mMusicNotes = gson.fromJson(str, itemType)
        }

        val noteClickListener = object : MusicNoteClickListener {
            override fun onNoteClicked(item: String) {
                mMusicNoteListAdapter.submitList(mMusicNotes)
                mSharedViewModel.musicNoteString.postValue(item)
                bringMusicNoteToFirst(item)
                findNavController().navigateUp()
            }
        }

        noteBottomSheet_recycler_notes.apply {
            layoutManager = GridLayoutManager(context, calculateNoOfColumns(140f))
            mMusicNoteListAdapter = MusicNoteListAdapter(mMusicNotes, noteClickListener)
            adapter = mMusicNoteListAdapter
        }
    }

    private fun calculateNoOfColumns(columnWidthDp: Float): Int {
        val displayMetrics: DisplayMetrics = requireContext().resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / columnWidthDp + 0.5).toInt()
    }

    private fun bringMusicNoteToFirst(musicNote: String) {
        val index = mMusicNotes.indexOf(musicNote)
        mMusicNotes.removeAt(index)
        mMusicNotes.add(0, musicNote)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mSharedPreferences.isInitialized) {
            saveMusicNotes()
        }
    }

    private fun saveMusicNotes() {
        val editor = mSharedPreferences.edit()
        editor.putString(Constants.SHARED_PREF_KEY_MUSICNOTE, gson.toJson(mMusicNotes))
        editor.apply()
    }

    companion object {
        private val TAG = NoteBottomSheetFragment::class.qualifiedName
    }
}