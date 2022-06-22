

package com.onepay.onepaygo.adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.response.TerminalResponse
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.data.AppSharedPreferences

/* A list always displaying one element: the number of flowers. */

class TerminalAdapter(
    var terminalList: List<TerminalResponse>,
    var sharedPreferences: SharedPreferences,
    var callBack: CallbackInterface
) : RecyclerView.Adapter<TerminalAdapter.HeaderViewHolder>() {
    var previousTerminal: RadioButton? = null

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val radioIsSelect: RadioButton = itemView.findViewById(R.id.radio_item)
        val cardLocation: CardView = itemView.findViewById(R.id.card_location)


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

        if (AppSharedPreferences.readInt(sharedPreferences, PreferencesKeys.terminalValuesId) == terminalList.get(position).Id) {
            holder.radioIsSelect.isChecked = true
            previousTerminal = holder.radioIsSelect
        } else holder.radioIsSelect.isChecked = false
        holder.cardLocation.setOnClickListener {
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.terminalValues, terminalList.get(position).TerminalType)
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.terminalName, terminalList.get(position).TerminalName)
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.terminalValuesId, terminalList.get(position).Id)
            callBack.onCallback(Constants.navUpdate)
            if (previousTerminal != null)
                previousTerminal?.isChecked = false
            previousTerminal = holder.radioIsSelect
            holder.radioIsSelect.isChecked = true
        }
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return terminalList.size
    }
}