package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.LoginResponse
import com.onepay.onepaygo.common.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository {
    private val TAG: String = LoginRepository::class.java.name

    fun login(
        username: String,
        password: String,
        onResult: (isSuccess: Boolean, response: LoginResponse?,message:String) -> Unit
    ) {
       // val loginRequest = LoginRequest(username, password, "password")
//        RetrofitInstance.apiInterface.loginUser(loginRequest)
//            .enqueue(object : Callback<LoginResponse> {
//                override fun onResponse(
//                    call: Call<LoginResponse>,
//                    response: Response<LoginResponse>
//                ) {
//                    Logger.debug(TAG, call.toString() + response.toString())
//                    onResult(true, response.body())
//
//                }
//
//                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                    onResult(false, null)
//                }
//
//            })
        RetrofitInstance.apiInterface.loginUserWith("application/x-www-form-urlencoded", "password", username, password)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    Logger.debug(TAG, call.toString() + response.toString())
                    if(response.code()==200)
                    onResult(true, response.body(),"")
                    else onResult(true,null, response.message())

                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    onResult(false, null,t.message.toString())
                }

            })

    }
}