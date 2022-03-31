package com.onepay.onepaygo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.onepay.onepaygo.R
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.PermissionCheck(this)
    }

    override fun onStart() {
        super.onStart()
        findNavController(R.id.nav_host_login_container).navigate(R.id.loginMainFragment)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}