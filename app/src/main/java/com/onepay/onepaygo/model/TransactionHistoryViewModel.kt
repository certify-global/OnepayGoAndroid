package com.onepay.onepaygo.model

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.request.RetrieveTransactionRequest
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.controller.DatabaseController
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.repo.TransactionHistoryRepository

class TransactionHistoryViewModel : ViewModel() {
    val transactionHistoryResponse = MutableLiveData<List<RetrieveTransactionApiResponse>>()
    var transactionHistoryDB = MutableLiveData<List<ReportRecords>>()
    val messageError = MutableLiveData<String>()
    private var transactionRepository: TransactionHistoryRepository = TransactionHistoryRepository()

    fun transactionHistory(sharedPreferences: SharedPreferences, toDate: String, amount: String, cardNumber: String, customerName: String, transactionId: String, username: String, customerId: String, source: String) {
        val retrieveTH = RetrieveTransactionRequest(
            Utils.getDateSearch(toDate), "", toDate, AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId),
            amount, cardNumber, customerName, transactionId, username, customerId, source
        )
        transactionRepository.transactionHistory(
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.gatewayId),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token),
            retrieveTH
        ) { isSuccess, response, message ->
            messageError.value = message
            if (isSuccess) {
                if (response != null) {
                    val transactionList = ArrayList<ReportRecords>()
                    for (item in response) {
                        val itemObj = ReportRecords()
                        itemObj.name = item.Name
                        itemObj.transactionAmount = item.TransactionAmount.toString()
                        itemObj.status = item.Status.toString()
                        itemObj.dateTime = item.DateTime!!.toString()
                        itemObj.dateSearch = Utils.getDateInsert(item.DateTime!!.toString())
                        itemObj.transactionDatetime = item.TransactionDatetime.toString()
                        itemObj.transactionId = item.TransactionId.toString()
                        itemObj.cardType = item.CardType
                        itemObj.last4 = item.Last4.toString()
                        itemObj.batchId = item.BatchId.toString()
                        itemObj.customerId = item.CustomerId.toString()
                        itemObj.firstName = item.FirstName.toString()
                        itemObj.lastName = item.LastName.toString()
                        itemObj.email = item.Email.toString()
                        itemObj.phoneNumber = item.PhoneNumber.toString()
                        itemObj.sourceApplication = item.SourceApplication.toString()
                        itemObj.merchantTerminalID = item.MerchantTerminalID!!
                        itemObj.terminalName = item.TerminalName
                        transactionList.add(itemObj)
                    }
                    try {
                        DatabaseController.instance?.insertRecordToDB(transactionList)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                transactionHistoryResponse.value = response
            }
        }
    }

    fun readingDBData(searchType: Int, dateVal: String, limit: Int, offsetValue: Int, value: String, mTerminalId: Int) {
        val temp = DatabaseController.instance?.DbRecordsSearch(searchType, dateVal, limit, offsetValue, value, mTerminalId)
        transactionHistoryDB.value = temp as List<ReportRecords>
    }
}