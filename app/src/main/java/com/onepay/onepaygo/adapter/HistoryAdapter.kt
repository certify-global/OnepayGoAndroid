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
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.callback.ItemSelectedInterface
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.model.ReportRecords

class HistoryAdapter(
    var transactionList: ArrayList<ReportRecords>,
    var callBack: ItemSelectedInterface,
    var context: Context
) : RecyclerView.Adapter<HistoryAdapter.HeaderViewHolder>() {
    private val TAG: String = HistoryAdapter::class.java.name

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = itemView.findViewById(R.id.tv_card_name)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_card_amount)
        val tvDate: TextView = itemView.findViewById(R.id.tv_card_date)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_card_payment_Status)
        val imgCard:ImageView = itemView.findViewById(R.id.img_card_type)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_view, parent, false)

        return HeaderViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.tvAmount.text = String.format("$%s", transactionList.get(position).transactionAmount)
        holder.tvDate.text = Utils.getDateMMMDDYYYYHHMMA(transactionList.get(position).dateTime!!)
        holder.tvName.text = transactionList.get(position).name
        holder.imgCard.setImageResource(Utils.getBrandIcon(transactionList.get(position).cardType))
        holder.tvStatus.text = transactionList.get(position).status
        if(transactionList.get(position).status.equals("APPROVED")){
            holder.tvStatus.setTextColor(context.getColor(R.color.green))
        } else   if(transactionList.get(position).status.equals("VOID")){
            holder.tvStatus.setTextColor(context.getColor(R.color.orange))
        } else holder.tvStatus.setTextColor(context.getColor(R.color.red_light))

        holder.itemView.setOnClickListener {
            try {
                callBack.onItemSelected(position, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override fun getItemCount(): Int {
        return transactionList.size
    }

}