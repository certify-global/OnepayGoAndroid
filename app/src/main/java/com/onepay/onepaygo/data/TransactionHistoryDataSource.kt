package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.CustomFieldResponse
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.model.ReportRecords

object TransactionHistoryDataSource {

    private var transactionHistoryResponseData = arrayListOf<ReportRecords>()
    private var customFieldResponse = arrayListOf<CustomFieldResponse>()
    private var transactionId: ReportRecords? = null
    private var transactionItem: TransactionDetailsResponse? = null


    fun setTransactionHistory(data: List<ReportRecords>) {
        if(data.size == 0) transactionHistoryResponseData.clear()
        else
            transactionHistoryResponseData.addAll(data.sortedByDescending { it.dateTime })
    }

    fun getTransactionHistoryList() = transactionHistoryResponseData

    fun setTransaction(transactionApiId: ReportRecords) {
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