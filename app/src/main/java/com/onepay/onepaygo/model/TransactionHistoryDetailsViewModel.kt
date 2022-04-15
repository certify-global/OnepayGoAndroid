package com.onepay.onepaygo.model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.repo.TransactionDetailsRepository

class TransactionHistoryDetailsViewModel : ViewModel() {
    private val TAG: String = TransactionHistoryDetailsViewModel::class.java.name
    val transactionHistoryResponse = MutableLiveData<TransactionDetailsResponse>()
    val messageError = MutableLiveData<String>()

    private var transactionRepository: TransactionDetailsRepository = TransactionDetailsRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun transactionHistory(sharedPreferences: SharedPreferences,transactionId: String) {
        transactionRepository.transactionHistoryDetails(
            transactionId,
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.access_token),
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.gatewayId)
        ) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+",message "+message)
            messageError.value = message
            if (isSuccess) {
                TransactionHistoryDataSource.setTransactionItem(response!!)
                transactionHistoryResponse.value = response

            }
        }
    }
}