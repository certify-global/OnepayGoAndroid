package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.CustomFieldResponse
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.model.ReportRecords

object TransactionHistoryDataSource {

    private var transactionHistoryResponseData = arrayListOf<ReportRecords>()
    private var customFieldResponse = arrayListOf<CustomFieldResponse>()
    private var transactionId: ReportRecords? = null
    private var transactionItem: TransactionDetailsResponse? = null


    fun setTransactionHistory(data: List<ReportRecords>) {
        if(data.size == 0) transactionHistoryResponseData.clear()
        else transactionHistoryResponseData.addAll(data.sortedByDescending { it.dateTime })
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

    fun searchFilter(searchType: Int, value: String,mTerminalId :Int): ArrayList<ReportRecords>? {
        var terminalList: ArrayList<ReportRecords>
        when (searchType) {
            Constants.SearchType.All.value -> {
                return transactionHistoryResponseData
            }
            Constants.SearchType.TransactionID.value -> {

                terminalList = transactionHistoryResponseData.filter {
                    it.transactionId.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.FirstName.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.firstName!!.contains(value, true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.LastName.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.lastName!!.contains(value, true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.CustomerID.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.customerId.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.Email.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.email.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.Phone.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.phoneNumber.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.TransactionAmount.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.transactionAmount.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.CardLast4Digits.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.last4.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
            Constants.SearchType.SourceApplication.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.sourceApplication.toString().contains(value,true) && it.merchantTerminalID == mTerminalId
                } as ArrayList<ReportRecords>
                return terminalList
            }
        }

        return null
    }
}