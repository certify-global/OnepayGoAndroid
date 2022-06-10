package com.onepay.onepaygo.api

import com.onepay.onepaygo.api.request.ApiKeyRequest
import com.onepay.onepaygo.api.request.LoginRequest
import com.onepay.onepaygo.api.request.RetrieveTransactionRequest
import com.onepay.onepaygo.api.request.TransactionRequest
import com.onepay.onepaygo.api.response.*
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

    @FormUrlEncoded
    @POST("EP/Token")
    fun refreshToken(
        @Field("grant_type") grantType: String?,
        @Field("refresh_token") refreshToken: String?
    ): Call<RefreshTokenResponse>

    @GET("EP/api/Merchant/GetTerminalAccessList")
    fun terminalAccess(
        @Header("Authorization") authorization: String?,
        @Header("Content-type") contentType: String?,
        @Header("GatewayId") gatewayId: String?,
        @Header("UserId") userId: String?
    ): Call<List<TerminalResponse>>

    @POST("ep/api/Merchant/GetApiKey")
    fun getApikey(
        @Header("Authorization") authorization: String?,
        @Header("Content-type") contentType: String?,
        @Body apiRequest: ApiKeyRequest
    ): Call<String>

    @POST("Transaction")
    fun transaction(
        @Header("x-authorization") fromAPiId: String?,
        @Header("Content-Type") content_Type: String?,
        @Header("Accept") Accept: String?,
        @Body transactionRequest: TransactionRequest
    ): Call<Object>

    @POST("EP/api/Transaction/RetriveTransaction")
    fun retrieveTransactionAPi(
        @Header("GatewayId") gatewayId: String?,
        @Header("Authorization") authorization: String?,
        @Body retrieveTransactionApiRequest: RetrieveTransactionRequest?
    ): Call<List<RetrieveTransactionApiResponse>>

    @GET("EP/api/Transaction/GetTransactionDetails/{transactionID}")
    fun transactionDetail(
        @Path("transactionID") transactionID: String?,
        @Header("Authorization") authorization: String?,
        @Header("Content-Type") content_Type: String?,
        @Header("GatewayId") headGatewayId: String?
    ): Call<TransactionDetailsResponse>

    @GET("EP/api/Users/GetCustomFieldsForEdit/true")
    fun transactionGetCustomFieldsForEdit(
        @Header("Authorization") authorization: String?,
        @Header("GatewayId") GatewayId: String?,
        @Header("Content-Type") content_Type: String?
    ): Call<List<CustomFieldResponse>>

    @GET("EP/api/Users/Get/{userId}")
    fun userProfile(
        @Path("userId") userId: String?,
        @Header("Authorization") authorization: String?,
        @Header("Content-Type") content_Type: String?
        ): Call<UserProfileResponse>
}