package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.CustomFieldResponse
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.api.response.TransactionDetailsResponse

object TransactionHistoryDataSource {

    private var transactionHistoryResponseData = arrayListOf<RetrieveTransactionApiResponse>()
    private var customFieldResponse = arrayListOf<CustomFieldResponse>()
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
    fun setCustomFieldResponse(data: List<CustomFieldResponse>) {
        this.customFieldResponse = data as ArrayList<CustomFieldResponse>
    }


    fun getTransactionItem() = transactionItem
    fun getTransaction() = transactionId
    fun getCustomFieldList() = customFieldResponse
}