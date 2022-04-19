package com.onepay.onepaygo.model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.RetrieveTransactionRequest
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.repo.TransactionHistoryRepository

class TransactionHistoryViewModel : ViewModel() {
    private val TAG: String = TransactionHistoryViewModel::class.java.name
    val transactionHistoryResponse = MutableLiveData<List<RetrieveTransactionApiResponse>>()
    val messageError = MutableLiveData<String>()

    private var transactionRepository: TransactionHistoryRepository = TransactionHistoryRepository()



    fun transactionHistory(sharedPreferences: SharedPreferences) {
      val retrieveTH =  RetrieveTransactionRequest(Utils.getCalculatedDate("yyyy-MM-dd HH:mm:ss:SSS", -7),AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.gatewayterminalId),Utils.getCurrentFromDate(),AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.userId))
        transactionRepository.transactionHistory(
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.gatewayId),
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.access_token),
            retrieveTH
        ) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+",message "+message)
            messageError.value = message
            if (isSuccess) {
                if(response != null)
                TransactionHistoryDataSource.setTransactionHistory(response)
                transactionHistoryResponse.value = response

            }
        }
    }
}