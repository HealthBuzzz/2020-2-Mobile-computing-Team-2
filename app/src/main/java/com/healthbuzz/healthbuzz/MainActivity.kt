package com.healthbuzz.healthbuzz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {
    val PERMISSION_ALL = 1

    public var userName: LiveData<String> = MutableLiveData()
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.any {
                    it != PackageManager.PERMISSION_GRANTED
                }) {
                Toast.makeText(
                    this,
                    getString(R.string.require_storage_permission),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                if (ensureExternalManager())
                    startSensorService(this)
            }
        }
    }

//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                // Permission is granted. Continue the action or workflow in your
//                // app.
//
//                startSensorService(this)
////                startBlService(this)
//            } else {
//                Toast.makeText(
//                    this,
//                    getString(R.string.require_storage_permission),
//                    Toast.LENGTH_LONG
//                ).show()
//                finish()
//                // Explain to the user that the feature is unavailable because the
//                // features requires a permission that the user has denied. At the
//                // same time, respect the user's decision. Don't link to system
//                // settings in an effort to convince the user to change their
//                // decision.
//            }
//        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

        // Show the Up button in the action bar.
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val weatherFragment = WeatherFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.weather_container, weatherFragment)
                .commit()

            val dashboardFragment = DashboardFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, dashboardFragment)
                .commit()

            val welcomeFragment = WelcomeFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.welcome_container, welcomeFragment)
                .commit()
        }

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        val hasPermission = hasPermissions(
            this,
            *permissions
        )

        if (hasPermission) {
            if (ensureExternalManager())
                startSensorService(this)
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL)
            //            requestPermissionLauncher.launch(
            //                Manifest.permission.WRITE_EXTERNAL_STORAGE
            //            )
        }
    }

    private fun ensureExternalManager(): Boolean {
        val isExternalStorageManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        if (!isExternalStorageManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val theIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(theIntent)
                finish()
            }
        }
        return isExternalStorageManager
    }


    override fun onResume() {
        super.onResume()

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val continueService = sharedPrefs.getBoolean("activate_drinking", false)

        val img = findViewById<View>(R.id.img_watch_icon) as ImageView

        if (continueService) {
            img.visibility = View.VISIBLE
        } else {
            img.visibility = View.INVISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back

//                    navigateUpTo(Intent(this, ItemListActivity::class.java))

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}