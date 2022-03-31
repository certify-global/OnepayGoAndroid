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

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.activity.TDynamoDeviceActivity
import com.onepay.onepaygo.callback.ItemSelectedInterface
import com.onepay.onepaygo.common.Logger

class DevicesAdapter(
    var deviceList: ArrayList<BluetoothDevice>,
    var callBack: ItemSelectedInterface,
    var statusType: String
) : RecyclerView.Adapter<DevicesAdapter.HeaderViewHolder>() {
    private val TAG: String = DevicesAdapter::class.java.name

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = itemView.findViewById(R.id.item_device_tv_name)
        val cardView: CardView = itemView.findViewById(R.id.item_device_cv)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return HeaderViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        Logger.debug(TAG, "onBindViewHolder = " + position)
        val bluetoothDevice = deviceList.get(position)

        holder.tvName.text = bluetoothDevice.name

        holder.cardView.setOnClickListener {
            try {
               callBack.onItemSelected(position, statusType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun getItemCount(): Int {
        return deviceList.size
    }


}