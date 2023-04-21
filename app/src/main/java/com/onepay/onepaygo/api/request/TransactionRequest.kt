package com.onepay.onepaygo.api.request

import java.io.Serializable
import java.util.ArrayList

data class TransactionRequest(
    val amount: String, val method: String, val type:String, val nonce: String, val test: String, val reference_transaction_id:String?, val client_ip: String, val device_code: String, val device_id:String,
    val market_code: String, val referrerUrl: String, val notes:String, val emv: EMVTransactionBody?, val card: CardRequest?, val action_code:String, val custom_fields: ArrayList<AdditionalDataFiles>?, val additional_data: ArrayList<AdditionalDataFiles>,
    val customer:CustomerRequest?) : Serializable

data class EMVTransactionBody (val emv_data : String, val pos_entry_mode : String, val service_code:String) : Serializable

data class CardRequest (val number : String, val code : String, val expiration_date:String,val type : String, val track_data : String, val entry_mode:String,val action_code : String, val default_card : String, val token:String,
                               val ksn : String) : Serializable
data class AdditionalDataFiles(val id:String,val value:String):Serializable
data class CustomerRequest(val first_name:String?,val last_name:String?,val street_1:String?,val street_2:String?,val city:String?,val state:String?,val zip:String?,val country:String?,
                           val phone_number:String?,val company:String?,val customer_id:String?,val invoice_number:String?,val email:String?,val email_receipt:String?,val notes:String?,val action_code:String?,):Serializable