package com.onepay.onepaygo.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.TerminalResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.repo.TerminalRepository

class TerminalViewModel : ViewModel() {
    private val TAG: String = TerminalViewModel::class.java.name
    val mlTerminalResponse = MutableLiveData<List<TerminalResponse>>()
    val messageError = MutableLiveData<String>()

    private var terminalRepository: TerminalRepository = TerminalRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun terminal(access_token: String, gatewayId: String, userId: String) {
        terminalRepository.terminal(
            access_token,
            gatewayId,
            userId
        ) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+",message "+message)
            messageError.value = message
            mlTerminalResponse.value = response
        }
    }
}