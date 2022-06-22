package com.onepay.onepaygo.activity

import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.onepay.miura.bluetooth.BluetoothDeviceChecking
import com.onepay.miura.bluetooth.BluetoothModule
import com.onepay.onepaygo.adapter.DevicesAdapter
import com.onepay.onepaygo.callback.ItemSelectedInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.databinding.ActivityDevicesBinding
import java.util.*


class MiuraDeviceActivity : AppCompatActivity(), ItemSelectedInterface {

    var sharedPreferences: SharedPreferences? = null
    private lateinit var binding: ActivityDevicesBinding
    private var pairedDevices: ArrayList<BluetoothDevice>? = null
    private var nonPairedDevices: ArrayList<BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)
        loadDevices()

    }


    private fun loadDevices() {

        BluetoothModule.getInstance().getBluetoothDevicesWithChecking(
            baseContext, BluetoothDeviceChecking.Mode.noChecking
        ) { pairedDevicesT, nonPairedDevicesT ->
            pairedDevices = pairedDevicesT
            nonPairedDevices = nonPairedDevicesT
            if (pairedDevices?.size!! > 0) {
                showPairedDevices(pairedDevices)
            }
            if (nonPairedDevices?.size!! > 0) {
                showAvailableDevices(nonPairedDevices)
            }
        }
    }

    private fun showPairedDevices(pairedDevices: ArrayList<BluetoothDevice>?) {
        val devicesAdapter = DevicesAdapter(pairedDevices!!, this, "selected")
        if (pairedDevices.size > 0)
            binding.devicesTvPairedMsg.visibility = View.GONE
        else binding.devicesTvPairedMsg.visibility = View.VISIBLE
        binding.devicesRvPaired.adapter = devicesAdapter
    }

    private fun showAvailableDevices(pairedDevices: ArrayList<BluetoothDevice>?) {
        val devicesAdapter = DevicesAdapter(pairedDevices!!, this, "available")
        binding.devicesRvNonPaired.adapter = devicesAdapter
    }

    override fun onItemSelected(pos: Int, bluetoothDevice:  BluetoothDevice?) {
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.deviceStatus, true)
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.selectedDevice, Constants.DeviceType.MIURA.name)
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.bluetoothAddress, bluetoothDevice?.address)
            BluetoothModule.getInstance().setDefaultDevice(this, bluetoothDevice)
            finish()

    }

}