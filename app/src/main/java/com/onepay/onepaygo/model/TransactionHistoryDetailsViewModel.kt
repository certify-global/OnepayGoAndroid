package com.onepay.onepaygo.model

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.repo.TransactionDetailsRepository

class TransactionHistoryDetailsViewModel : ViewModel() {
    val transactionHistoryResponse = MutableLiveData<TransactionDetailsResponse>()
    val messageError = MutableLiveData<String>()

    private var transactionRepository: TransactionDetailsRepository = TransactionDetailsRepository()

    fun transactionHistory(sharedPreferences: SharedPreferences,transactionId: String) {
        transactionRepository.transactionHistoryDetails(
            transactionId,
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.access_token),
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.gatewayId)
        ) { isSuccess, response, message ->
            messageError.value = message
            if (isSuccess) {
                TransactionHistoryDataSource.setTransactionItem(response!!)
                transactionHistoryResponse.value = response

            }
        }
    }
}