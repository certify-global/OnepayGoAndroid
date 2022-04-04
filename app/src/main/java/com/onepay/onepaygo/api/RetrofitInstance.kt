package com.onepay.onepaygo.api

import android.content.Context
import com.onepay.onepaygo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    lateinit var apiInterface: ApiInterface
    lateinit var apiInterfaceGateway: ApiInterface

    fun init(context: Context?) {
        createRetrofitInstance()
        createRetrofitInstanceGateway()

    }

    private fun createRetrofitInstance() {
        apiInterface = Retrofit.Builder().run {
            baseUrl(BuildConfig.ENDPOINT_URL)
            addConverterFactory(GsonConverterFactory.create())
            client(createOkHttpClient())
            build()
        }.create(ApiInterface::class.java)
    }

    private fun createRetrofitInstanceGateway() {
        apiInterfaceGateway = Retrofit.Builder().run {
            baseUrl(BuildConfig.GATEWAY_URL)
            addConverterFactory(GsonConverterFactory.create())
            client(createOkHttpClient())
            build()
        }.create(ApiInterface::class.java)
    }
    private fun createOkHttpClient(): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val requestOriginal = chain.request()
                val requestBuilder = requestOriginal.newBuilder().apply {
                    //header("Accept", "application/json")
                    header("Content-Type", "application/x-www-form-urlencoded")
                    // header("Authorization", "Bearer " ) //TODO
                    method(requestOriginal.method, requestOriginal.body)
                }
                chain.proceed(requestBuilder.build())
            }
        }
        val logsHttp = HttpLoggingInterceptor()
        logsHttp.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(logsHttp)
        return okHttpClient.build()
    }
}