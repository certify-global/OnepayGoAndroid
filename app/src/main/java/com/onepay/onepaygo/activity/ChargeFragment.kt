package com.onepay.onepaygo.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onepay.miura.data.TransactionApiData
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Constants.KeyBoard.AMOUNT
import com.onepay.onepaygo.common.Constants.KeyBoard.CARD
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.controller.MiuraController
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.FragmentChargeBinding
import com.onepay.onepaygo.databinding.LayoutCardManualBinding
import com.onepay.onepaygo.databinding.LayoutCardSwipeBinding
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
    private lateinit var cardManualBinding: LayoutCardManualBinding
    private lateinit var cardSwipeBinding: LayoutCardSwipeBinding
    private lateinit var cardManualEbtBinding: LayoutCardSwipeBinding
    private lateinit var cardSwipeEbtBinding: LayoutCardSwipeBinding
    private var isManual = false
    private var isSwipe = false
    private lateinit var sharedPreferences: SharedPreferences
    private var isEbt = false

    private lateinit var imageManualArrow: AppCompatImageView
    private lateinit var etCardNumber: AppCompatEditText
    private lateinit var etCvv: AppCompatEditText
    private lateinit var etMmYy: AppCompatEditText
    private lateinit var imageSwipeArrow: AppCompatImageView
    private lateinit var tvSwipeMessage: AppCompatTextView
    private lateinit var tvConnectSwipe: AppCompatTextView
    private lateinit var layoutAnim: LinearLayout
    private lateinit var viewPrograce: View
    private var chargeFragmentView: View? = null

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
    ): View {
        binding = FragmentChargeBinding.inflate(layoutInflater)
        cardManualBinding = binding.includePayment.cardManual
        cardManualEbtBinding = binding.includePayment.ebtCardManual
        cardSwipeBinding = binding.includePayment.cardSwipe
        cardSwipeEbtBinding = binding.includePayment.ebtCardSwipe
        apiKeyViewModel = ViewModelProvider(this).get(ApiKeyViewModel::class.java)
        transactionViewModel = ViewModelProvider(this).get(TransactionViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        chargeFragmentView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        RetrofitInstance.init()
        transactionViewModel?.init(requireContext())
        refreshTokenViewModel?.init(requireContext())
        MiuraController.instance?.init(context)
        MiuraController.instance?.setCallbackListener(this)
        setClickListener()
        setAPIDataListener()
        setDefaultPayment()
        Logger.info(TAG, " onViewCreated()", "ChargeFragment")
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        if (TransactionDataSource.getIsChargeFragment() == true) {
            TransactionDataSource.setIsChargeFragment(false)
            amountCharge = TransactionDataSource.getAmount().toString()
            binding.includeCharge.etCharge.setText(amountCharge)
            setDefaultPayment()
            updatePaymentUi()
        } else if (!TransactionDataSource.getIsRetry()!!) {
            binding.includeCharge.etCharge.setText("0.00")
            Utils.deleteTrackData(requireContext())
            paymentUIUpdate()
            manualDataReset()
        }
        TransactionDataSource.setIsRetry(false)
        try {
            (activity as HomeActivity).updateTitle("")
        } catch (e: Exception) {
            Logger.error(TAG, " onResume()", e.message!!)
        }

    }

    private fun initView() {
        cardManualEbtBinding.tvCardSwipe.setText(R.string.ebt_card_manual_entry)
        cardSwipeEbtBinding.tvCardSwipe.setText(R.string.ebt_card_swipe)
        initManual(cardManualBinding)
        initSwipe(cardSwipeBinding)
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

        binding.slidingLayout.panelState = PanelState.COLLAPSED
        binding.includeCharge.etCharge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                validationValue(s.toString())


            }
        })

        binding.slidingLayout.addPanelSlideListener(object :
            SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                // binding.includeCustomer.root.setAlpha(slideOffset)
            }

            override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
                if (newState == PanelState.ANCHORED) {
                    binding.includePayment.root.visibility = View.VISIBLE
                    binding.llAction.visibility = View.VISIBLE
                    binding.includeCustomer.root.visibility = View.GONE
                }
                if (newState == PanelState.EXPANDED) {
                    binding.includePayment.root.visibility = View.GONE
                    binding.includeCustomer.root.visibility = View.VISIBLE
                }
                if (newState == PanelState.COLLAPSED) {
                    binding.includeCustomer.root.visibility = View.GONE
                    binding.llAction.visibility = View.GONE
                    Utils.hideKeyboard(requireActivity())
                }
            }
        })
        binding.slidingLayout.setFadeOnClickListener {
            binding.slidingLayout.panelState = PanelState.COLLAPSED
        }

        binding.tvCancel.setOnClickListener {
            Logger.info(TAG, "tvCancel", "Cancel")
            manualDataReset()
            paymentUIUpdate()
            val deviceCode = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceCode)
            if (deviceCode.equals(Constants.DeviceType.MIURA.name)) {
                MiuraController.instance?.cancelTransaction()
            }
        }
    }

    private fun initManual(cardBinding: LayoutCardManualBinding) {
        imageManualArrow = cardBinding.imgManualArrow
        etCardNumber = cardBinding.etCardNumber
        etCvv = cardBinding.etCvv
        etMmYy = cardBinding.etMmYy

        etCardNumber.doAfterTextChanged {
            val formattedText = it.toString().replace(" ", "").chunked(4).joinToString(" ")
            if (formattedText != it.toString()) {
                etCardNumber.setText(formattedText)
                etCardNumber.setSelection(formattedText.length)
            }
            if (formattedText.length > 16)
                if (formattedText.startsWith("37") || formattedText.startsWith("34")) {
                    etCvv.filters = arrayOf(InputFilter.LengthFilter(4))
                } else etCvv.filters = arrayOf(InputFilter.LengthFilter(3))

            if (formattedText.length > 18) {
                etCvv.isFocusable = true
                etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue)
                etCvv.requestFocus()
                startPayment()
            }
//            else {
//                binding.includePayment.etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue_read)
//            }
        }
        etCvv.doAfterTextChanged {
            if ((etCardNumber.text!!.startsWith("37") || etCardNumber.text!!.startsWith("34"))
                && etCvv.text!!.length == 4
            ) {
                etCvv.setBackgroundResource(R.drawable.edit_text_border_blue)
                etMmYy.requestFocus()
                startPayment()
            } else if (it.toString().length == 3) {
                etCvv.setBackgroundResource(R.drawable.edit_text_border_blue)
                etMmYy.requestFocus()
                startPayment()
            }

//            else if(binding.includePayment.etCvv.text!!.isNotEmpty()){
//                binding.includePayment.etCvv.setBackgroundResource(R.drawable.edit_text_border_blue_read)
//
//            }
        }
        etMmYy.doAfterTextChanged {
            val formattedText = it.toString().replace("/", "").chunked(2).joinToString("/")
            if (formattedText != it.toString()) {
                etMmYy.setText(formattedText)
                etMmYy.setSelection(formattedText.length)
            }
            if (Utils.ValidationMMYY(formattedText)) {
                etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue)
                startPayment()
            } else if (etMmYy.text!!.isNotEmpty())
                etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue_read)
        }
    }

    private fun initSwipe(swipeCardBinding: LayoutCardSwipeBinding) {
        imageSwipeArrow = swipeCardBinding.imgSwipeArrow
        tvSwipeMessage = swipeCardBinding.tvSwipeMessage
        tvConnectSwipe = swipeCardBinding.tvConnectSwipe
        layoutAnim = swipeCardBinding.layoutAnim
        viewPrograce = swipeCardBinding.viewPrograce

        tvConnectSwipe.setOnClickListener {
            Logger.info(TAG, " tvConnectSwipe", "Swipe Card")
            Utils.enableBluetooth()
            tvSwipeMessage.text = ""
            if (AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.deviceStatus)) {
                layoutAnim.visibility = View.VISIBLE
                tvConnectSwipe.visibility = View.GONE
                animMove = AnimationUtils.loadAnimation(requireContext(), R.anim.progress_animation)
                animMove?.setAnimationListener(this)
                animMove?.repeatCount = Animation.INFINITE
                viewPrograce.startAnimation(animMove)
                if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.selectedDevice).equals(Constants.DeviceType.MIURA.name)) {
                    AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.deviceCode, Constants.DeviceType.MIURA.name)
                    MiuraController.instance?.MiuraPairing(amountCharge, isEbt)
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
    }

    fun validationValue(s: String) {
        try {
            if (s != current) {
                val replaceable = String.format("[%s,.\\s]", "$")
                val cleanString = s.replace(replaceable.toRegex(), "")
                val parsed: Double = try {
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
            Logger.error(TAG, " validationValue()", e.message!!)
        }
    }

    private fun updatePaymentUi() {
        Logger.info(TAG, " updatePaymentUi()", "Proceed")
        Utils.slideUp(binding.slidingLayout)
        binding.slidingLayout.anchorPoint = 0.8f
        binding.slidingLayout.panelState = PanelState.ANCHORED
        Utils.deleteTrackData(requireContext())
        when (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)) {
            Constants.retail -> {
                binding.includePayment.tvConnect.visibility = View.VISIBLE
                binding.includePayment.llCardSwipe.visibility = View.VISIBLE
            }
            Constants.moto -> {
                binding.includePayment.tvConnect.visibility = View.GONE
                binding.includePayment.llCardSwipe.visibility = View.GONE
            }
            Constants.ecomm -> {
                binding.includePayment.tvConnect.visibility = View.GONE
                binding.includePayment.llCardSwipe.visibility = View.GONE
            }
        }
        TransactionDataSource.setIsRetry(true)
        binding.includePayment.root.visibility = View.VISIBLE
        binding.llAction.visibility = View.VISIBLE
        binding.includePayment.tvAmountPaymentView.visibility = View.VISIBLE
        amountCharge = binding.includeCharge.etCharge.text.toString()
        binding.includePayment.tvAmountPaymentView.text = (String.format("%s $%s", resources.getString(R.string.charge), amountCharge))
        binding.includeCustomer.tvAmountFields.text = (String.format("%s $%s", resources.getString(R.string.charge), amountCharge))
    }

    private fun setClickListener() {
        binding.includeCharge.tvProceed.setOnClickListener {
            updatePaymentUi()
            context?.let { it1 -> transactionViewModel?.init(it1) }
        }
        binding.tvProceedPayment.setOnClickListener {
            Logger.info(TAG, " tvProceedPayment", "Payment")
            binding.tvProceedPayment.isEnabled = false
            getApiKey()
        }
        binding.includePayment.llCardManual.setOnClickListener {
            if (cardSwipeBinding.layoutAnim.visibility == View.VISIBLE ||
                cardSwipeEbtBinding.layoutAnim.visibility == View.VISIBLE ||
                cardManualEbtBinding.layoutAnim.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            isEbt = false
            MiuraController.instance?.isManualEbt = false
            initManual(cardManualBinding)
            handleCardManualClick()
        }
        binding.includePayment.llCardSwipe.setOnClickListener {
            if (cardSwipeBinding.layoutAnim.visibility == View.VISIBLE ||
                cardSwipeEbtBinding.layoutAnim.visibility == View.VISIBLE ||
                cardManualEbtBinding.layoutAnim.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            isEbt = false
            MiuraController.instance?.isManualEbt = false
            swipeDataReset()
            initSwipe(cardSwipeBinding)
            handleCardSwipeClick()
        }
        binding.includePayment.ebtCardManualLayout.setOnClickListener {
            if (cardSwipeBinding.layoutAnim.visibility == View.VISIBLE ||
                cardSwipeEbtBinding.layoutAnim.visibility == View.VISIBLE ||
                cardManualEbtBinding.layoutAnim.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            isEbt = true
            MiuraController.instance?.isManualEbt = true
            swipeDataReset()
            initSwipe(cardManualEbtBinding)
            handleCardSwipeClick()
        }
        binding.includePayment.ebtCardSwipeLayout.setOnClickListener {
            if (cardSwipeBinding.layoutAnim.visibility == View.VISIBLE ||
                cardSwipeEbtBinding.layoutAnim.visibility == View.VISIBLE ||
                cardManualEbtBinding.layoutAnim.visibility == View.VISIBLE) {
                return@setOnClickListener
            }
            isEbt = true
            MiuraController.instance?.isManualEbt = false
            swipeDataReset()
            initSwipe(cardSwipeEbtBinding)
            handleCardSwipeClick()
        }
    }

    private fun handleCardManualClick() {
        tvSwipeMessage.visibility = View.GONE
        Utils.deleteTrackData(requireContext())
        setDefaultPayment()
        if (isManual) {
            isManual = false
            manualDataReset()
        } else {
            imageManualArrow.setImageResource(R.drawable.ic_arrow_up)
            keyBoardType = CARD.name
            etCardNumber.visibility = View.VISIBLE
            etCvv.visibility = View.VISIBLE
            etMmYy.visibility = View.VISIBLE
            binding.includePayment.tvAmountPaymentView.visibility = View.VISIBLE
            tvConnectSwipe.visibility = View.GONE
            imageSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
            isManual = true
            isSwipe = false

        }
    }

    private fun handleCardSwipeClick() {
        tvSwipeMessage.text = ""
        if (isSwipe) {
            swipeDataReset()
        } else {
            imageSwipeArrow.setImageResource(R.drawable.ic_arrow_up)
            isSwipe = true
            isManual = false
            manualDataReset()
            tvConnectSwipe.visibility = View.VISIBLE
        }
    }

    private fun manualDataReset() {
        etCardNumber.visibility = View.GONE
        etCvv.visibility = View.GONE
        etMmYy.visibility = View.GONE
        //binding.includePayment.tvAmountPaymentView.visibility = View.GONE
        etCardNumber.setText("")
        etCvv.setText("")
        etMmYy.setText("")
        binding.includeCustomer.edittextFName.setText("")
        binding.includeCustomer.edittextLName.setText("")
        binding.includeCustomer.edittextNotes.setText("")
        binding.includeCustomer.edittextInvoiceNo.setText("")
        binding.includeCustomer.edittextCId.setText("")

        etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue)
        binding.includePayment.llCardSwipe.alpha = 1f
        binding.includePayment.llCardSwipe.isEnabled = true
        imageManualArrow.setImageResource(R.drawable.ic_arrow_down)
    }

    private fun swipeDataReset() {
        setDefaultPayment()
        isSwipe = false
        tvConnectSwipe.visibility = View.GONE
        layoutAnim.visibility = View.GONE
        imageSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
    }

    private fun startPayment() {
        setDefaultPayment()
        if (etCardNumber.text!!.length < 13) {
            etCardNumber.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            etCardNumber.requestFocus()
        } else if (((etCardNumber.text!!.startsWith("37") || etCardNumber.text!!.startsWith("34"))
                    && etCvv.text!!.length < 4) || etCvv.text!!.length < 3
        ) {
            etCvv.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            etCvv.requestFocus()
        } else if (etMmYy.text!!.length < 5 || !Utils.ValidationMMYY(etMmYy.text.toString())) {
            etMmYy.setBackgroundResource(R.drawable.edit_text_border_blue_read)
            etMmYy.requestFocus()

        } else {
            Utils.hideKeyboard(requireActivity())
            cardNumber = etCardNumber.text.toString().replace(" ", "")
            cardCVC = etCvv.text.toString()
            cardMMYY = etMmYy.text.toString().replace("/", "")
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
                AppSharedPreferences.readInt(sharedPreferences, PreferencesKeys.terminalValuesId).toString(),
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
                refreshTokenViewModel?.refreshToken(AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.refresh_token))
        }
        transactionViewModel?.transactionRep?.observe(viewLifecycleOwner) {
            if (pDialog != null) pDialog?.cancel()
            binding.tvProceedPayment.isEnabled = true
            if (it != null) {
                Logger.info(TAG, "transactionRep", "Payment result${it.result_code}")
                if (it.result_code == 1) {
                    binding.llAction.visibility = View.GONE
                    binding.slidingLayout.panelState = PanelState.COLLAPSED
                    TransactionDataSource.setIsRetry(false)
                    context?.startActivity(Intent(context, SignatureActivity::class.java))
                } else context?.startActivity(Intent(context, PaymentResultActivity::class.java))

            } else {
                if (transactionViewModel?.messageError?.value!!.isNotEmpty())
                    Toast.makeText(context, transactionViewModel?.messageError?.value, Toast.LENGTH_LONG).show()
                else {
                    Toast.makeText(context, getString(R.string.payment_timeout), Toast.LENGTH_LONG).show()
                }
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(viewLifecycleOwner) {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(requireContext(), this)
            } else {
                apiKeyViewModel?.apikey(
                    AppSharedPreferences.readInt(sharedPreferences, PreferencesKeys.terminalValuesId).toString(),
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
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.deviceId, transactionApiData!!.deviceId())
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.serviceCode, "")
            writeTransactionMethod(transactionApiData)
            AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.entryMode, transactionApiData.entryMode())
            if ((transactionApiData.tlvData() != null) &&
                    (transactionApiData.tlvData().startsWith("e4"))) {
                AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.arqc, transactionApiData.tlvData()) //arqcData
            } else {
                AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.track1, transactionApiData.encryptedCardData())
                AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.ksn, transactionApiData.KSN())
                AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.pinBlock, transactionApiData.pinData())
                AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.pinKsn, transactionApiData.pinKsn())
                cardMMYY = transactionApiData.expiryDate()
            }
        } catch (e: Exception) {
            Logger.error(TAG, " onMiuraSuccess()", e.message!!)
        }
    }

    override fun onMiuraFailure(responseMsg: String?) {
        Logger.toast(context, responseMsg!!)
    }

    override fun onError(errorMessage: String?) {
        activity?.runOnUiThread {
            updateUIDevicePayment(resources.getString(R.string.connect))
        }
    }

    private fun updateUIDevicePayment(message: String) {
        try {
            if (MiuraController.instance?.isManualEbt == false) {
                runBlocking(Dispatchers.Main) {
                    displayUIPayment(message)
                }
            } else {
                displayUIPayment(message)
            }
        } catch (e: Exception) {
            Logger.error(TAG, " updateUIDevicePayment()", e.message!!)
        }
    }

    private fun displayUIPayment(message: String) {
        layoutAnim.visibility = View.GONE
        tvConnectSwipe.visibility = View.GONE
        tvSwipeMessage.visibility = View.VISIBLE
        tvSwipeMessage.text = message
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
        layoutAnim.visibility = View.GONE
        tvSwipeMessage.visibility = View.GONE
        imageSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
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
        layoutAnim.visibility = View.GONE
        tvConnectSwipe.visibility = View.GONE
        tvSwipeMessage.visibility = View.VISIBLE
        tvSwipeMessage.text = status
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
                if (value.toDouble() > 0) {
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

    private fun writeTransactionMethod(transactionApiData: TransactionApiData?) = when {
        transactionApiData?.isDebit == true -> {
            AppSharedPreferences.writeSp(
                sharedPreferences,
                PreferencesKeys.transactionMethod,
                Constants.MethodType.DB.name
            )
        }
        MiuraController.instance?.isManualEbt == true || MiuraController.instance?.isEbt == true -> {
            AppSharedPreferences.writeSp(
                sharedPreferences,
                PreferencesKeys.transactionMethod,
                Constants.MethodType.EBT.name
            )
        }
        else -> {
            AppSharedPreferences.writeSp(
                sharedPreferences,
                PreferencesKeys.transactionMethod,
                Constants.MethodType.CC.name
            )
        }
    }
}