package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository {

    fun login(
        username: String,
        password: String,
        onResult: (isSuccess: Boolean, response: LoginResponse?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.loginUserWith("application/x-www-form-urlencoded", "password", username, password)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.code() == 200)
                        onResult(true, response.body(), "")
                    else onResult(true, null, response.message())

                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    onResult(false, null, t.message.toString())
                }

            })

    }
}