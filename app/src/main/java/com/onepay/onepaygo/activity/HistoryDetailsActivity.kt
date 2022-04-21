package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.api.response.TransactionDetailsResponse
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.databinding.HistoryDetailsViewBinding
import com.onepay.onepaygo.model.*
import java.util.*

class HistoryDetailsActivity : AppCompatActivity(),CallbackInterface {
    private val TAG = HistoryDetailsActivity::class.java.name

    private lateinit var binding: HistoryDetailsViewBinding
    var transactionHistoryDetailsViewModel: TransactionHistoryDetailsViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null
    var transactionViewModel: TransactionViewModel? = null
    var apiKeyViewModel: ApiKeyViewModel? = null

    private lateinit var sharedPreferences: SharedPreferences
    private var transactionDetails: TransactionDetailsResponse? = null
    private var retrieveTransactionDetails: RetrieveTransactionApiResponse? = null
    private var pDialog: Dialog? = null
    private var typeVoidRF: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HistoryDetailsViewBinding.inflate(layoutInflater)
        transactionHistoryDetailsViewModel =
            ViewModelProvider(this).get(TransactionHistoryDetailsViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        apiKeyViewModel = ViewModelProvider(this).get(ApiKeyViewModel::class.java)
        setContentView(binding.root)
        Utils.PermissionCheck(this)
        initView()
        RetrofitInstance.init(this)
        transactionHistoryDetailsViewModel?.init(this)
        refreshTokenViewModel?.init(this)
        transactionViewModel?.init(this)
        apiKeyViewModel?.init(this)
        pDialog?.show()
        transactionHistoryDetailsViewModel?.transactionHistory(
            sharedPreferences,
            retrieveTransactionDetails?.TransactionId!!
        )
        setTerminalDataListener()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(this)!!
        retrieveTransactionDetails = TransactionHistoryDataSource.getTransaction()
        pDialog = Utils.showDialog(this)
        Utils.checkLocation(this, sharedPreferences)
        binding.includeAppBar.tvTitleSettings.setText("Transaction Details")
        binding.includeAppBar.drawerIcon.setImageResource(R.drawable.ic_back_arrow)
        binding.imgCardType.setImageResource(Utils.getBrandIcon(retrieveTransactionDetails?.CardType!!))
        binding.btNewReceipt.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, HistoryDetailsActivity::class.java))
        })
        binding.btRefund.setOnClickListener(View.OnClickListener {
            getApiKey()
        })
        binding.btViewMore.setOnClickListener(View.OnClickListener { })
        binding.includeAppBar.drawerIcon.setOnClickListener(View.OnClickListener { finish() })
    }

    private fun setTerminalDataListener() {
        transactionHistoryDetailsViewModel?.transactionHistoryResponse?.observe(this) {
            Logger.debug(TAG, "transactionHistoryResponse")
            pDialog?.cancel()
            if (it == null) {
                Logger.toast(this, transactionHistoryDetailsViewModel?.messageError?.value!!)
            } else {
                transactionDetails = it
                updateUI()
            }
        }
        transactionHistoryDetailsViewModel?.messageError?.observe(this) {
            Logger.debug(TAG, "messageError")
            if (transactionHistoryDetailsViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.refresh_token))
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(this) {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(this , this)
            } else {
                transactionHistoryDetailsViewModel?.transactionHistory(
                    sharedPreferences,
                    retrieveTransactionDetails?.TransactionId!!
                )
            }
        }
        apiKeyViewModel?.apikey?.observe(this) {
            if (it != null && it.isNotEmpty()) {
                transactionViewModel?.refundVoidTransaction(
                    transactionDetails?.Transaction?.Id!!.toString(),
                    TransactionDataSource.getAPIkey().toString(),
                    typeVoidRF!!,
                    retrieveTransactionDetails?.TransactionAmount!!,
                    transactionDetails?.Transaction?.AccountNumberLast4!!
                )
            } else {
                if (pDialog != null) pDialog?.cancel()
                Utils.openDialogVoid(this, it.toString(), "", null)
            }
        }
        transactionViewModel?.transactionRep?.observe(this) {
            if (pDialog != null) pDialog?.cancel()
            if (it != null) {
                Logger.debug(TAG, "" + it.result_code)
                if (it.result_code == 1) { Toast.makeText(this, it.result_text, Toast.LENGTH_LONG).show()
                    transactionHistoryDetailsViewModel?.transactionHistory(sharedPreferences, retrieveTransactionDetails?.TransactionId!!)
                } else Toast.makeText(
                    this,
                    it.result_text,
                    Toast.LENGTH_LONG
                ).show()

            } else {
                if (transactionViewModel?.messageError?.value!!.isNotEmpty())
                    Toast.makeText(this, transactionViewModel?.messageError?.value, Toast.LENGTH_LONG).show()
                else Toast.makeText(this, getString(R.string.payment_timeout), Toast.LENGTH_LONG).show()
            }
        }
    }
        fun updateUI() {

            binding.rlPaymentStatus.visibility = View.VISIBLE
            binding.tvPaymentDevice.visibility = View.VISIBLE
            binding.tvHdReceipt.visibility = View.VISIBLE
            binding.tvHdTransactionId.visibility = View.VISIBLE
            binding.tvHdCardNo.visibility = View.VISIBLE
            binding.tvHdSettlementStatus.visibility = View.VISIBLE
            // binding.tvHdTerminalName.visibility = View.VISIBLE

            binding.tvHdCustomerEmailValue.visibility = View.GONE
            binding.tvHdCustomerNo.visibility = View.GONE
            binding.tvHdCustomerNoValue.visibility = View.GONE
            binding.btNewReceipt.visibility = View.VISIBLE
            if(transactionDetails?.Transaction?.ResultText?.lowercase(Locale.getDefault())
                    .equals("declined")) {
                binding.rlPaymentStatus.setBackgroundResource(R.drawable.border_read)
                binding.btNewReceipt.visibility = View.GONE
            } else if (transactionDetails?.Transaction?.ResultText.equals("Void")){
                binding.rlPaymentStatus.setBackgroundResource(R.drawable.border_orange)
            }else{
                binding.rlPaymentStatus.setBackgroundResource(R.drawable.border_green)
            }
            binding.tvHdStatus.setText(transactionDetails?.Transaction?.ResultText)
            binding.tvHdAmount.setText(transactionDetails?.Amount)
            binding.tvHdDate.setText(Utils.getDateMMMDDYYYY(transactionDetails?.DateTime!!))
            binding.tvHdTime.setText(Utils.getDateHHMMA(transactionDetails?.DateTime!!))
            if (transactionDetails?.Transaction?.ResultText?.lowercase(Locale.getDefault())
                    .equals("approved") && (transactionDetails?.Transaction?.BatchId ===0)
            ) {
                binding.btRefund.visibility = View.VISIBLE
                typeVoidRF = Constants.Type.Void.value.toString()
                binding.btRefund.text = resources.getString(R.string.btn_void)

            } else if (transactionDetails?.Transaction?.SettledStatus === 1) {
                typeVoidRF = Constants.Type.Refund.value.toString()
                binding.btRefund.visibility = View.VISIBLE
                binding.btRefund.text = resources.getString(R.string.refund)
            } else binding.btRefund.visibility = View.GONE

            if (transactionDetails?.Transaction?.SettledStatus === 1) {
                binding.tvHdSettlementStatusValue.setText("Settled")
            } else if(transactionDetails?.Transaction?.SettledStatus ===2)
                binding.tvHdSettlementStatusValue.setText("Void")
            else binding.tvHdSettlementStatusValue.setText("Unsettled")
//        if (transactionDetails?.Transaction?.PhoneNumber.isNullOrEmpty())
//            binding.tvHdCustomerNoValue.setText("N/A")
//        else
//            binding.tvHdCustomerNoValue.setText(transactionDetails?.Transaction?.PhoneNumber)
            if (transactionDetails?.Transaction?.InvoiceNumber.isNullOrEmpty()) {
                binding.tvHdReceipt.visibility = View.GONE
                binding.tvHdReceiptValue.visibility = View.GONE
            } else {
                binding.tvHdReceipt.visibility = View.VISIBLE
                binding.tvHdReceiptValue.visibility = View.VISIBLE
                binding.tvHdReceiptValue.setText(transactionDetails?.Transaction?.InvoiceNumber.toString())
            }
            binding.tvHdTransactionValue.setText(transactionDetails?.Transaction?.Id.toString())
            //binding.tvHdCustomerEmailValue.setText(transactionDetails?.Transaction?.Email)
            binding.tvHdCardNoValue.setText(transactionDetails?.CardNumber)

        }

        private fun getApiKey() {
            if (Utils.isConnectingToInternet(this)) {
                if (pDialog != null) pDialog!!.show()
                apiKeyViewModel?.apikey(
                    AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.gatewayterminalId
                    ),
                    AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.gatewayId),
                    AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token)
                )
            } else {
                Logger.toast(this, resources.getString(R.string.network_error))
            }
        }

        override fun onBackPressed() {
            super.onBackPressed()
        }

    override fun onCallback(msg: String?) {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}