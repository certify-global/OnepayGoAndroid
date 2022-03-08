package com.onepay.miura.api

import com.onepay.miura.api.request.LoginRequest
import com.onepay.miura.api.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {

    //TODO:
    @POST("")
    fun loginUser(@Body loginRequest: LoginRequest) : Call<LoginResponse>

}