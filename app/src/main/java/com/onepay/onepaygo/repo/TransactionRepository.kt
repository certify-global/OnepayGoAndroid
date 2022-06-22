package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.TransactionRequest
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionRepository {
    private val TAG: String = TransactionRepository::class.java.name

    fun transaction(
        apiId: String,
        transactionRequest: TransactionRequest,
        onResult: (isSuccess: Boolean, response: TransactionResponseData?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterfaceGateway.transaction(
            apiId, "application/json", "application/json", transactionRequest).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                var json1: JSONObject
                if (response.code() == 401)
                    onResult(false, null, response.message())
                else if (response.code() == 200) {
                    try {
                        json1 = JSONObject(response.body().toString())
                    } catch (e: Exception) {
                        Logger.error(TAG, e.message)
                        json1 = JSONObject("{}")
                    }
                    if (!json1.isNull("transaction_response")) {
                        val transaction = json1.getJSONObject("transaction_response")
                        var name = ""
                        if (!json1.isNull("customer")) {
                            val customer = json1.getJSONObject("customer")
                            name =
                                customer.getString("first_name") + customer.getString("last_name")
                        }
                        val dateValue =
                            Utils.getTransactionDate(transaction.getString("transaction_datetime"))
                        val transactionResponse = TransactionResponseData(
                            transaction.getInt("result_code"),
                            transaction.getString("transaction_id"),
                            transaction.getString("approved_amount"),
                            transaction.getString("result_text"),
                            null,
                            transaction.getString("account_last_4"),
                            dateValue!!,
                            name
                        )
                        onResult(true, transactionResponse, "")
                    } else onResult(true, null, "")
                } else onResult(false, null, response.message())

            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                onResult(false, null, t.message.toString())
            }

        })

    }
}