package com.onepay.onepaygo.activity

import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.onepay.onepaygo.adapter.DevicesAdapter
import com.onepay.onepaygo.callback.ItemSelectedInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.databinding.ActivityDevicesBinding
import com.onepay.onepaygo.tdynamo.TDynamoUtils
import java.util.*

class TDynamoDeviceActivity : AppCompatActivity(), TDynamoUtils.TDynamoCallbackListener,
    ItemSelectedInterface {
    private val TAG: String = TDynamoDeviceActivity::class.java.name
    var sharedPreferences: SharedPreferences? = null
    private lateinit var binding: ActivityDevicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)
        TDynamoUtils.getInstance().init(this, this)
        TDynamoUtils.getInstance().setPaymentMode(false)
        TDynamoUtils.getInstance().setCallbackListener(this)
        TDynamoUtils.getInstance().scanLeDevice(true, this)

    }

    override fun onTDynamoSuccess(foundDevicestdynamo: ArrayList<BluetoothDevice>?) {
        Logger.debug(TAG, "" + foundDevicestdynamo?.size)
        binding.devicesTvPairedMsg.visibility = View.GONE
        val devicesAdapter = DevicesAdapter(foundDevicestdynamo!!, this, "available")
        binding.devicesRvPaired.adapter = devicesAdapter
    }

    override fun onTDynamoFailure(responseMsg: String?) {
        Logger.error(TAG, responseMsg.toString())
    }

    override fun onItemSelected(pos: Int, bluetoothDevice: BluetoothDevice?) {
        TDynamoUtils.getInstance().openDevice(this)
        TDynamoUtils.getInstance().stopScanning()
        AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.deviceStatus, true)
        AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.selectedDevice, Constants.DeviceType.MAGTEK.name)
    }

}