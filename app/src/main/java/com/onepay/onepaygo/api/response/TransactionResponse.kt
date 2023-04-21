package com.onepay.onepaygo.api.response

import java.io.Serializable

data class TransactionResponseData (val result_code : Int, val transaction_id : String?, val approved_amount:String?,val result_text : String, val auth_code : String?, val account_last_4 : String?, val transaction_datetime : String?,val customerName:String?) : Serializable