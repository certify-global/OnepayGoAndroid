package com.onepay.miura.activity

import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onepay.miura.adapter.DevicesAdapter
import com.onepay.miura.data.AppSharedPreferences
import com.onepay.miura.databinding.ActivityDevicesBinding
import com.onepay.miura.tdynamo.TDynamoUtils
import java.util.*

class TDynamoDeviceActivity : AppCompatActivity(), TDynamoUtils.TDynamoCallbackListener {
    var sharedPreferences: SharedPreferences? = null
    private lateinit var binding: ActivityDevicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)
        TDynamoUtils.getInstance().init(this)
        TDynamoUtils.getInstance().setPaymentMode(false)
        TDynamoUtils.getInstance().setCallbackListener(this)
        TDynamoUtils.getInstance().scanLeDevice(true, this)

    }

    override fun onTDynamoSuccess(foundDevicestdynamo: ArrayList<BluetoothDevice>?) {
        val devicesAdapter = DevicesAdapter(foundDevicestdynamo!!)
        binding.devicesRvPaired.adapter = devicesAdapter
    }

    override fun onTDynamoFailure(responseMsg: String?) {
        TODO("Not yet implemented")
    }

}