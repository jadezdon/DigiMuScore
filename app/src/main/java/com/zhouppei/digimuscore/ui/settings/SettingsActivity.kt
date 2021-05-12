package com.zhouppei.digimuscore.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zhouppei.digimuscore.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        settings_button_back.setOnClickListener { onBackPressed() }
    }
}