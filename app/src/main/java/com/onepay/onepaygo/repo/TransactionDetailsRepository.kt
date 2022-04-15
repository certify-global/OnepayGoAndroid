package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.common.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionDetailsRepository {
    private val TAG: String = TransactionDetailsRepository::class.java.name

    fun transactionHistoryDetails(
        transactionID: String,
        token:String,
        headGatewayId: String,
        onResult: (isSuccess: Boolean, response: TransactionDetailsResponse?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.transactionDetail(transactionID,token, "application/x-www-form-urlencoded",headGatewayId)
            .enqueue(object : Callback<TransactionDetailsResponse> {
                override fun onResponse(
                    call: Call<TransactionDetailsResponse>,
                    response: Response<TransactionDetailsResponse>
                ) {
                    Logger.debug(TAG, call.toString() + response.toString())
                    if (response.code() == 401)
                        onResult(false, null, "401")
                    else if (response.code() == 200) {
                        onResult(true, response.body(), "")
                    } else onResult(true, null, response.message())

                }

                override fun onFailure(call: Call<TransactionDetailsResponse>, t: Throwable) {
                    onResult(false, null, t.message.toString())
                }

            })

    }
}