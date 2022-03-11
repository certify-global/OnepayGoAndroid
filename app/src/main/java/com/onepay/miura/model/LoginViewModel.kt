package com.onepay.miura.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.miura.api.RetrofitInstance
import com.onepay.miura.api.response.LoginResponse
import com.onepay.miura.common.Logger
import com.onepay.miura.repo.LoginRepository

class LoginViewModel : ViewModel() {
    private val TAG : String = LoginRepository::class.java.name
    val mlLoginResponse = MutableLiveData<LoginResponse>()
    private var loginRepository : LoginRepository = LoginRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun login(username : String, password : String) {
         loginRepository.login(username,password){isSuccess, response ->
            Logger.debug(TAG,response.toString())
             mlLoginResponse.value = response;
        }

    }
}