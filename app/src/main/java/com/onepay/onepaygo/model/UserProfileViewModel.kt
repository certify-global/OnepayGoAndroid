package com.onepay.onepaygo.model

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.response.UserProfileResponse
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.repo.UserProfileRepository

class UserProfileViewModel : ViewModel() {
    val userProfileLiveData = MutableLiveData<UserProfileResponse>()
    val messageError = MutableLiveData<String>()

    private var userProfileRepository: UserProfileRepository = UserProfileRepository()

    fun userProfile(sharedPreferences: SharedPreferences) {
        userProfileRepository.userProfileDetails(
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token),
        ) { isSuccess, response, message ->
            messageError.value = message
            if (isSuccess) {
                userProfileLiveData.value = response
            }
        }
    }
}