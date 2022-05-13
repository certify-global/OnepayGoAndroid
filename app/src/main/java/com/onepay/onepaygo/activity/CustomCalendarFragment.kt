package com.onepay.onepaygo.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onepay.onepaygo.R.style
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.databinding.DateBtmSheetBinding
import java.util.*

class CustomCalendarFragment(private var selectedDate: String?) : BottomSheetDialogFragment(){

    private var callback: CallbackInterface? = null
    private lateinit var binding :DateBtmSheetBinding

    fun setItemClickListener(onItemClickListener: CallbackInterface) {
        callback = onItemClickListener
    }

    override fun getTheme(): Int {
        super.getTheme()
        return style.CustomBottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DateBtmSheetBinding.inflate(inflater)
        init()
        return binding.root
    }

    private fun init() {
        binding.calendarView.maxDate = Date().time
        if(!selectedDate.isNullOrEmpty())
        binding.calendarView.date = Utils.getSelectedDate(selectedDate!!).time
        binding.calendarView.setOnDateChangeListener { calendarView,  year, monthOf, dayOfmonth ->
            val month = monthOf + 1
            val monthStr = if (month < 10) "0$month" else month.toString()
            val dayStr = if (dayOfmonth < 10) "0$dayOfmonth" else dayOfmonth.toString()
          val  selectedDate = "$year-$monthStr-$dayStr 23:59:59:000"
            callback?.onCallback(selectedDate)
            dismiss()
        }
    }
}