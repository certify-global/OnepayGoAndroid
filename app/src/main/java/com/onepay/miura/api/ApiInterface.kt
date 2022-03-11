package com.onepay.miura.api

import com.onepay.miura.api.request.LoginRequest
import com.onepay.miura.api.response.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {


    @POST("EP/Token")  // this way 400 error getting
    fun loginUser(@Body loginRequest: LoginRequest) : Call<LoginResponse>

    @FormUrlEncoded
    @POST("EP/Token")
    fun loginUserWith(
        @Header("Content-Type") contentType: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>
}