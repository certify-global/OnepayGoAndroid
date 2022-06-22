package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.databinding.ActivityProfileBinding
import com.onepay.onepaygo.model.RefreshTokenViewModel
import com.onepay.onepaygo.model.UserProfileViewModel


class ProfileActivity : AppCompatActivity(), CallbackInterface {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var userProfileViewModel: UserProfileViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null

    private var pDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userProfileViewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        RetrofitInstance.init()

        initView()
    }

    fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)!!
        pDialog = Utils.showDialog(this)
        binding.includeAppBarProfile.tvTitleSettings.text = resources.getString(R.string.profile)
        binding.includeAppBarProfile.drawerIcon.setImageResource(R.drawable.ic_back_arrow)
        pDialog?.show()
        userProfileViewModel?.userProfile(sharedPreferences)
        binding.includeAppBarProfile.drawerIcon.setOnClickListener { finish() }

        userProfileViewModel?.userProfileLiveData?.observe(this) { it ->
            pDialog?.dismiss()
            if (it != null) {
                binding.tvUserName.text = it.userName
                binding.tvFirstName.text = it.first_name
                binding.tvLastName.text = it.last_name
                binding.tvEmail.text = it.email
                binding.tvPhone.text = it.phone_number
                val roleId = it.roleId
                val roleName = it.roles.filter { it.Id == roleId }
                if (roleName.size > 0)
                    binding.tvRole.text = roleName.get(0).Name

            }
        }
        userProfileViewModel?.messageError?.observe(this)
        {
            if (userProfileViewModel?.messageError?.value.isNullOrEmpty()) return@observe
            if (userProfileViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.refresh_token))
            } else {
                pDialog?.cancel()
                Logger.toast(this, userProfileViewModel?.messageError?.value!!)
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(this)
        {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(this, this)
            } else {
                userProfileViewModel?.userProfile(sharedPreferences)
            }
        }
    }

    override fun onCallback(msg: String?) {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}