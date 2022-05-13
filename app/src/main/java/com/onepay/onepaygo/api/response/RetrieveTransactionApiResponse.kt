package com.onepay.onepaygo.api.response

data class RetrieveTransactionApiResponse(
    val Name: String,
    var TransactionAmount: String?,
    var Status: String?,
    var DateTime: String?,
    var TransactionDatetime: String?,
    var TransactionId: String?,
    var CardType: String,
    var Last4: String?,
    var BatchId: String?,
    var CustomerId: String?,
    var FirstName: String?,
    var LastName: String?,
    var Email: String?,
    var PhoneNumber: String?,
    var SourceApplication: String?,
    var MerchantTerminalID: Int?,
    var TerminalName:String
)