package com.zhouppei.digimuscore


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.utils.FontUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FontUtil.loadTypefaces(assets)

        requestAndCheckPermissions()
    }

    private fun checkReadAndWritePermission() {
        val readPermission =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "To use application read permission needs to be granted. Go to Settings > Apps > DigiMuScore grant storage permission",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        val writePermission =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "To use application write permission needs to be granted. Go to Settings > Apps > DigiMuScore grant storage permission",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun requestAndCheckPermissions() {
        val isFirstRun =
            PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.SHARED_PREF_KEY_FIRST_RUN, true)
        if (isFirstRun) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, Constants.REQUESTCODE_PERMISSIONS)
        } else {
            checkReadAndWritePermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Constants.REQUESTCODE_PERMISSIONS) {
            with(PreferenceManager.getDefaultSharedPreferences(this).edit()) {
                putBoolean(Constants.SHARED_PREF_KEY_FIRST_RUN, false)
                commit()
            }
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (!readPermission || !writePermission) {
                    Toast.makeText(
                        this,
                        "To use application read and write permissions need to be granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }
}