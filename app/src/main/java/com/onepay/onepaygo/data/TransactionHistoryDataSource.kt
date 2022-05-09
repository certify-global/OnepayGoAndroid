package com.onepay.onepaygo.data

import com.onepay.onepaygo.api.response.CustomFieldResponse
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.common.Constants

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

    fun searchFilter(searchType: Int, value: String): ArrayList<RetrieveTransactionApiResponse>? {
        var terminalList: ArrayList<RetrieveTransactionApiResponse>
        when (searchType) {
            Constants.SearchType.All.value -> {
                return transactionHistoryResponseData
            }
            Constants.SearchType.TransactionID.value -> {

                terminalList = transactionHistoryResponseData.filter {
                    it.TransactionId.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.FirstName.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.FirstName!!.contains(
                        value,
                        true
                    )
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.LastName.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.LastName!!.contains(
                        value,
                        true
                    )
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.CustomerID.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.CustomerId.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.Email.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.Email.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.Phone.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.PhoneNumber.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.TransactionAmount.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.TransactionAmount.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.CardLast4Digits.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.Last4.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
            Constants.SearchType.SourceApplication.value -> {
                terminalList = transactionHistoryResponseData.filter {
                    it.SourceApplication.toString().contains(value,true)
                } as ArrayList<RetrieveTransactionApiResponse>
                return terminalList
            }
        }

        return null
    }
}