package com.onepay.onepaygo.model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.LoginResponse
import com.onepay.onepaygo.api.response.RefreshTokenResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.repo.LoginRepository
import com.onepay.onepaygo.repo.RefreshTokenRepository

class RefreshTokenViewModel : ViewModel() {
    private val TAG : String = RefreshTokenViewModel::class.java.name
    val refreshTokenResponse = MutableLiveData<RefreshTokenResponse>()
    val messageError = MutableLiveData<String>()
    var sharedPreferences : SharedPreferences? = null


    private var refreshRepository :  RefreshTokenRepository = RefreshTokenRepository()

    fun init(context: Context?) {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)
    }

    fun refreshToken(refreshToken: String) {
        refreshRepository.refreshToken(refreshToken){isSuccess, response ,message->
            Logger.debug(TAG,response.toString())
            if(isSuccess) {
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.refresh_token,
                    response?.refresh_token
                )
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.access_token,
                    response?.access_token
                )
            }
            messageError.value = message
            refreshTokenResponse.value = response
        }

    }
}