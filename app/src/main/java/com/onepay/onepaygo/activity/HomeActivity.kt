package com.onepay.onepaygo.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.onepay.onepaygo.BuildConfig.VERSION_CODE
import com.onepay.onepaygo.BuildConfig.VERSION_NAME
import com.onepay.onepaygo.R
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.controller.DatabaseController
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.HomeActivityBinding
import com.onepay.onepaygo.databinding.UserProfileBinding


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    CallbackInterface {
    private lateinit var bindingHomeActivity: HomeActivityBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navViewHeaderBinding: UserProfileBinding

    var toggle: ActionBarDrawerToggle? = null
    private var pDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingHomeActivity = HomeActivityBinding.inflate(layoutInflater)
        setContentView(bindingHomeActivity.root)
        initView()
        initDrawer()
        setClickListener()
        Logger.info("", "onCreate", "HomeActivity")
    }


    private fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            bindingHomeActivity.drawerLayout,
            null,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        toggle?.isDrawerIndicatorEnabled = false
        bindingHomeActivity.drawerLayout.addDrawerListener(toggle!!)
        toggle?.syncState()
        bindingHomeActivity.navView.setNavigationItemSelectedListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListener() {

        bindingHomeActivity.includeView.includeAppBar.drawerIcon.setOnClickListener {
            if (bindingHomeActivity.drawerLayout.isDrawerOpen(GravityCompat.START))
                bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.END)
            else {
                bindingHomeActivity.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun initView() {

        pDialog = Utils.showDialog(this)
        bindingHomeActivity.navView.itemIconTintList = null
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)!!
        navViewHeaderBinding =
            UserProfileBinding.bind(bindingHomeActivity.navView.getHeaderView(0))
        navViewHeaderBinding.tvUserName.text =
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userName)
        navViewHeaderBinding.tvEmailUser.text =
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.email)
        navViewHeaderBinding.tvPhoneUser.text =
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalName)
        navViewHeaderBinding.root.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))

        }
        bindingHomeActivity.tvAppVersion.text = String.format("%s %s.%s", resources.getString(R.string.onepay_go_version), VERSION_NAME, VERSION_CODE)
        logoutInIt()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_transaction_menu -> {
                bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.text = resources.getString(R.string.reporting)
                findNavController(R.id.nav_left_menu_container).navigate(R.id.TransactionHistoryFragment)

            }
            R.id.nav_chage_menu -> {
                bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.text = ""
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
                bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.text = getString(R.string.tv_settings)
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
        if (bindingHomeActivity.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            bindingHomeActivity.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (TransactionDataSource.getIsHome() == false) {
            TransactionDataSource.setIsHome(true)
            findNavController(R.id.nav_left_menu_container).navigate(R.id.chargeFragment)

        } else {
            if (supportFragmentManager.backStackEntryCount == 0) {
                val builder = AlertDialog.Builder(this@HomeActivity)
                builder.setMessage(resources.getString(R.string.are_you_sure_you_want_to))
                    .setCancelable(false)
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ -> finishAffinity() }
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> dialog.cancel() }
                val alert = builder.create()
                alert.show()
            }
        }
    }

    fun updateTitle(title: String) {
        try {
            bindingHomeActivity.includeView.includeAppBar.tvTitleSettings.text = title
        } catch (e: Exception) {
            e.toString()
        }

    }

    fun updateLeftMenu() {
        try {
            navViewHeaderBinding.tvPhoneUser.text = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalName)
        } catch (e: Exception) {
            e.toString()
        }

    }

    private fun logoutInIt() {
        bindingHomeActivity.tvLogout.setOnClickListener { Utils.openDialogLogout(this, this) }

    }

    override fun onCallback(msg: String?) {
        DatabaseController.instance?.deleteAll()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}