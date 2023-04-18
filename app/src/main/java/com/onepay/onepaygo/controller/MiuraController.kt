package com.onepay.onepaygo.controller

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import com.miurasystems.mpi.api.executor.MiuraManager
import com.miurasystems.mpi.events.MpiEventHandler
import com.miurasystems.mpi.tlv.CardData
import com.onepay.miura.api.ConnectApi
import com.onepay.miura.api.ManualTransactionApi
import com.onepay.miura.api.TransactionApi
import com.onepay.miura.bluetooth.BluetoothModule
import com.onepay.miura.bluetooth.BluetoothPairing
import com.onepay.miura.common.Constants
import com.onepay.miura.data.ConnectApiData
import com.onepay.miura.data.TransactionApiData
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MiuraController {
    private var context: Context? = null
    private var listener: MiuraCallbackListener? = null
    private var sharedPreferences: SharedPreferences? = null
    private var transactionApi: TransactionApi? = null
    var isManualEbt : Boolean = false
    var isEbt : Boolean = false

    interface MiuraCallbackListener {
        fun onCardStatusChanged()
        fun onMiuraSuccess(transactionApiData: TransactionApiData?)
        fun onMiuraFailure(responseMsg: String?)
        fun onError(errorMessage: String?)
    }

    fun init(context: Context?) {
        this.context = context
        isManualEbt = false
        isEbt = false
        this.transactionApi = null
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)
    }

    fun setCallbackListener(callbackListener: MiuraCallbackListener?) {
        listener = callbackListener
    }

    fun MiuraPairing(amountSetting: String, isEbt: Boolean) {
        try {
            var pairB: BluetoothDevice? = null
            val bluetoothPairing = BluetoothPairing(context)
            val pairedDevices = bluetoothPairing.pairedDevices
            this.isEbt = isEbt
            if (pairedDevices.size > 0) {
                for (i in pairedDevices.indices) {
                    if (pairedDevices[i].address == AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.bluetoothAddress)) {
                        pairB = pairedDevices[i]
                        break
                    }
                }
                if (pairB == null) return
                val bluetoothModule = BluetoothModule.getInstance()
                BluetoothModule.getInstance().setSelectedBluetoothDevice(pairB)
                if (bluetoothModule.isSessionOpen) {
                    getTransactionData(amountSetting, pairB, isEbt)
                } else {
                    val finalPairB: BluetoothDevice = pairB
                    ConnectApi.getInstance().setConnectListener { connectApiData: ConnectApiData? ->
                        if (connectApiData != null) {
                            if (connectApiData.returnReason() == Constants.SuccessReason) {
                                getTransactionData(amountSetting, finalPairB, isEbt)
                            } else {
                                if (listener != null) {
                                    listener!!.onError(connectApiData.returnReason())
                                }
                            }
                        }
                    }
                    ConnectApi.getInstance().connect(pairB.address, 60)
                }
            }
        } catch (e: Exception) {
            Logger.error(TAG, "MiuraPairing = " + e.message)
        }
    }

    private fun getTransactionData(amountSetting: String, pairB: BluetoothDevice, isEbt: Boolean) {
        if (!isManualEbt) {
            emvTapSwipeTransaction(amountSetting, pairB, isEbt)
        } else {
            manualEbtTransaction(amountSetting, pairB, isEbt)
        }
    }

    private fun emvTapSwipeTransaction(amountSetting: String, pairB: BluetoothDevice, isEbt: Boolean) {
        try {
            transactionApi = TransactionApi()
            val amount = amountSetting.replace(",", "")
            transactionApi?.setTransactionParams(
                amount.toDouble(),
                "Testing",
                pairB.address,
                true,
                isEbt,
                125
            )
            transactionApi?.performTransaction { transactionApiData ->
                if (transactionApiData.returnStatus() == 1) {
                    if (listener != null) listener!!.onMiuraSuccess(transactionApiData)
                } else {
                    Logger.toast(context, transactionApiData.returnReason())
                    listener!!.onError(transactionApiData.returnReason())
                }
            }
            val mCardEventHandler: MpiEventHandler<CardData>
            mCardEventHandler = MpiEventHandler { cardData: CardData? ->
                if (listener != null) {
                    listener!!.onCardStatusChanged()
                }
            }
            MiuraManager.getInstance().mpiEvents.CardStatusChanged.register(mCardEventHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun manualEbtTransaction(amount: String, pairB: BluetoothDevice, isEbt: Boolean) {
        ManualTransactionApi.getInstance().setManualTransactionParams(amount.toDouble(), "Testing", pairB.address, 125, isEbt, false)
        ManualTransactionApi.getInstance().performManualTransaction { transactionApiData ->
            if (transactionApiData.returnStatus() == 1) {
                if (listener != null) listener!!.onMiuraSuccess(transactionApiData)
            } else {
                listener!!.onError(transactionApiData.returnReason())
            }

        }
        val mCardEventHandler: MpiEventHandler<CardData>
        mCardEventHandler = MpiEventHandler { cardData: CardData? ->
            if (listener != null) {
                listener!!.onCardStatusChanged()
            }
        }
        MiuraManager.getInstance().mpiEvents.CardStatusChanged.register(mCardEventHandler)
    }

    fun cancelTransaction() {
        runBlocking(Dispatchers.IO) {
            if (!isManualEbt) {
                transactionApi?.cancelTransaction()
            }
        }
        if (isManualEbt) {
            ManualTransactionApi.getInstance().cancelTransaction()
        }
    }

    companion object {
        private val TAG = MiuraController::class.java.simpleName
        var instance: MiuraController? = null
            get() {
                if (field == null) {
                    field = MiuraController()
                }
                return field
            }
            private set
    }
}