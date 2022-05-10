package com.onepay.onepaygo.activity

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.FragmentPaymentBinding


class PaymentResultActivity : AppCompatActivity() {
    private val TAG = PaymentResultActivity::class.java.name
    private lateinit var binding: FragmentPaymentBinding
    var dataTransaction: TransactionResponseData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataTransaction = TransactionDataSource.getTransactionResponse()
        initView()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun initView() {
        val dataTransaction = TransactionDataSource.getTransactionResponse()

        binding.tvDateValue.setText(String.format("%s",dataTransaction?.transaction_datetime!!))
        if (dataTransaction.result_code.equals(1)) {
            binding.tvPaymentSuccess.setText(resources.getString(R.string.payment_successful))
            binding.imgSuccess.setImageResource(R.drawable.ic_successful)
            binding.btSendReceipt.setText(resources.getString(R.string.send_receipt))
            val word: Spannable = SpannableString(resources.getString(R.string.amount_paid))
            word.setSpan(ForegroundColorSpan(resources.getColor(R.color.gray_text,null)), 0, word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvAmountValue.append(word)
            binding.tvAmountValue.append(" $")
            binding.tvAmountValue.append(dataTransaction?.approved_amount)
            binding.tvTransactionValue.setText(dataTransaction?.transaction_id)
        } else {
            binding.tvAmountValue.visibility = View.GONE
            binding.tvTransactionStr.visibility = View.GONE
            binding.tvTransactionValue.setText(dataTransaction?.result_text)
            binding.tvPaymentSuccess.setText(resources.getString(R.string.payment_failed))
            binding.imgSuccess.setImageResource(R.drawable.ic_failed)
            binding.btSendReceipt.setText(resources.getString(R.string.retry))
        }
        if(!dataTransaction.customerName.isNullOrEmpty()) {
            binding.tvPaidByStr.visibility = View.VISIBLE
            val userName: Spannable = SpannableString(resources.getString(R.string.paid_by))
            userName.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.gray_text, null)),
                0,
                userName.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvPaidByStr.append(userName)
            binding.tvPaidByStr.append(" ")
            binding.tvPaidByStr.append(dataTransaction.customerName)
        }else             binding.tvPaidByStr.visibility = View.GONE

        binding.btSendReceipt.setOnClickListener({
            finish()
            if (dataTransaction.result_code.equals(1))
            startActivity(Intent(applicationContext, ReceiptActivity::class.java))
            else {
                TransactionDataSource.setIsRetry(true)
            }
        })
        binding.tvNoThanks.setOnClickListener({
            TransactionDataSource.setIsRetry(false)
            finish()
        })
    }
}