package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.RetrieveTransactionRequest
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.common.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionHistoryRepository {
    private val TAG: String = TransactionHistoryRepository::class.java.name

    fun transactionHistory(
        gatewayId: String,
        token:String,
        retrieveTrans: RetrieveTransactionRequest,
        onResult: (isSuccess: Boolean, response: List<RetrieveTransactionApiResponse>?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.retrieveTransactionAPi(gatewayId,token, retrieveTrans)
            .enqueue(object : Callback<List<RetrieveTransactionApiResponse>> {
                override fun onResponse(
                    call: Call<List<RetrieveTransactionApiResponse>>,
                    response: Response<List<RetrieveTransactionApiResponse>>
                ) {
                    Logger.debug(TAG, call.toString() + response.toString())
                    if (response.code() == 401)
                        onResult(false, null, "401")
                    else if (response.code() == 200) {
                        onResult(true, response.body(), "")
                    } else onResult(true, null, response.message())

                }

                override fun onFailure(call: Call<List<RetrieveTransactionApiResponse>>, t: Throwable) {
                    onResult(false, null, t.message.toString())
                }

            })

    }
}