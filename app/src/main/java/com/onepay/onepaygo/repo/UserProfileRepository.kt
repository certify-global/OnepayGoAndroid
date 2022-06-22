package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileRepository {

    fun userProfileDetails(
        transactionID: String,
        token: String,
        onResult: (isSuccess: Boolean, response: UserProfileResponse?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.userProfile(transactionID, token, "application/x-www-form-urlencoded")
            .enqueue(object : Callback<UserProfileResponse> {
                override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                    if (response.code() == 401)
                        onResult(false, null, "401")
                    else if (response.code() == 200) {
                        onResult(true, response.body(), "")
                    } else onResult(true, null, response.message())

                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                    onResult(false, null, t.message.toString())
                }

            })

    }
}