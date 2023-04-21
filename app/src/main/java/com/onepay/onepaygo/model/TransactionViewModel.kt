package com.onepay.onepaygo.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.request.*
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.common.*
import com.onepay.onepaygo.controller.MiuraController
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.repo.TransactionRepository
import java.text.DecimalFormat
import java.util.ArrayList

@SuppressLint("StaticFieldLeak")
class TransactionViewModel : ViewModel() {
    private val TAG: String = TransactionViewModel::class.java.name
    private lateinit var sharedPreferences: SharedPreferences
    val transactionRep = MutableLiveData<TransactionResponseData>()
    val messageError = MutableLiveData<String>()
    private lateinit var context: Context
    var latitudeStr: String = ""
    var longitudeStr: String = ""
    private var transactionRepository: TransactionRepository = TransactionRepository()

    fun init(context: Context) {
        messageError.value = ""
        this.context = context
        this.sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        if (sharedPreferences.getString(PreferencesKeys.deviceCode, Constants.DeviceType.MIURA.name) == Constants.DeviceType.MIURA.name) {
            MiuraController.instance?.init(context)
        }
    }

    fun transactionPayment(
        amountCharge: String,
        cardNumber: String,
        cardCVC: String,
        cardMMYY: String,
        apiToken: String,
        fName: String,
        lName: String,
        customerId: String,
        InvoiceNumber: String,
        note: String
    ) {
        val gpsTracker = GPSTracker(context)
        if (gpsTracker.getLatitude() != 0.0 && gpsTracker.getLongitude() != 0.0) {
            val latitude = DecimalFormat("##.######").format(gpsTracker.getLatitude()).toDouble()
            val longitude = DecimalFormat("##.######").format(gpsTracker.getLongitude()).toDouble()
            latitudeStr = latitude.toString()
            longitudeStr = longitude.toString()
        }
        val marketCode: String
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues).equals(Constants.moto))
            marketCode = Constants.marketCode.M.name
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues).equals(Constants.retail))
            marketCode = Constants.marketCode.R.name
        else marketCode = Constants.marketCode.E.name

        val transaction = TransactionRequest(
            amountCharge,
            getTransactionMethod(),
            Constants.Type.AuthandCapture.value.toString(),
            Utils.getCurrentDateTime(),
            Constants.Test.LiveZero.value.toString(),
            "",
            Utils.getLocalIpAddress()!!,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceCode),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceId),
            marketCode,
            Constants.referrerUrl,
            "",
            getEMVTransactions(),
            getCardData(cardNumber, cardCVC, cardMMYY),
            Constants.ActionCode.ActionCodeEmpty.value,
            null,
            getAdditionalDataFiles(), getCustomerFields(fName, lName, customerId, InvoiceNumber, note)
        )
        transaction(apiToken, transaction, true)

    }

    fun signatureTransaction(transaction_id: String, bitmapScale: Bitmap, token: String) {
        val transactionRequest = TransactionRequest(
            "",
            getTransactionMethod(),
            Constants.Type.SignatureEmail.value.toString(),
            Utils.getCurrentDateTime(),
            Constants.Test.LiveZero.value.toString(),
            transaction_id,
            Utils.getLocalIpAddress()!!,
            "",
            "",
            "",
            Constants.referrerUrl,
            "",
            null,
            null,
            Constants.ActionCode.ActionCode_2.value,
            null,
            getAdditionalDataFiles(bitmapScale),
            null
        )
        transaction(token, transactionRequest, false)
    }

    fun receiptTransaction(transaction_id: String?, token: String, emailReceipt: String) {
        val customerReq = CustomerRequest(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            emailReceipt,//AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.email),
            emailReceipt,
            "",
            ""
        )
        val transactionRequest = TransactionRequest(
            "",
            getTransactionMethod(),
            Constants.Type.SignatureEmail.value.toString(),
            Utils.getCurrentDateTime(),
            Constants.Test.LiveZero.value.toString(),
            transaction_id,
            Utils.getLocalIpAddress()!!,
            "",
            "",
            "",
            Constants.referrerUrl,
            "",
            null,
            null,
            Constants.ActionCode.ActionCode_3.value,
            null,
            getReceiptAdditionalDataFiles(),
            customerReq
        )
        transaction(token, transactionRequest, false)
    }

    fun refundVoidTransaction(transaction_id: String, token: String, type: String, amount: String, AccountNumberLast4: String) {
        val card = CardRequest(
            AccountNumberLast4,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        )
        val transactionRequest = TransactionRequest(
            amount,
            Constants.MethodType.CC.name,
            type,
            Utils.getCurrentDateTime(),
            Constants.Test.LiveZero.value.toString(),
            transaction_id,
            Utils.getLocalIpAddress()!!,
            "",
            "",
            "",
            Constants.referrerUrl,
            "",
            null,
            card,
            Constants.ActionCode.ActionCodeEmpty.value,
            getAdditionalDataFiles(),
            getReceiptAdditionalDataFiles(),
            null
        )
        transaction(token, transactionRequest, false)
    }

    fun transaction(token: String, transactionRequest: TransactionRequest, isUpdateTran: Boolean) {

        transactionRepository.transaction(
            token,
            transactionRequest
        ) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString() + " " + message)
            messageError.value = message
            if (isSuccess) {
                if (isUpdateTran)
                    TransactionDataSource.addTransactionResponse(response!!)
                transactionRep.value = response
            } else {
                transactionRep.value = response
            }
        }
    }

    private fun getAdditionalDataFiles(bitmapScale: Bitmap): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additionalData = bitmapScale.let {
            Utils.encodeImage(it)?.let {
                AdditionalDataFiles(Constants.signature, it)
            }
        }
        val additionalDataSource = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)

        if (additionalData != null) {
            additionalDataFiles.add(additionalData)
        }
        additionalDataFiles.add(additionalDataSource)
        return additionalDataFiles
    }

    private fun getReceiptAdditionalDataFiles(): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additionalDataSource = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)
        additionalDataFiles.add(additionalDataSource)
        return additionalDataFiles
    }

    private fun getCardData(cardNumber: String, cardCVC: String, cardMMYY: String): CardRequest? {
        if (getEMVTransactions() != null) return null
        val trackData: String
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track1).isNotEmpty())
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track1)
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track2).isNotEmpty())
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track2)
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track3).isNotEmpty())
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track3)
        else trackData = ""
        return CardRequest(cardNumber, cardCVC, cardMMYY, "", trackData, AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.entryMode), "", "", "", AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.ksn))
    }

    private fun getAdditionalDataFiles(): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additionalData = AdditionalDataFiles(Constants.user, AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId))
        val additionalDataSource = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)
        val additionalDataLocation = AdditionalDataFiles(Constants.location, "$latitudeStr;$longitudeStr")
        additionalDataFiles.add(additionalData)
        additionalDataFiles.add(additionalDataSource)
        additionalDataFiles.add(additionalDataLocation)
        return additionalDataFiles
    }

    private fun getEMVTransactions(): EMVTransactionBody? {
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.arqc).isEmpty())
            return null
        else {
            val deviceCode = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceCode)
            var pos_entry_mode = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.pos)
            if (deviceCode == Constants.DeviceType.MIURA.name) {
                pos_entry_mode = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.entryMode)
            }
            return EMVTransactionBody(
                AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.arqc),
                pos_entry_mode,
                AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.serviceCode)
            )
        }
    }

    private fun getCustomerFields(
        fName: String,
        lName: String,
        customerId: String,
        InvoiceNumber: String,
        note: String
    ): CustomerRequest? {
        try {
            return CustomerRequest(
                fName, lName, "", "", "", "", "", "", "", "", customerId, InvoiceNumber, "", "", note, ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getTransactionMethod() : String {
        var transactionMethod = Constants.MethodType.CC.name
        if (AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.isdebit)) {
            transactionMethod = Constants.MethodType.DB.name
        }
        return transactionMethod
    }
}