package com.onepay.onepaygo.api

import com.onepay.onepaygo.api.request.LoginRequest
import com.onepay.onepaygo.api.response.LoginResponse
import com.onepay.onepaygo.api.response.TerminalResponse
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

    @GET("EP/api/Merchant/GetTerminalAccessList")
    fun terminalAccess(
        @Header("Authorization") authorization: String?,
        @Header("Content-type") contentType: String?,
        @Header("GatewayId") gatewayId: String?,
        @Header("UserId") userId: String?
    ): Call<List<TerminalResponse>>


}