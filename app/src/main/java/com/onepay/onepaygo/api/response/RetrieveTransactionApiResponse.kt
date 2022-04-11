package com.onepay.onepaygo.api.response

data class RetrieveTransactionApiResponse(val Name: String, var TransactionAmount: String?, var Status: String?, var DateTime: String?, var TransactionId: String?,
    var CardType: String, var Last4: String?, var BatchId: String?
)