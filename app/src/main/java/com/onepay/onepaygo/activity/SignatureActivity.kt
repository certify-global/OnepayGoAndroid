package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.gcacace.signaturepad.views.SignaturePad
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.TransactionResponseData
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.ActivitySignatureViewBinding
import com.onepay.onepaygo.model.TransactionViewModel

class SignatureActivity : AppCompatActivity() {
    private val TAG = SignatureActivity::class.java.name
    private lateinit var binding: ActivitySignatureViewBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var pDialog: Dialog? = null
    private var bitmapScale: Bitmap? = null
    var transactionViewModel: TransactionViewModel? = null
    var dataTransaction: TransactionResponseData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignatureViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataTransaction = TransactionDataSource.getTransactionResponse()
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        RetrofitInstance.init(this)
        transactionViewModel?.init(this)
        initView()
        setAPIDataListener()
    }

    override fun onPause() {
        super.onPause()
       // binding.signaturePad.clear()
    }
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.clear()
    }
    private fun initView() {
        try {
            sharedPreferences = AppSharedPreferences.getSharedPreferences(this)!!
            disablePayButton()
            pDialog = Utils.showDialog(this)
            binding.tvPaymentAmount.text = String.format("$ %s", dataTransaction?.approved_amount)
            binding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
                override fun onStartSigning() {
                    Log.i(TAG, "onStartSigning")
                }

                override fun onSigned() {
                    enabledPayButton()
                    binding.btnClear.visibility = View.VISIBLE

                }

                override fun onClear() {
                    Log.i(TAG, "onClear")

                }
            })
            binding.btnClear.setOnClickListener {
                binding.signaturePad.clearView()
                disablePayButton()
            }
            binding.btnSignature.setOnClickListener {
                if (binding.signaturePad.isEmpty)
                    disablePayButton()
                else {
                    val bitmap = binding.signaturePad.getSignatureBitmap()
                    bitmapScale = Utils.scaleBitmapAndKeepRation(bitmap, 250, 500)
                    pDialog?.show()
                    transactionViewModel?.signatureTransaction(
                        dataTransaction?.transaction_id!!,
                        bitmapScale!!,
                        TransactionDataSource.getAPIkey().toString()
                    )
                }
            }
            binding.cbAgree.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    enabledPayButton()
                } else {
                    disablePayButton()
                }
            }
        }catch (e:Exception){
            e.toString()
        }
    }

    private fun disablePayButton() {
        binding.btnSignature.alpha = .5f
        binding.btnSignature.isEnabled = false
        if (binding.signaturePad.isEmpty)
            binding.btnClear.visibility = View.GONE
    }

    private fun enabledPayButton() {
        if (binding.cbAgree.isChecked && !binding.signaturePad.isEmpty) {
            binding.btnSignature.alpha = 1f
            binding.btnSignature.isEnabled = true
        }

    }

    private fun setAPIDataListener() {

        transactionViewModel?.transactionRep?.observe(this, Observer {
            if (pDialog != null) pDialog?.cancel()
            Logger.debug(TAG, "setAPIDataListener = " + it.result_code)
            finish()
            startActivity(Intent(applicationContext, PaymentResultActivity::class.java))
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try{
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}