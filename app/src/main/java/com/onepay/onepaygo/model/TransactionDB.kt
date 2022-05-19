package com.onepay.onepaygo.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = arrayOf("transactionId"), unique = true)])
class TransactionDB {
    @PrimaryKey
    var transactionId: String = ""
    var name: String = ""
    var transactionAmount: String= ""
    var status: String= ""
    var dateTime: String= ""
    var transactionDatetime: String= ""
    var cardType: String= ""
    var last4: String= ""
    var batchId: String= ""
    var customerId: String= ""
    var firstName: String= ""
    var lastName: String= ""
    var email: String= ""
    var phoneNumber: String= ""
    var sourceApplication: String= ""
    var merchantTerminalID: Int= 0
    var terminalName:String= ""
}