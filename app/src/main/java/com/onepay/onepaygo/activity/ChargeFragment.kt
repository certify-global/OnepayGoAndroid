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
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Constants.KeyBoard.*
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.controller.MiuraController
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.FragmentChargeBinding
import com.onepay.onepaygo.model.ApiKeyViewModel
import com.onepay.onepaygo.model.RefreshTokenViewModel
import com.onepay.onepaygo.model.TransactionViewModel
import com.onepay.onepaygo.tdynamo.TDynamoUtils
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.*


class ChargeFragment : Fragment(), MiuraController.MiuraCallbackListener,
    TDynamoUtils.TDynamoPaymentListener,
    Animation.AnimationListener, View.OnClickListener, CallbackInterface {
    private val TAG = ChargeFragment::class.java.name

    private lateinit var binding: FragmentChargeBinding
    private var isManual = false
    private var isSwipe = false
    private lateinit var sharedPreferences: SharedPreferences

    private var pDialog: Dialog? = null
    var current = ""
    var apiKeyViewModel: ApiKeyViewModel? = null
    var transactionViewModel: TransactionViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null
    var amountCharge: String = ""
    var cardNumber: String = ""
    var cardMMYY: String = ""
    var cardCVC: String = ""
    var animMove: Animation? = null
    var keyBoardType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChargeBinding.inflate(layoutInflater)
        apiKeyViewModel = ViewModelProvider(this).get(ApiKeyViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        RetrofitInstance.init(context)
        apiKeyViewModel?.init(requireContext())
        transactionViewModel?.init(requireContext())
        refreshTokenViewModel?.init(requireContext())
        MiuraController.instance?.init(context)
        MiuraController.instance?.setCallbackListener(this)
        setClickListener()
        setAPIDataListener()
        setDefaultPayment()
    }

    override fun onResume() {
        super.onResume()

        if (TransactionDataSource.getIsChargeFragment() == true) {
            TransactionDataSource.setIsChargeFragment(false)
            amountCharge = TransactionDataSource.getAmount().toString();
            binding.includeCharge.etCharge.setText(amountCharge)
            updatePaymentUi()
        } else if (!TransactionDataSource.getIsRetry()!!) {

            binding.includeCharge.etCharge.setText("0.00")
            Utils.deleteTrackData(requireContext())
            paymentUIUpdate()
            manualDataReset()
        }
        TransactionDataSource.setIsRetry(false)
        try{
            (activity as HomeActivity).updateTitle("")
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun initView() {
        keyBoardType = AMOUNT.name
        pDialog = Utils.showDialog(context)
        TransactionDataSource.setIsHome(true)
        binding.includeCharge.includeKeyboard.btn1.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn2.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn3.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn4.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn5.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn6.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn7.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn8.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn9.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.btn0.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.imgDone.setOnClickListener(this)
        binding.includeCharge.includeKeyboard.imgDelete.setOnClickListener(this)

        binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//        binding.includePayment.etCardNumber.showSoftInputOnFocus = false
//        binding.includePayment.etCvv.showSoftInputOnFocus = false
//        binding.includePayment.etMmYy.showSoftInputOnFocus = false
        binding.includeCharge.etCharge.addTextChangedListener(object : TextWatcher {
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
                startPayment()
            } else if (binding.includePayment.etMmYy.text!!.isNotEmpty())
                binding.includePayment.etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue_read)
        }

        binding.slidingLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
               // binding.includeCustomer.root.setAlpha(slideOffset)
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState
            ) {
                Logger.debug(TAG,newState.name);
                if (newState == PanelState.ANCHORED) {
                    binding.includePayment.root.setVisibility(View.VISIBLE)
                    binding.llAction.visibility = View.VISIBLE
                    binding.includeCustomer.root.visibility = View.GONE
                }
                if (newState == PanelState.EXPANDED) {
                    binding.includePayment.root.setVisibility(View.GONE)
                    binding.includeCustomer.root.visibility = View.VISIBLE
                }
                if (newState == PanelState.COLLAPSED) {
                    binding.includeCustomer.root.setVisibility(View.GONE)
                    binding.llAction.visibility = View.GONE
                }
            }
        })
        binding.slidingLayout.setFadeOnClickListener(View.OnClickListener {  binding.slidingLayout.setPanelState(PanelState.COLLAPSED) })
    }

    fun validationValue(s: String) {
        try {
            if (!s.equals(current)) {
                val replaceable =
                    String.format(
                        "[%s,.\\s]",
                        "$"
                    )
                val cleanString = s.replace(replaceable.toRegex(), "")
                val parsed: Double
                parsed = try {
                    cleanString.toDouble()
                } catch (e: NumberFormatException) {
                    0.00
                }
                if (parsed > 0) binding.includeCharge.tvProceed.visibility = View.VISIBLE
                else binding.includeCharge.tvProceed.visibility = View.INVISIBLE
                current = NumberFormat.getCurrencyInstance(Locale.US).format(parsed / 100)
                current = current.replace("$", "")
                binding.includeCharge.etCharge.setText(current)
                binding.includeCharge.etCharge.setSelection(current.length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePaymentUi() {
        Utils.deleteTrackData(requireContext())
        when (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)) {
            Constants.retail -> binding.includePayment.llCardSwipe.visibility = View.VISIBLE
            Constants.moto -> binding.includePayment.llCardSwipe.visibility = View.GONE
            Constants.ecomm -> binding.includePayment.llCardSwipe.visibility = View.GONE
        }
        TransactionDataSource.setIsRetry(true)
  //      binding.includeCharge.tvProceed.visibility = View.GONE
        binding.includePayment.root.visibility = View.VISIBLE
        binding.llAction.visibility = View.VISIBLE
        binding.includePayment.tvAmountPaymentView.visibility = View.VISIBLE
        amountCharge = binding.includeCharge.etCharge.text.toString()
        binding.includePayment.tvAmountPaymentView.text = (String.format("%s $%s",resources.getString(R.string.charge),amountCharge))
      binding.includeCustomer.tvAmountFields.text =(String.format("%s $%s",resources.getString(R.string.charge),amountCharge))
    }
    private fun setClickListener() {
        binding.includeCharge.tvProceed.setOnClickListener {
            Utils.slideUp(binding.slidingLayout)
            binding.slidingLayout.setAnchorPoint(0.8f)
            binding.slidingLayout.setPanelState(PanelState.ANCHORED)
            updatePaymentUi() }
        binding.tvProceedPayment.setOnClickListener {
            binding.tvProceedPayment.isEnabled = false
            getApiKey()
        }
        binding.includePayment.llCardManual.setOnClickListener {
            if (isManual) {
                isManual = false
                manualDataReset()

                //       binding.includeKeyboard.root.visibility = View.GONE

            } else {
                binding.includePayment.imgManualArrow.setImageResource(R.drawable.ic_arrow_up)
                keyBoardType = CARD.name
            //    binding.includePayment.includeKeyboardPayment.root.visibility = View.VISIBLE
                binding.includePayment.etCardNumber.visibility = View.VISIBLE
                binding.includePayment.etCvv.visibility = View.VISIBLE
                binding.includePayment.etMmYy.visibility = View.VISIBLE
                binding.includePayment.tvAmountPaymentView.visibility = View.VISIBLE
                //                binding.nestedScroll.smoothScrollTo(0, binding.nestedScroll.bottom)
//                binding.includePayment.llCardSwipe.alpha = .5f
//                binding.includePayment.llCardSwipe.isEnabled = false
                binding.includePayment.tvConnectSwipe.visibility = View.GONE
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
                isManual = true
                isSwipe = false

            }

        }
        binding.includePayment.llCardSwipe.setOnClickListener {
            if (isSwipe) {
                setDefaultPayment()
                isSwipe = false
                binding.includePayment.tvConnectSwipe.visibility = View.GONE
                binding.includePayment.layoutAnim.visibility = View.GONE
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
            } else {
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_arrow_up)
                isSwipe = true
                isManual = false
                manualDataReset()
                binding.includePayment.tvConnectSwipe.visibility = View.VISIBLE
            }
        }
        binding.includePayment.tvConnectSwipe.setOnClickListener {
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

                } else {
                    TDynamoUtils.getInstance().setPaymentListener(this)
                    TDynamoUtils.getInstance().init(requireContext(), requireActivity())
                    TDynamoUtils.getInstance().setPaymentMode(true)

                    TDynamoUtils.getInstance().SCRAHandlerCallback().connectDevice()
                }
            } else {
                TransactionDataSource.setAmount(amountCharge)
                Utils.openDialogDevice(requireContext(), requireActivity())
            }
        }
        binding.tvCancel.setOnClickListener(View.OnClickListener {
            manualDataReset()
            paymentUIUpdate()

        })
    }

    private fun manualDataReset() {
        binding.includePayment.etCardNumber.visibility = View.GONE
        binding.includePayment.etCvv.visibility = View.GONE
        binding.includePayment.etMmYy.visibility = View.GONE
        binding.includePayment.tvAmountPaymentView.visibility = View.GONE
        binding.includePayment.etCardNumber.setText("")
        binding.includePayment.etCvv.setText("")
        binding.includePayment.etMmYy.setText("")
        binding.includePayment.etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue)
        binding.includePayment.llCardSwipe.alpha = 1f
        binding.includePayment.llCardSwipe.isEnabled = true
        binding.includePayment.imgManualArrow.setImageResource(R.drawable.ic_arrow_down)
    }

    private fun startPayment() {
        setDefaultPayment()
        if (binding.includePayment.etCardNumber.text!!.length < 13) {
            binding.includePayment.etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            binding.includePayment.etCardNumber.requestFocus()
        } else if (binding.includePayment.etCvv.text!!.length < 3) {
            binding.includePayment.etCvv.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            binding.includePayment.etCvv.requestFocus()
        } else if (binding.includePayment.etMmYy.text!!.length < 5 || !Utils.ValidationMMYY(binding.includePayment.etMmYy.text.toString())) {
            binding.includePayment.etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            binding.includePayment.etMmYy.requestFocus()

        } else {
            Utils.hideKeyboard(requireActivity())
            cardNumber = binding.includePayment.etCardNumber.text.toString().replace(" ", "")
            cardCVC = binding.includePayment.etCvv.text.toString()
            cardMMYY = binding.includePayment.etMmYy.text.toString().replace("/", "")
            binding.tvProceedPayment.alpha = 1f
            binding.tvProceedPayment.isEnabled = true

        }

    }

    private fun setDefaultPayment() {
        binding.tvProceedPayment.alpha = .5f
        binding.tvProceedPayment.isEnabled = false
    }

    fun enableConfirmButton() {
        runBlocking(Dispatchers.Main) {
            binding.tvProceedPayment.alpha = 1f
            binding.tvProceedPayment.isEnabled = true
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
                    it,
                    binding.includeCustomer.edittextFName.text.toString(),
                    binding.includeCustomer.edittextLName.text.toString(),
                    binding.includeCustomer.edittextCId.text.toString(),
                    binding.includeCustomer.edittextInvoiceNo.text.toString(),
                    binding.includeCustomer.edittextNotes.text.toString()
                )

            } else {
                if (pDialog != null) pDialog?.cancel()
                Utils.openDialogVoid(requireContext(), it.toString(), "", null)
            }
        }
        apiKeyViewModel?.messageError?.observe(viewLifecycleOwner) {
            if (it != null && it.equals("401"))
                refreshTokenViewModel?.refreshToken(
                    AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.refresh_token
                    )
                )
        }
        transactionViewModel?.transactionRep?.observe(viewLifecycleOwner) {
            if (pDialog != null) pDialog?.cancel()
            if (it != null) {
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
                else Toast.makeText(context, getString(R.string.payment_timeout), Toast.LENGTH_LONG)
                    .show()
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(viewLifecycleOwner) {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(requireContext(), this)
            } else {
                apiKeyViewModel?.apikey(
                    AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.gatewayterminalId
                    ),
                    AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.gatewayId),
                    AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token)
                )
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
        binding.slidingLayout.setPanelState(PanelState.COLLAPSED)
        binding.llAction.visibility = View.GONE
        binding.includePayment.root.visibility = View.GONE
        binding.includeCharge.etCharge.isEnabled = true
        setDefaultPayment()
    }

    override fun onSuccess(foundDevicestdynamo: String?) {
        binding.tvProceedPayment.alpha = 1f
        binding.tvProceedPayment.isEnabled = true
    }

    override fun onUpdateStatus(status: String?) {
        binding.includePayment.layoutAnim.visibility = View.GONE
        binding.includePayment.tvSwipeMessage.visibility = View.VISIBLE
        binding.includePayment.tvSwipeMessage.text = status
        //updateUIDevicePayment(status!!)
    }

    override fun onFailure(responseMsg: String?) {
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_0 -> {
                KeyBoardValue("0")
            }
            R.id.btn_1 -> {
                KeyBoardValue("1")
            }
            R.id.btn_2 -> {
                KeyBoardValue("2")
            }
            R.id.btn_3 -> {
                KeyBoardValue("3")

            }
            R.id.btn_4 -> {
                KeyBoardValue("4")

            }
            R.id.btn_5 -> {
                KeyBoardValue("5")

            }
            R.id.btn_6 -> {
                KeyBoardValue("6")

            }
            R.id.btn_7 -> {
                KeyBoardValue("7")

            }
            R.id.btn_8 -> {
                KeyBoardValue("8")

            }
            R.id.btn_9 -> {
                KeyBoardValue("9")
            }
            R.id.img_done -> {
               val value = binding.includeCharge.etCharge.text.toString()
                if(value.toDouble()>0) {
                    Utils.slideUp(binding.slidingLayout)
                    binding.slidingLayout.setAnchorPoint(0.8f)
                    binding.slidingLayout.setPanelState(PanelState.ANCHORED)
                    updatePaymentUi()
                }
            }
            R.id.img_delete -> {
                if (current.length > 0) {
                    binding.includeCharge.etCharge.setText(current.substring(0, current.length - 1))
                }
            }
        }
    }

    fun KeyBoardValue(str: String) {
        binding.includeCharge.etCharge.setText(String.format("%s%s", binding.includeCharge.etCharge.text, str))
    }

    override fun onCallback(msg: String?) {
        context?.startActivity(Intent(context, LoginActivity::class.java))
        activity?.finishAffinity()
    }
}