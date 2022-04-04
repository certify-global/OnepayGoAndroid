package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.TransactionResponseData

object TransactionDataSource {

   private var transactionResponseData : TransactionResponseData? = null

    fun addTransactionResponse(data: TransactionResponseData) {
         transactionResponseData = data
    }


    fun getTransactionResponse() = transactionResponseData

}