package com.onepay.miura.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.onepay.miura.R
import com.onepay.miura.common.PreferencesKeys
import com.onepay.miura.common.Utils
import com.onepay.miura.data.AppSharedPreferences
import com.onepay.miura.databinding.AppBarMainBinding
import com.onepay.miura.databinding.HomeActivityBinding
import com.onepay.miura.databinding.UserProfileBinding


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val TAG: String = HomeActivity::class.java.name

    private lateinit var bindingHomeActivity: HomeActivityBinding

    private lateinit var sharedPreferences: SharedPreferences



    var toggle: ActionBarDrawerToggle? = null
    private var pDialog: Dialog? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val LOCATION_PERMISSION_REQ_CODE = 1000;
   // private lateinit var mLocationRequest : LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingHomeActivity = HomeActivityBinding.inflate(layoutInflater)
        setContentView(bindingHomeActivity.root)
      //  setSupportActionBar(bindingHomeActivity.includeAppBar.toolbar)
        initView()
        initDrawer()
      //  initViewModel()
        setClickListener()
    }


    override fun onStart() {
        super.onStart()
       // startLocationUpdates()

    }



    private fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            bindingHomeActivity.drawerLayout,
            null,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        toggle?.setDrawerIndicatorEnabled(false);
        bindingHomeActivity.drawerLayout.addDrawerListener(toggle!!)
        toggle?.syncState()
        bindingHomeActivity.navView.setNavigationItemSelectedListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListener() {

        bindingHomeActivity.includeAppBar.drawerIcon.setOnClickListener(View.OnClickListener {
            if (bindingHomeActivity.drawerLayout.isDrawerOpen(GravityCompat.START) == true)
                bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.END)
            else
                bindingHomeActivity.drawerLayout.openDrawer(GravityCompat.START)
        })
    }

    private fun initView() {

        pDialog = Utils.showDialog(this)
        bindingHomeActivity.navView.setItemIconTintList(null);
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)!!
        val navViewHeaderBinding=UserProfileBinding.bind(bindingHomeActivity.navView.getHeaderView(0))
        navViewHeaderBinding.tvUserName.text = AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.userName)
        navViewHeaderBinding.tvEmailUser.text = AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.email)
        navViewHeaderBinding.tvPhoneUser.text = AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.terminalValues)


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_transaction -> {
                findNavController(R.id.nav_left_menu_container).navigate(R.id.chargeFragment)

            }
            R.id.nav_support -> {
             //   launchProfileFragment()
                Toast.makeText(baseContext,"nav_support",Toast.LENGTH_LONG).show()
            }
            R.id.nav_help -> {

                Toast.makeText(baseContext,"nav_help",Toast.LENGTH_LONG).show()
                // launchAccountFragment()
            }
            R.id.nav_settings -> {
                Toast.makeText(baseContext,"nav_settings",Toast.LENGTH_LONG).show()

            }
            R.id.nav_Privacy_pol -> {
             //   Util.logOutDialog(this)
            }
        }
        bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (bindingHomeActivity.drawerLayout.isDrawerOpen(GravityCompat.START) == true) {
            bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            this.finishAffinity()
        }
    }

}