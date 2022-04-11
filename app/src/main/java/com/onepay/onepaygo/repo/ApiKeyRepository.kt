package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.ApiKeyRequest
import com.onepay.onepaygo.common.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiKeyRepository {
    private val TAG: String = ApiKeyRepository::class.java.name

    fun apiKey(
        token: String,
        apiKeyRequest: ApiKeyRequest,
        onResult: (isSuccess: Boolean, response: String?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.getApikey(token,"application/json",apiKeyRequest).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                Logger.debug(TAG, call.toString() + response.toString())
                if (response.code() == 401)
                    onResult(false, null, "401")
                else if (response.code() == 200) {
                    onResult(true, response.body(), "")
                } else onResult(false, null, response.message())

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                onResult(false, null, t.message.toString())
            }

        })

    }
}