package com.onepay.miura.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.miura.api.RetrofitInstance
import com.onepay.miura.api.response.LoginResponse
import com.onepay.miura.api.response.TerminalResponse
import com.onepay.miura.common.Logger
import com.onepay.miura.repo.LoginRepository
import com.onepay.miura.repo.TerminalRepository
import retrofit2.Callback

class TerminalViewModel : ViewModel() {
    private val TAG : String = TerminalViewModel::class.java.name
    val mlTerminalResponse = MutableLiveData<List<TerminalResponse>>()
    val messageError = MutableLiveData<String>()

    private var terminalRepository : TerminalRepository = TerminalRepository()

    fun init(context: Context?) {
        RetrofitInstance.init(context)
    }

    fun terminal( access_token: String, gatewayId: String, userId:String) {
         terminalRepository.terminal( access_token, gatewayId, userId){ isSuccess, response ,message->
             Logger.debug(TAG,response.toString())
             mlTerminalResponse.value = response
             messageError.value = message
         }
    }
}