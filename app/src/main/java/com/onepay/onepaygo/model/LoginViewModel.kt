package com.onepay.onepaygo.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.LoginResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.repo.LoginRepository

class LoginViewModel : ViewModel() {
    private val TAG : String = LoginViewModel::class.java.name
    val mlLoginResponse = MutableLiveData<LoginResponse>()
    val messageError = MutableLiveData<String>()

    private var loginRepository : LoginRepository = LoginRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun login(username : String, password : String) {
         loginRepository.login(username,password){isSuccess, response ,message->
            Logger.debug(TAG,response.toString())
             mlLoginResponse.value = response
             messageError.value = message
        }

    }
}