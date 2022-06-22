package com.onepay.onepaygo.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.response.TerminalResponse
import com.onepay.onepaygo.repo.TerminalRepository

class TerminalViewModel : ViewModel() {
    val mlTerminalResponse = MutableLiveData<List<TerminalResponse>>()
    val messageError = MutableLiveData<String>()

    private var terminalRepository: TerminalRepository = TerminalRepository()

    fun terminal(access_token: String, gatewayId: String, userId: String) {
        terminalRepository.terminal(access_token, gatewayId, userId) { _, response, message ->
            messageError.value = message
            mlTerminalResponse.value = response
        }
    }
}