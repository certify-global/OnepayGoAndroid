package com.onepay.onepaygo.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.ActivityReceiptBinding
import com.onepay.onepaygo.model.TransactionViewModel

class ReceiptActivity : AppCompatActivity() {
    private val TAG = ReceiptActivity::class.java.name
    private lateinit var binding: ActivityReceiptBinding
    private var pDialog: Dialog? = null
    var transactionViewModel: TransactionViewModel? = null
    var dataTransaction : TransactionResponseData? = null
    private var callBack = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // callBack = intent.getStringExtra(PreferencesKeys.callBack)!!
        dataTransaction = TransactionDataSource.getTransactionResponse()
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        transactionViewModel?.init(this)
        initView()
        setAPIDataListener()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
    private fun initView() {
        binding.btReceiptSubmit.setOnClickListener {
            if(!Utils.validateEmail(binding.etReceiptEmail.text.toString())){
                binding.etReceiptEmail.setError("Invalid Email")
            }else{
                transactionViewModel?.receiptTransaction(dataTransaction?.transaction_id!!,TransactionDataSource.getAPIkey().toString(),binding.etReceiptEmail.text.toString())
            }
        }
        binding.tvReceiptCancel.setOnClickListener({
            finish()
        })

    }
    private fun setAPIDataListener() {

        transactionViewModel?.transactionRep?.observe(this,  {
            if (pDialog != null) pDialog?.cancel()
            Logger.debug(TAG,"setAPIDataListener = "+it.result_code)
            finish()
        })
    }
}