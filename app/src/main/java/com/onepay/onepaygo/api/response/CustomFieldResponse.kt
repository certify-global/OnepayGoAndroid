package com.onepay.onepaygo.api.response

data class CustomFieldResponse (val Id : Int, val CustUDFNumber : String, var Label : String?, val UDFNumber : Int,
                                val ShowCustomer : Boolean, val GatewayId : Long?, val ShowInTransaction : Boolean, val DefaultValue : String
)
//[{
//    "Id": 5193,
//    "GatewayId": 100070,
//    "UDFNumber": 1,
//    "CustUDFNumber": "1",
//    "Label": "Payer",
//    "Type": {
//        "CustomerProfile": true,
//        "TransactionDetails": true,
//        "PayPage": true
//    },
//    "ShowCustomer": true,
//    "ShowInTransaction": true,
//    "Required": false,
//    "ShowOnReciept": false,
//    "CreatedDate": "2022-04-21T07:13:41.4997886",
//    "CreatedBy": 524,
//    "UpdatedDate": null,
//    "UpdatedBy": null,
//    "Active": true,
//    "DefaultValue": "payer",
//    "Editable": false
//}