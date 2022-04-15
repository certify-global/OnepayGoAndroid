package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.api.response.TransactionDetailsResponse

object TransactionHistoryDataSource {

    private var transactionHistoryResponseData = arrayListOf<RetrieveTransactionApiResponse>()
    private var transactionId: RetrieveTransactionApiResponse? = null
    private var transactionItem: TransactionDetailsResponse? = null


    fun setTransactionHistory(data: List<RetrieveTransactionApiResponse>) {
        transactionHistoryResponseData = data as ArrayList<RetrieveTransactionApiResponse>
    }

    fun getTransactionHistoryList() = transactionHistoryResponseData

    fun setTransaction(transactionApiId: RetrieveTransactionApiResponse) {
        transactionId = transactionApiId
    }

    fun setTransactionItem(transactionItem: TransactionDetailsResponse) {
        this.transactionItem = transactionItem
    }

    fun getTransactionItem() = transactionItem
    fun getTransaction() = transactionId
}