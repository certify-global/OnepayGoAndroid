package com.onepay.onepaygo.api.response

data class CustomFieldResponse(val Id: Int, val CustUDFNumber: String, var Label: String?, val UDFNumber: Int, val ShowCustomer: Boolean, val GatewayId: Long?, val ShowInTransaction: Boolean, val DefaultValue: String)