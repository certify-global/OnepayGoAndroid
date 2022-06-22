package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.RefreshTokenResponse
import com.onepay.onepaygo.common.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefreshTokenRepository {
    fun refreshToken(
        refreshToken: String,
        onResult: (isSuccess: Boolean, response: RefreshTokenResponse?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.refreshToken(Constants.refreshTokenGrantType, refreshToken)
            .enqueue(object : Callback<RefreshTokenResponse> {
                override fun onResponse(
                    call: Call<RefreshTokenResponse>,
                    response: Response<RefreshTokenResponse>
                ) {
                    if (response.code() == 200)
                        onResult(true, response.body(), "")
                    else onResult(true, null, response.message())

                }

                override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                    onResult(false, null, t.message.toString())
                }

            })

    }
}