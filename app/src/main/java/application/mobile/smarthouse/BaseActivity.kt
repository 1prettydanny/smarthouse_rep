package application.mobile.smarthouse


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import application.mobile.smarthouse.databinding.ActivityBaseBinding
import application.mobile.smarthouse.ui.Profile.ProfileFragment
import application.mobile.smarthouse.ui.dashboard.DevicesFragment
import application.mobile.smarthouse.ui.home.HomeFragment

class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding
    private lateinit var mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = binding.toolbar
        toolbar.inflateMenu(R.menu.home_menu)
        val customToolbar = layoutInflater.inflate(R.layout.custom_toolbar_layout, null)
        val title = customToolbar.findViewById<TextView>(R.id.toolbar_title)


        var home_name = intent.getStringExtra("home_name")
        if (home_name == null) {
            GlobalObj.db.collection("homes")
                .whereEqualTo("home_id", UserInfo.homes.first())
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        home_name = document["home_name"].toString()
                        title.text = home_name
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "errInitH: "+exception.message , Toast.LENGTH_SHORT).show()
                }
        }
        else{
            title.text = home_name
        }

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_base)
        navView.setupWithNavController(navController)



        mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->

                when (item.itemId) {
                    R.id.navigation_home -> {
                        val homeFragment = HomeFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_base, homeFragment)
                            .commit()

                        title.text = home_name
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_devices -> {
                        val devicesFragment = DevicesFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_base, devicesFragment)
                            .commit()

                        title.text = getString(R.string.devices)
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_profile -> {
                        val profileFragment = ProfileFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_activity_base, profileFragment)
                            .commit()

                        title.text = getString(R.string.profile)
                        return@OnNavigationItemSelectedListener true

                    }

                    else -> return@OnNavigationItemSelectedListener false
                }
            }


        navView.setOnNavigationItemSelectedListener(null)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        toolbar.addView(customToolbar)
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

}