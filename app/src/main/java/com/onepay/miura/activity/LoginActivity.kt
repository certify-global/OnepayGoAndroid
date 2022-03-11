package com.onepay.miura.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.onepay.miura.R
import com.onepay.miura.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
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