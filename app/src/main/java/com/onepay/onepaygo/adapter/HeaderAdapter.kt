/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onepay.onepaygo.adapter

import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.DeviceType
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences

/* A list always displaying one element: the number of flowers. */

class HeaderAdapter(
    var sharedPreferences: SharedPreferences,
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
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter == null) {
                holder.switchBluetooth.isChecked = false
// Device does not support Bluetooth
            } else if (!mBluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled :)
                holder.switchBluetooth.isChecked = false
            } else {
                // Bluetooth is enabled
                holder.switchBluetooth.isChecked = true
            }
            holder.switchLocation.isChecked =
                AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.locationStatus)
            holder.switchBluetooth.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
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
            })

            holder.radioTdynamo.isChecked = false
            holder.radioMura.isChecked = false
            if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.selectedDevice)
                    .equals(DeviceType.TDYNAMO.name)
            ) {
                holder.radioTdynamo.isChecked = true
            } else if (AppSharedPreferences.readString(
                    sharedPreferences,
                    PreferencesKeys.selectedDevice
                ).equals(DeviceType.MUIRA.name)
            ) {
                holder.radioMura.isChecked = true
            }

//            holder.radioTdynamo.setOnCheckedChangeListener { compoundButton, b ->
//                holder.radioTdynamo.isChecked = b
//                holder.radioMura.isChecked = !b
//            }
//            holder.radioMura.setOnCheckedChangeListener { compoundButton, b ->
//                holder.radioTdynamo.isChecked = !b
//                holder.radioMura.isChecked = b
//            }
            holder.cardTdynamo.setOnClickListener(View.OnClickListener {
                callBack.onCallback(DeviceType.TDYNAMO.name)
            })
            holder.cardMiura.setOnClickListener(View.OnClickListener {
                callBack.onCallback(DeviceType.MUIRA.name)
            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }


}