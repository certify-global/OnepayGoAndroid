

package com.onepay.onepaygo.adapter

import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences


class HeaderAdapter(
    var sharedPreferences: SharedPreferences, var sizeTerminal: Int,
    var callBack: CallbackInterface
) : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val switchLocation: SwitchCompat = itemView.findViewById(R.id.switch_location)
        val switchBluetooth: SwitchCompat = itemView.findViewById(R.id.switch_bluetooth)
        val cardTdynamo: CardView = itemView.findViewById(R.id.card_tdynamo)
        val cardMiura: CardView = itemView.findViewById(R.id.card_miura)
        val radioTdynamo: RadioButton = itemView.findViewById(R.id.radio_tdynamo)
        val radioMura: RadioButton = itemView.findViewById(R.id.radio_mura)
        val terminalTitle: TextView = itemView.findViewById(R.id.tv_terminal)

    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.header_settings, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds number of flowers to the header. */
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        try {
            if (sizeTerminal > 0)
                holder.terminalTitle.visibility = View.VISIBLE
            else
                holder.terminalTitle.visibility = View.INVISIBLE
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter == null) {
                holder.switchBluetooth.isChecked = false
// Device does not support Bluetooth
            } else holder.switchBluetooth.isChecked = mBluetoothAdapter.isEnabled
            holder.switchLocation.isChecked =
                AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.locationStatus)
            holder.switchBluetooth.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    Utils.enableBluetooth()
                    AppSharedPreferences.writeSp(
                        sharedPreferences,
                        PreferencesKeys.bluetoothStatus,
                        true
                    )

                } else {
                    Utils.disableBluetooth()
                    AppSharedPreferences.writeSp(
                        sharedPreferences,
                        PreferencesKeys.bluetoothStatus,
                        false
                    )
                }
            }

            holder.radioTdynamo.isChecked = false
            holder.radioMura.isChecked = false
            if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.selectedDevice)
                    .equals(Constants.DeviceType.MAGTEK.name)
            ) {
                holder.radioTdynamo.isChecked = true
            } else if (AppSharedPreferences.readString(
                    sharedPreferences,
                    PreferencesKeys.selectedDevice
                ).equals(Constants.DeviceType.MIURA.name)
            ) {
                holder.radioMura.isChecked = true
            }
            holder.cardTdynamo.setOnClickListener {
                callBack.onCallback(Constants.DeviceType.MAGTEK.name)
            }
            holder.cardMiura.setOnClickListener {
                callBack.onCallback(Constants.DeviceType.MIURA.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }


}