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

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.response.TerminalResponse
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences

/* A list always displaying one element: the number of flowers. */

class TerminalAdapter(
    var terminalList: List<TerminalResponse>,
    var sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<TerminalAdapter.HeaderViewHolder>() {
    var previousTerminal: RadioButton? = null

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val radioIsSelect: RadioButton = itemView.findViewById(R.id.radio_item)


    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_terminal, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds number of flowers to the header. */
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.tvName.text = terminalList.get(position).TerminalName
        if (previousTerminal != null)
            previousTerminal?.isChecked = false
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)
                .equals(terminalList.get(position).TerminalType)
        ) {
            holder.radioIsSelect.isChecked = true
            previousTerminal = holder.radioIsSelect
        } else holder.radioIsSelect.isChecked = false
        holder.radioIsSelect.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (b) {
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.terminalValues,
                    terminalList.get(position).TerminalType
                )
                if (previousTerminal != null && previousTerminal != holder.radioIsSelect)
                    previousTerminal?.isChecked = false
                previousTerminal = holder.radioIsSelect

            }
        })
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return terminalList.size
    }

    fun updateTerminalList(dataList: List<TerminalResponse>) {
        Log.i("dddddddddddd", "" + dataList.size)
        terminalList = dataList
    }
}