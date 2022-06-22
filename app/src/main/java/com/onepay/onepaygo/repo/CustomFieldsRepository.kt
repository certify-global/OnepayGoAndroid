package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.CustomFieldResponse
import com.onepay.onepaygo.common.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomFieldsRepository {
    private val TAG: String = CustomFieldsRepository::class.java.name

    fun customFields(
        token: String,
        gatewayId: String,
        onResult: (isSuccess: Boolean, response: List<CustomFieldResponse>?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.transactionGetCustomFieldsForEdit(token, gatewayId, "application/json").enqueue(object : Callback<List<CustomFieldResponse>> {
            override fun onResponse(
                call: Call<List<CustomFieldResponse>>,
                response: Response<List<CustomFieldResponse>>
            ) {
                Logger.debug(TAG, call.toString() + response.toString())
                if (response.code() == 401)
                    onResult(false, null, response.message())
                else if (response.code() == 200) {
                    onResult(true, response.body(), response.message())

                } else onResult(false, null, response.message())

            }

            override fun onFailure(call: Call<List<CustomFieldResponse>>, t: Throwable) {
                onResult(false, null, t.message.toString())
            }

        })

    }
}