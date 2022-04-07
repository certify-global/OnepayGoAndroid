package com.onepay.onepaygo.activity

import android.content.Intent
import android.os.Bundle
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
        binding.tvAmountValue.setText(dataTransaction?.approved_amount)
        binding.tvTransactionValue.setText(dataTransaction?.transaction_id)
        binding.tvDateValue.setText(dataTransaction?.transaction_datetime!!)
        if (dataTransaction.result_code.equals(1)) {
            binding.tvPaymentSuccess.setText(resources.getString(R.string.payment_successful))
            binding.imgSuccess.setImageResource(R.drawable.ic_successful)
            binding.btSendReceipt.setText(resources.getString(R.string.send_receipt))
        } else {
            binding.tvPaymentSuccess.setText(resources.getString(R.string.payment_failed))
            binding.imgSuccess.setImageResource(R.drawable.ic_failed)
            binding.btSendReceipt.setText(resources.getString(R.string.retry))
        }

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