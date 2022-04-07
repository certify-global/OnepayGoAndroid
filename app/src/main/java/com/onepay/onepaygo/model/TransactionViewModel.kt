package com.onepay.onepaygo.model

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.request.AdditionalDataFiles
import com.onepay.onepaygo.api.request.CardRequest
import com.onepay.onepaygo.api.request.CustomerRequest
import com.onepay.onepaygo.api.request.TransactionRequest
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.common.*
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.repo.TransactionRepository
import java.text.DecimalFormat
import java.util.ArrayList

class TransactionViewModel : ViewModel() {
    private val TAG: String = TransactionViewModel::class.java.name
    private lateinit var sharedPreferences: SharedPreferences
    val transactionRep = MutableLiveData<TransactionResponseData>()
    val messageError = MutableLiveData<String>()
   var context :Context? = null
    var latitudeStr: String = ""
    var longitudeStr: String = ""
    private var transactionRepository:TransactionRepository = TransactionRepository()

    fun init(context: Context?) {
        messageError.value = ""
        this.context = context
        this.sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        RetrofitInstance.init(context)
    }
    fun transactionPayment(amountCharge:String,cardNumber:String,cardCVC:String,cardMMYY:String,apiToken :String){
        val gpsTracker = GPSTracker(context!!)
        if (gpsTracker.getLatitude() != 0.0 && gpsTracker.getLongitude() != 0.0) {
            val latitude = DecimalFormat("##.######").format(gpsTracker.getLatitude()).toDouble()
            val longitude = DecimalFormat("##.######").format(gpsTracker.getLongitude()).toDouble()
            latitudeStr = latitude.toString()
            longitudeStr = longitude.toString()
        }
        var marketCode: String
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)
                .equals(Constants.moto)
        )
            marketCode = Constants.marketCode.M.name
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)
                .equals(Constants.retail)
        )
            marketCode = Constants.marketCode.R.name
        else marketCode = Constants.marketCode.E.name

        val transaction = TransactionRequest(
            amountCharge,
            "CC",
            "2",
            Utils.getCurrentDateTime()!!,
            "0",
            "",
            Utils.getLocalIpAddress()!!,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceCode),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceId),
            marketCode,
            Constants.referrerUrl,
            "",
            null,
            getCardData(cardNumber,cardCVC,cardMMYY),
            "",
            null,
            getAdditionalDataFiles(),null)
        transaction(apiToken,transaction,true)

    }

    fun signatureTransaction(transaction_id:String,bitmapScale:Bitmap,token:String) {
        val transactionRequest = TransactionRequest("", "CC", "10", Utils.getCurrentDateTime()!!, "0", transaction_id, Utils.getLocalIpAddress()!!, "", "", "", Constants.referrerUrl,
            "",            null,            null,            "2",            null,            getAdditionalDataFiles(bitmapScale)  ,null      )
        transaction(token,transactionRequest,false)
    }

    fun receiptTransaction(transaction_id:String,token:String,emailReceipt:String) {
        val customerReq = CustomerRequest("","","","","","","","","","","","",AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.email),emailReceipt,"","")
        val transactionRequest = TransactionRequest("", "CC", "10", Utils.getCurrentDateTime()!!, "0", transaction_id, Utils.getLocalIpAddress()!!, "", "", "", Constants.referrerUrl,
            "",            null,            null,            "3",            null,            getReceiptAdditionalDataFiles()  ,customerReq      )
        transaction(token,transactionRequest,false)
    }
    fun transaction(token:String,transactionRequest:TransactionRequest,isUpdateTran:Boolean) {

        transactionRepository.transaction(token,transactionRequest) { isSuccess, response, message ->
            Logger.debug(TAG, response.toString()+" "+message)
            messageError.value = message
            if (isSuccess) {
                if(isUpdateTran)
                TransactionDataSource.addTransactionResponse(response!!)
                transactionRep.value = response
            }else {
                transactionRep.value = response
            }
        }
    }
    private fun getAdditionalDataFiles(bitmapScale:Bitmap): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additional_data = bitmapScale?.let {
            Utils.encodeImage(it)?.let {
                AdditionalDataFiles(
                    Constants.signature,
                    it
                )
            }
        }
        val additional_data_source = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)

        if (additional_data != null) {
            additionalDataFiles.add(additional_data)
        }
        additionalDataFiles.add(additional_data_source)
        return additionalDataFiles
    }

    private fun getReceiptAdditionalDataFiles(): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additional_data_source = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)
        additionalDataFiles.add(additional_data_source)
        return additionalDataFiles
    }
    private fun getCardData(cardNumber:String,cardCVC:String,cardMMYY:String): CardRequest? {
        if (cardNumber.isEmpty()) return null
        var trackData: String
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track1).isNotEmpty())
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track1)
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track2)
                .isNotEmpty()
        )
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track2)
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track3)
                .isNotEmpty()
        )
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track3)
        else trackData = ""
        val card = CardRequest(
            cardNumber,
            cardCVC,
            cardMMYY,
            "",
            trackData,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.entryMode),
            "",
            "",
            "",
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.ksn)
        )

        return card
    }

    private fun getAdditionalDataFiles(): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additional_data = AdditionalDataFiles(
            Constants.user,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId)
        )
        val additional_data_source = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)
        val additional_data_location =
            AdditionalDataFiles(Constants.location, latitudeStr + ";" + longitudeStr)
        additionalDataFiles.add(additional_data)
        additionalDataFiles.add(additional_data_source)
        additionalDataFiles.add(additional_data_location)
        return additionalDataFiles
    }
}