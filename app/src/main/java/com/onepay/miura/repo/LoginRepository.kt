package com.onepay.miura.repo

import com.onepay.miura.api.request.LoginRequest

class LoginRepository {


    fun signIn(username : String, passwd : String) {
        val loginRequest = LoginRequest(username, passwd)
    }
}