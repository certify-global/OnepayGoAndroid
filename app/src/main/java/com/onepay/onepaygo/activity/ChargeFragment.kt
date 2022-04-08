package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onepay.miura.data.TransactionApiData
import com.onepay.onepaygo.R
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.controller.MiuraController
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.FragmentChargeBinding
import com.onepay.onepaygo.model.ApiKeyViewModel
import com.onepay.onepaygo.model.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat


class ChargeFragment : Fragment(), MiuraController.MiuraCallbackListener,
    Animation.AnimationListener {
    private val TAG = ChargeFragment::class.java.name

    private lateinit var binding: FragmentChargeBinding
    private var isManual = false
    private var isSwipe = false
    private lateinit var sharedPreferences: SharedPreferences

    private var pDialog: Dialog? = null
    var current = ""

    var apiKeyViewModel: ApiKeyViewModel? = null
    var transactionViewModel: TransactionViewModel? = null
    var amountCharge: String = ""
    var cardNumber: String = ""
    var cardMMYY: String = ""
    var cardCVC: String = ""
    var animMove: Animation? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChargeBinding.inflate(layoutInflater)
        apiKeyViewModel = ViewModelProvider(this).get(ApiKeyViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        apiKeyViewModel?.init(requireContext())
        transactionViewModel?.init(requireContext())
        MiuraController.instance?.init(context)
        MiuraController.instance?.setCallbackListener(this)
        setClickListener()
        setAPIDataListener()
        setDefaultPayment()
    }

    override fun onResume() {
        super.onResume()
        if (!TransactionDataSource.getIsRetry()!!) {
            binding.includePayment.root.visibility = View.GONE
            binding.etCharge.isEnabled = true
            binding.etCharge.setText("0.00")
            binding.etCharge.requestFocus()
            Utils.showKeyboard(requireActivity())
            Utils.deleteTrackData(requireContext())
            paymentUIUpdate()
            manualDataReset()
        }
        TransactionDataSource.setIsRetry(false)
    }

    private fun initView() {
        pDialog = Utils.showDialog(context)
        binding.etCharge.setSelection(binding.etCharge.text.length)

        binding.etCharge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                Logger.debug(TAG, s.toString() + ", current=" + current)
                validationValue(s.toString())


            }
        })
        binding.includePayment.etCardNumber.doAfterTextChanged {
            val formattedText = it.toString().replace(" ", "").chunked(4).joinToString(" ")
            if (formattedText != it.toString()) {
                binding.includePayment.etCardNumber.setText(formattedText)
                binding.includePayment.etCardNumber.setSelection(formattedText.length)
            }
            if (formattedText.length > 18) {
                binding.includePayment.etCvv.isFocusable = true
                binding.includePayment.etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue)
                binding.includePayment.etCvv.requestFocus()
                startPayment()
            }
