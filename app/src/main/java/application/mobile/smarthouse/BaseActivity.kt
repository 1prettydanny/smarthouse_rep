package application.mobile.smarthouse


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import application.mobile.smarthouse.databinding.ActivityBaseBinding
import application.mobile.smarthouse.ui.Profile.ProfileFragment


class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)


        val customToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.my_toolbar)
        val toolbarTitle = customToolbar.findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = UserInfo.selected_home_name




        setSupportActionBar(customToolbar)

        binding.navView.setupWithNavController(
            findNavController(R.id.nav_host_fragment_activity_base)
        )



//        val navView: BottomNavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_activity_base)
//
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home,
//                R.id.navigation_dashboard,
//                R.id.navigation_profile,
//            )
//        )
//
//        setupActionBarWithNavController(navController, appBarConfiguration)
//
//        navView.setupWithNavController(navController)


        //            navView.setOnItemSelectedListener { item ->
    //
    //                when (item.itemId) {
    //                    R.id.navigation_home -> {
    //                        val homeFragment = HomeFragment()
    //                        supportFragmentManager.beginTransaction()
    //                            .replace(R.id.nav_host_fragment_activity_base, homeFragment)
    //                            .commit()
    //
    //                        title.text = home_name
    //                        return@setOnItemSelectedListener true
    //                    }
    //
    //                    R.id.navigation_devices -> {
    //                        val devicesFragment = DevicesFragment()
    //                        supportFragmentManager.beginTransaction()
    //                            .replace(R.id.nav_host_fragment_activity_base, devicesFragment)
    //                            .commit()
    //
    //                        title.text = getString(R.string.devices)
    //                        return@setOnItemSelectedListener true
    //                    }
    //
    //                    R.id.navigation_profile -> {
    //                        val profileFragment = ProfileFragment()
    //                        supportFragmentManager.beginTransaction()
    //                            .replace(R.id.nav_host_fragment_activity_base, profileFragment)
    //                            .commit()
    //
    //                        title.text = getString(R.string.profile)
    //                        return@setOnItemSelectedListener true
    //
    //                    }
    //
    //                    else -> return@setOnItemSelectedListener false
    //                }
    //            }


    }




    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
        }
    }


}