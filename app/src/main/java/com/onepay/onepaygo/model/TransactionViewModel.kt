package com.onepay.onepaygo.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.TransactionRequest
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.repo.TransactionRepository

class TransactionViewModel : ViewModel() {
    private val TAG: String = TransactionViewModel::class.java.name
    val transactionRep = MutableLiveData<TransactionResponseData>()
    val messageError = MutableLiveData<String>()

    private var transactionRepository:TransactionRepository = TransactionRepository()

    fun init(context: Context?) {
        messageError.value = ""
        RetrofitInstance.init(context)
    }

    fun transaction(token:String,transactionRequest:TransactionRequest) {
        transactionRepository.transaction(token,transactionRequest) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+" "+message)
            if (isSuccess) {
                TransactionDataSource.addTransactionResponse(response!!)
                transactionRep.value = response
            }else
            messageError.value = message
        }
    }
}