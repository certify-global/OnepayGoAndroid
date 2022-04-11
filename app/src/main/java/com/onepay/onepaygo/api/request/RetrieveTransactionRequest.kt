package com.onepay.onepaygo.api.request

import java.io.Serializable

data class RetrieveTransactionRequest (val Fromdate : String, val MerchantTerminalId : String,val Todate : String, val UserId : String) : Serializable