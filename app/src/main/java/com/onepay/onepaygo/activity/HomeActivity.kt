package com.onepay.onepaygo.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.HomeActivityBinding
import com.onepay.onepaygo.databinding.UserProfileBinding


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

        bindingHomeActivity.includeView.includeAppBar.drawerIcon.setOnClickListener(View.OnClickListener {
            if (bindingHomeActivity.drawerLayout.isDrawerOpen(GravityCompat.START) == true)
                bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.END)
            else {
                bindingHomeActivity.drawerLayout.openDrawer(GravityCompat.START)
                Utils.hideKeyboard(this)
            }
        })
    }

    private fun initView() {

        pDialog = Utils.showDialog(this)
        bindingHomeActivity.navView.setItemIconTintList(null);
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)!!
        val navViewHeaderBinding=UserProfileBinding.bind(bindingHomeActivity.navView.getHeaderView(0))
        bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.setText(getString(R.string.Charge))
        navViewHeaderBinding.tvUserName.text = AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.userName)
        navViewHeaderBinding.tvEmailUser.text = AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.email)
        navViewHeaderBinding.tvPhoneUser.text = AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.terminalValues)


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_transaction_menu -> {
                bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.setText(resources.getString(R.string.transaction_history))
                findNavController(R.id.nav_left_menu_container).navigate(R.id.TransactionHistoryFragment)

            }
            R.id.nav_chage_menu -> {
                bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.setText(getString(R.string.Charge))
                findNavController(R.id.nav_left_menu_container).navigate(R.id.chargeFragment)

            }
            R.id.nav_support -> {
                if (Utils.isConnectingToInternet(this)) {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("loadURL", Constants.supportUrl)
                    startActivity(intent)
                } else {
                    Logger.toast(this, resources.getString(R.string.network_error))
                }
            }
            R.id.nav_help -> {

                if (Utils.isConnectingToInternet(this)) {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("loadURL", Constants.helpUrl)
                    startActivity(intent)
                } else {
                    Logger.toast(this, resources.getString(R.string.network_error))
                }
            }
            R.id.nav_settings -> {
                bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.setText(getString(R.string.tv_settings))
                findNavController(R.id.nav_left_menu_container).navigate(R.id.settingFragment)


            }
            R.id.nav_Privacy_pol -> {
                if (Utils.isConnectingToInternet(this)) {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("loadURL", Constants.privacyUrl)
                    startActivity(intent)
                } else {
                    Logger.toast(this, resources.getString(R.string.network_error))
                }
            }
        }
        bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (bindingHomeActivity.drawerLayout.isDrawerOpen(GravityCompat.START) == true) {
            bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.START)
        } else if(TransactionDataSource.getIsHome() == false){
            TransactionDataSource.setIsHome(true)
            findNavController(R.id.nav_left_menu_container).navigate(R.id.chargeFragment)

        }else{
            if (supportFragmentManager.backStackEntryCount == 0) {
                val builder = AlertDialog.Builder(this@HomeActivity)
                builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id -> finishAffinity() }
                    .setNegativeButton("No") { dialog, id -> dialog.cancel() }
                val alert = builder.create()
                alert.show()
            }else{

            }
        }
    }
    fun updateTitle( title:String){
        try {
            bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.setText(title)
        }catch (e:Exception){
            e.toString()
        }

    }
}