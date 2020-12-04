package com.healthbuzz.healthbuzz

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    public var userName: LiveData<String> = MutableLiveData()
    private val requestPermissionLauncher =
        registerForActivityResult(
                ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.

                startSensorService(this)
            } else {
                Toast.makeText(
                        this,
                        getString(R.string.require_storage_permission),
                        Toast.LENGTH_LONG
                ).show()
                finish()
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    //override fun onResume() {
    //super.onResume()
    //welcomeFragment.textView2.setText(LoginDataSource.name)
    //}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

//        findViewById<FloatingActionButton>(R.id.fab) is not needed thanks to kotlin synthetic android extension

        // Show the Up button in the action bar.
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don"t need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val dashboardFragment = DashboardFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, dashboardFragment)
                .commit()

            val welcomeFragment = WelcomeFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.welcome_container, welcomeFragment)
                .commit()
        }

        val isExternalStorageManager =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }


        val hasPermission =
//            ContextCompat.checkSelfPermission(
//            applicationContext,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (!hasPermission) {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
//            requestPermissionLauncher.launch(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            )
            requestPermissionLauncher.launch(

                Manifest.permission.ACCESS_FINE_LOCATION,

            )
        }
        if (hasPermission)
            startSensorService(this)

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