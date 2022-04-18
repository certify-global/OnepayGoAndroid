package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.TransactionResponseData

object TransactionDataSource {

    private var transactionResponseData: TransactionResponseData? = null
    private var apikey: String? = null
    private var isRetry: Boolean? = false
    private var isChargeFragment: Boolean? = false
    private var isHome: Boolean? = true
    private var amount: String? = ""




    fun addTransactionResponse(data: TransactionResponseData) {
        transactionResponseData = data
    }

    fun setApiKey(data: String) {
        apikey = data
    }

    fun setIsRetry(data: Boolean) {
        isRetry = data
    }
    fun setIsChargeFragment(data: Boolean) {
        isChargeFragment = data
    }
    fun setIsHome(data: Boolean) {
        isHome = data
    }
    fun setAmount(data: String) {
        amount = data
    }
    fun getTransactionResponse() = transactionResponseData

    fun getAPIkey() = apikey

    fun getIsRetry() = isRetry

    fun getIsChargeFragment() = isChargeFragment

    fun getIsHome() = isHome

    fun getAmount() = amount
}