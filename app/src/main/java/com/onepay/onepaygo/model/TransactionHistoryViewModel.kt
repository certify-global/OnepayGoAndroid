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


//AppSharedPreferences.readInt(sharedPreferences,PreferencesKeys.terminalValuesId).toString()
    fun transactionHistory(sharedPreferences: SharedPreferences,toDate:String, amount : String,  cardNumber : String, customerName : String, transactionId : String, username : String, customerId : String,  source : String) {
      val retrieveTH =  RetrieveTransactionRequest(Utils.getDateSearch(toDate),"",toDate,AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.userId),
          amount,cardNumber,customerName,transactionId,username,customerId,source)
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