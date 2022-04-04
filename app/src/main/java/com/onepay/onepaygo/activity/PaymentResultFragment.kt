package com.onepay.onepaygo.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.onepay.onepaygo.R
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.FragmentPaymentBinding


class PaymentResultFragment : Fragment() {
    private val TAG = PaymentResultFragment::class.java.name

    private lateinit var binding: FragmentPaymentBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        val dataTransaction = TransactionDataSource.getTransactionResponse()
        binding.tvAmountValue.setText(dataTransaction?.approved_amount)
        binding.tvTransactionValue.setText(dataTransaction?.transaction_id)
          binding.tvDateValue.setText(dataTransaction?.transaction_datetime!!)
        if (dataTransaction?.result_code!!.equals(1)) {
            binding.tvPaymentSuccess.setText(resources.getString(R.string.payment_successful))
            binding.imgSuccess.setImageResource(R.drawable.ic_successful)
            binding.tvSendReceipt.setText(resources.getString(R.string.send_receipt))
        } else {
            binding.tvPaymentSuccess.setText(resources.getString(R.string.payment_failed))
            binding.imgSuccess.setImageResource(R.drawable.ic_failed)
            binding.tvSendReceipt.setText(resources.getString(R.string.retry))
        }

    }

}