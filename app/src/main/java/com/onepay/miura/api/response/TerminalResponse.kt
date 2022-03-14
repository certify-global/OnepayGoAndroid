package com.onepay.miura.api.response

data class TerminalResponse (val Id : Int, val TerminalName : String, val Active : String?,
                             val TerminalType : String, val GatewayId : Long?, val MerchantName : String, val Processor : Int, val UserId : Int?
)