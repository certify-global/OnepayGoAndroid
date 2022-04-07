package com.onepay.onepaygo.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.ApiKeyRequest
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.repo.ApiKeyRepository

class ApiKeyViewModel : ViewModel() {
    private val TAG: String = ApiKeyViewModel::class.java.name
    val apikey = MutableLiveData<String>()
    val messageError = MutableLiveData<String>()

    private var apiKeyRepository: ApiKeyRepository = ApiKeyRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun apikey(terminalId: String, gatewayId: String,token:String) {
        val apiKeyRequest = ApiKeyRequest(terminalId, gatewayId)
        apiKeyRepository.apiKey(token,apiKeyRequest) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+" "+message)
            messageError.value = message
            if (isSuccess) {
                TransactionDataSource.setApiKey(response.toString())
                apikey.value = response
            }
        }
    }
}