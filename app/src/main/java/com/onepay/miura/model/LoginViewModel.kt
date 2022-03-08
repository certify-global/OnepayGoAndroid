package com.onepay.miura.model

import android.content.Context
import androidx.lifecycle.ViewModel
import com.onepay.miura.api.RetrofitInstance
import com.onepay.miura.repo.LoginRepository

class LoginViewModel : ViewModel() {

    private var loginRepository : LoginRepository = LoginRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun login(username : String, passwd : String) {

    }
}