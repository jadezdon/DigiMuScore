package com.zhouppei.digimuscore.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.ui.configmotion.ConfigMotionActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        findPreference<Preference>("configMotion")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ConfigMotionActivity::class.java))
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setPadding(0, 0, 0, 0)
    }
}