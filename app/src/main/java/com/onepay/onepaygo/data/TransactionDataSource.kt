package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.TransactionResponseData

object TransactionDataSource {

    private var transactionResponseData: TransactionResponseData? = null
    private var apikey: String? = null
    private var isRetry: Boolean? = false


    fun addTransactionResponse(data: TransactionResponseData) {
        transactionResponseData = data
    }

    fun setApiKey(data: String) {
        apikey = data
    }

    fun setIsRetry(data: Boolean) {
        isRetry = data
    }

    fun getTransactionResponse() = transactionResponseData
    fun getAPIkey() = apikey
    fun getIsRetry() = isRetry
}