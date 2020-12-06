package com.healthbuzz.healthbuzz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.healthbuzz.healthbuzz.Retrofit.RetrofitAPI
import com.healthbuzz.healthbuzz.data.LoginDataSource
import com.healthbuzz.healthbuzz.ui.login.LoginActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

        /*
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
           */
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

//    override fun onResume() {
//        super.onResume()
//
//        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
//        val continueService = sharedPrefs.getBoolean("activate_drinking", false)
//
//        val img = findViewById<View>(R.id.image) as ImageView
//
//        if (continueService) {
//            img.visibility = View.VISIBLE
//        } else {
//            img.visibility = View.INVISIBLE
//        }
//    }

}