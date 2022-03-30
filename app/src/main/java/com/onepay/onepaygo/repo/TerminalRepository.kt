package com.onepay.onepaygo.repo

import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.TerminalResponse
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.data.TerminalDataSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TerminalRepository {
    private val TAG: String = TerminalRepository::class.java.name

    fun terminal(
        access_token: String,
        gatewayId: String,
        userId: String,
        onResult: (isSuccess: Boolean, response: List<TerminalResponse>?, message: String) -> Unit
    ) {
        RetrofitInstance.apiInterface.terminalAccess(
            access_token,
            "application/json",
            gatewayId,
            userId
        )
            .enqueue(object : Callback<List<TerminalResponse>> {
                override fun onResponse(
                    call: Call<List<TerminalResponse>>,
                    response: Response<List<TerminalResponse>>
                ) {
                    Logger.debug(TAG, call.toString() + response.toString())
                    if (response.code() == 401)
                        onResult(true, null, "401")
                    else if (response.code() == 200) {
                        TerminalDataSource.addTerminalList(response.body()!!)
                        onResult(true, response.body(), "")
                    } else onResult(true, null, response.message())

                }

                override fun onFailure(call: Call<List<TerminalResponse>>, t: Throwable) {
                    onResult(false, null, t.message.toString())
                }

            })

    }
}