package com.onepay.onepaygo.model

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.response.CustomFieldResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.repo.CustomFieldsRepository

class CustomFieldViewModel : ViewModel() {
    private val TAG: String = CustomFieldViewModel::class.java.name
    val customFieldResponse = MutableLiveData<List<CustomFieldResponse>>()
    val messageError = MutableLiveData<String>()

    private var customFieldsRepository: CustomFieldsRepository = CustomFieldsRepository()



    fun customFieldEdit(sharedPreferences: SharedPreferences) {
        customFieldsRepository.customFields(
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.access_token),
            AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.gatewayId),
            ) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+",message "+message)
            messageError.value = message
            if (isSuccess) {
                if(response != null)
                TransactionHistoryDataSource.setCustomFieldResponse(response)
                customFieldResponse.value = response
            }
        }
    }
}