//            else {
//                binding.includePayment.etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue_read)
//            }
        }
        binding.includePayment.etCvv.doAfterTextChanged {
            if (it.toString().length == 3) {
                binding.includePayment.etCvv.setBackgroundResource(R.drawable.edit_text_border_blue)
                binding.includePayment.etMmYy.requestFocus()
                startPayment()
            }
//            else if(binding.includePayment.etCvv.text!!.isNotEmpty()){
//                binding.includePayment.etCvv.setBackgroundResource(R.drawable.edit_text_border_blue_read)
//
//            }
        }
        binding.includePayment.etMmYy.doAfterTextChanged {
            val formattedText = it.toString().replace("/", "").chunked(2).joinToString("/")
            if (formattedText != it.toString()) {
                binding.includePayment.etMmYy.setText(formattedText)
                binding.includePayment.etMmYy.setSelection(formattedText.length)
            }
            if (Utils.ValidationMMYY(formattedText)) {
                binding.includePayment.etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue)
                Utils.hideKeyboard(requireActivity())
                startPayment()
            } else if (binding.includePayment.etMmYy.text!!.isNotEmpty())
                binding.includePayment.etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue_read)
        }
    }

    fun validationValue(s: String) {
        try {
            if (!s.equals(current)) {
                val replaceable =
                    String.format(
                        "[%s,.\\s]",
                        NumberFormat.getCurrencyInstance().currency?.symbol
                    )
                val cleanString = s.replace(replaceable.toRegex(), "")
                val parsed: Double
                parsed = try {
                    cleanString.toDouble()
                } catch (e: NumberFormatException) {
                    0.00
                }
                if (parsed > 0) binding.tvProceed.visibility = View.VISIBLE
                else binding.tvProceed.visibility = View.INVISIBLE
                current = (parsed / 100).toString()
                binding.etCharge.setText(current)
                binding.etCharge.setSelection(current.length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setClickListener() {
        binding.tvProceed.setOnClickListener(View.OnClickListener {
            Utils.hideKeyboard(requireActivity())
            Utils.deleteTrackData(requireContext())
            when (AppSharedPreferences.readString(
                sharedPreferences,
                PreferencesKeys.terminalValues
            )) {
                Constants.retail -> binding.includePayment.llCardSwipe.visibility = View.VISIBLE
                Constants.moto -> binding.includePayment.llCardSwipe.visibility = View.GONE
                Constants.ecomm -> binding.includePayment.llCardSwipe.visibility = View.GONE
            }
            TransactionDataSource.setIsRetry(true)
            binding.includePayment.root.visibility = View.VISIBLE
            binding.etCharge.isEnabled = false
            amountCharge = binding.etCharge.text.toString()
        })
        binding.includePayment.tvProceedPayment.setOnClickListener(View.OnClickListener {
            getApiKey()
        })
        binding.includePayment.llCardManual.setOnClickListener(View.OnClickListener {
            if (isManual) {
                isManual = false
                manualDataReset()
            } else {
                binding.includePayment.imgManualArrow.setImageResource(R.drawable.ic_arrow_up)
                binding.includePayment.etCardNumber.visibility = View.VISIBLE
                binding.includePayment.etCvv.visibility = View.VISIBLE
                binding.includePayment.etMmYy.visibility = View.VISIBLE
                binding.includePayment.llCardSwipe.alpha = .5f
                binding.includePayment.llCardSwipe.isEnabled = false
                isManual = true

            }

        })
        binding.includePayment.llCardSwipe.setOnClickListener(View.OnClickListener {
            if (isSwipe) {
                setDefaultPayment()
                isSwipe = false
                binding.includePayment.tvConnectSwipe.visibility = View.GONE
                binding.includePayment.layoutAnim.visibility = View.GONE
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
            } else {
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_arrow_up)
                isSwipe = true
                binding.includePayment.tvConnectSwipe.visibility = View.VISIBLE
            }
        })
        binding.includePayment.tvConnectSwipe.setOnClickListener(View.OnClickListener {
            if (AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.deviceStatus)) {
                binding.includePayment.layoutAnim.visibility = View.VISIBLE
                binding.includePayment.tvConnectSwipe.visibility = View.GONE
                animMove = AnimationUtils.loadAnimation(requireContext(), R.anim.progress_animation)
                animMove?.setAnimationListener(this)
                animMove?.setRepeatCount(Animation.INFINITE)
                binding.includePayment.viewPrograce.startAnimation(animMove)
                if (AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.selectedDevice
                    ).equals(Constants.DeviceType.MIURA.name)
                ) {
                    AppSharedPreferences.writeSp(
                        sharedPreferences,
                        PreferencesKeys.deviceCode,
                        Constants.DeviceType.MIURA.name
                    )
                    MiuraController.instance?.MiuraPairing(amountCharge)

                }
            } else {

                Utils.openDialogDevice(requireContext(), requireActivity())
            }
        })
        binding.includePayment.tvCancel.setOnClickListener(View.OnClickListener {
            binding.includePayment.root.visibility = View.GONE
            binding.etCharge.isEnabled = true
            manualDataReset()
            paymentUIUpdate()

        })
    }

    private fun manualDataReset() {
        binding.includePayment.etCardNumber.visibility = View.GONE
        binding.includePayment.etCvv.visibility = View.GONE
        binding.includePayment.etMmYy.visibility = View.GONE
        binding.includePayment.etCardNumber.setText("")
        binding.includePayment.etCvv.setText("")
        binding.includePayment.etMmYy.setText("")
        binding.includePayment.llCardSwipe.alpha = 1f
        binding.includePayment.llCardSwipe.isEnabled = true
        binding.includePayment.imgManualArrow.setImageResource(R.drawable.ic_arrow_down)
    }

    private fun startPayment() {
        setDefaultPayment()
        if (binding.includePayment.etCardNumber.text!!.length < 19) {
            binding.includePayment.etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            binding.includePayment.etCardNumber.requestFocus()
        } else if (binding.includePayment.etCvv.text!!.length < 3) {
            binding.includePayment.etCvv.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            binding.includePayment.etCvv.requestFocus()
        } else if (binding.includePayment.etMmYy.text!!.length < 5 || !Utils.ValidationMMYY(binding.includePayment.etMmYy.text.toString())) {
            binding.includePayment.etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            binding.includePayment.etMmYy.requestFocus()
            binding.includePayment.etMmYy.requestFocus()

        } else {
            cardNumber = binding.includePayment.etCardNumber.text.toString().replace(" ", "")
            cardCVC = binding.includePayment.etCvv.text.toString()
            cardMMYY = binding.includePayment.etMmYy.text.toString().replace("/", "")
            binding.includePayment.tvProceedPayment.alpha = 1f
            binding.includePayment.tvProceedPayment.isEnabled = true
        }

    }

    private fun setDefaultPayment() {
        binding.includePayment.tvProceedPayment.alpha = .5f
        binding.includePayment.tvProceedPayment.isEnabled = false
    }

    fun enableConfirmButton() {
        runBlocking(Dispatchers.Main) {
            binding.includePayment.tvProceedPayment.alpha = 1f
            binding.includePayment.tvProceedPayment.isEnabled = true
        }
    }

    private fun getApiKey() {
        if (Utils.isConnectingToInternet(requireContext())) {
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
            Logger.toast(requireContext(), resources.getString(R.string.network_error))
        }
    }

    private fun setAPIDataListener() {
        apiKeyViewModel?.apikey?.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                transactionViewModel?.transactionPayment(
                    amountCharge,
                    cardNumber,
                    cardCVC,
                    cardMMYY,
                    it
                )

            } else {
                if (pDialog != null) pDialog?.cancel()
                Utils.openDialogVoid(requireContext(), it.toString(), "", null)
            }
        }
        transactionViewModel?.transactionRep?.observe(viewLifecycleOwner) {
            if (pDialog != null) pDialog?.cancel()
            if (it != null) {
                Logger.debug(TAG, "" + it.result_code)
                if (it.result_code == 1) {
                    TransactionDataSource.setIsRetry(false)
                    context?.startActivity(Intent(context, SignatureActivity::class.java))
                } else context?.startActivity(Intent(context, PaymentResultActivity::class.java))

            } else {
                if (transactionViewModel?.messageError?.value!!.isNotEmpty())
                    Toast.makeText(
                        context,
                        transactionViewModel?.messageError?.value,
                        Toast.LENGTH_LONG
                    ).show()
                else Toast.makeText(
                    context,
                    getString(R.string.payment_timeout),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCardStatusChanged() {
        updateUIDevicePayment(getResources().getString(R.string.please_wait))
    }

    override fun onMiuraSuccess(transactionApiData: TransactionApiData?) {
        try {
            updateUIDevicePayment("************" + transactionApiData?.accountLastFour())
            enableConfirmButton()
            AppSharedPreferences.writeSp(
                sharedPreferences,
                PreferencesKeys.deviceId,
                transactionApiData!!.deviceId()
            )
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.serviceCode, "")
            if (transactionApiData.tlvData().startsWith("e4")) {
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.arqc,
                    transactionApiData.tlvData()
                ) //arqcData
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.pos,
                    transactionApiData.entryMode()
                )
            } else {
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.track1,
                    transactionApiData.encryptedCardData()
                )
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.ksn,
                    transactionApiData.KSN()
                )
                AppSharedPreferences.writeSp(
                    sharedPreferences,
                    PreferencesKeys.entryMode,
                    transactionApiData.entryMode()
                )
                cardMMYY = transactionApiData.expiryDate()
            }
        } catch (e: Exception) {
            Logger.error(TAG, e.message)
        }
    }

    override fun onMiuraFailure(responseMsg: String?) {
        Logger.toast(context, responseMsg!!)
    }

    override fun onError(errorMessage: String?) {
        updateUIDevicePayment(getResources().getString(R.string.connect))
    }

    fun updateUIDevicePayment(message: String) {
        Logger.debug(TAG, "updateUIDevicePayment=" + message)
        try {
            runBlocking(Dispatchers.Main) {
                binding.includePayment.layoutAnim.visibility = View.GONE
                binding.includePayment.tvSwipeMessage.visibility = View.VISIBLE
                binding.includePayment.tvSwipeMessage.text = message
            }

        } catch (e: Exception) {
            Logger.error(TAG, e.message)
        }
    }

    override fun onAnimationStart(p0: Animation?) {
    }

    override fun onAnimationEnd(p0: Animation?) {
    }

    override fun onAnimationRepeat(p0: Animation?) {
    }

    fun paymentUIUpdate() {
        isSwipe = false
        isManual = false
        binding.includePayment.layoutAnim.visibility = View.GONE
        binding.includePayment.tvSwipeMessage.visibility = View.GONE
        binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)

    }
}