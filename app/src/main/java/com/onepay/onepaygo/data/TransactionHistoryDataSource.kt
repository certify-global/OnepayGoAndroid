package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse

object TransactionHistoryDataSource {

    private var transactionHistoryResponseData = arrayListOf<RetrieveTransactionApiResponse>()


    fun setTransactionHistory(data: List<RetrieveTransactionApiResponse>) {
        transactionHistoryResponseData = data as ArrayList<RetrieveTransactionApiResponse>
    }
    fun getTransactionHistoryList() = transactionHistoryResponseData


}