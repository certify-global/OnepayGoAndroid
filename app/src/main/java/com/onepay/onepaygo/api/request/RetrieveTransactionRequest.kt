package com.onepay.onepaygo.api.request

import java.io.Serializable

data class RetrieveTransactionRequest (val Fromdate : String, val MerchantTerminalId : String,val Todate : String, val UserId : String,val Amount : String, val CardNumber : String,val CustomerName : String, val TransactionId : String, val Username : String,val CustomerId : String, val Source : String) : Serializable