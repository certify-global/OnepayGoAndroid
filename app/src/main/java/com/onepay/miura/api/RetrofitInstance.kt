package com.onepay.miura.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    lateinit var apiInterface : ApiInterface

    fun init (context: Context?) {
        createRetrofitInstance(context)
    }

    private fun createRetrofitInstance(context: Context?) {
        apiInterface = Retrofit.Builder().run {
            baseUrl("")
            addConverterFactory(GsonConverterFactory.create())
            client(createOkHttpClient())
            build()
        }.create(ApiInterface::class.java)
    }

    private fun createOkHttpClient() : OkHttpClient {
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val requestOriginal = chain.request()
                val requestBuilder = requestOriginal.newBuilder().apply {
                    header("Accept", "application/json")
                    header("Content-Type", "application/json")
                    header("grant_type", "password")
                    header("Authorization", "Bearer " ) //TODO
                    method(requestOriginal.method(), requestOriginal.body())
                }
                chain.proceed(requestBuilder.build())
            }
        }
        return okHttpClient.build()
    }
}