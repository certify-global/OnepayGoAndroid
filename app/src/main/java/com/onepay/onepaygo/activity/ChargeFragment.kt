package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.onepay.onepaygo.R
import com.onepay.onepaygo.api.request.AdditionalDataFiles
import com.onepay.onepaygo.api.request.CardRequest
import com.onepay.onepaygo.api.request.TransactionRequest
import com.onepay.onepaygo.common.*
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.databinding.FragmentChargeBinding
import com.onepay.onepaygo.model.ApiKeyViewModel
import com.onepay.onepaygo.model.TransactionViewModel
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class ChargeFragment : Fragment() {
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
    var latitudeStr: String = ""
    var longitudeStr: String = ""

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
        apiKeyViewModel?.init(requireContext())
        transactionViewModel?.init(requireContext())
        setClickListener()
        setAPIDataListener()
        setDefaultPayment()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        pDialog = Utils.showDialog(context)
        binding.etCharge.setText("0.00")
        binding.etCharge.requestFocus()
        Utils.showKeyboard(requireActivity())
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

            when (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)) {
                Constants.retail -> binding.includePayment.llCardSwipe.visibility = View.VISIBLE
                Constants.moto -> binding.includePayment.llCardSwipe.visibility = View.GONE
                Constants.ecomm -> binding.includePayment.llCardSwipe.visibility = View.GONE
            }
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
                binding.includePayment.viewPrograce.visibility = View.GONE

                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
            } else {
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_arrow_up)
                isSwipe = true
                binding.includePayment.tvConnectSwipe.visibility = View.VISIBLE
            }
        })
        binding.includePayment.tvConnectSwipe.setOnClickListener(View.OnClickListener {
            if (AppSharedPreferences.readBoolean(sharedPreferences, PreferencesKeys.deviceStatus)) {

            } else {

                Utils.openDialogDevice(requireContext(), requireActivity())
            }
        })
        binding.includePayment.tvCancel.setOnClickListener(View.OnClickListener {
            binding.includePayment.root.visibility = View.GONE
            binding.etCharge.isEnabled = true
            manualDataReset()
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
            binding.includePayment.tvProceedPayment
        }

    }

    private fun setDefaultPayment() {
        binding.includePayment.tvProceedPayment.alpha = .5f
        binding.includePayment.tvProceedPayment.isEnabled = false
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
                getTranscationApi(it)
            } else {
                if (pDialog != null) pDialog?.cancel()
                Utils.openDialogVoid(requireContext(), it.toString(), "", null)
            }
        }
        transactionViewModel?.transactionRep?.observe(viewLifecycleOwner) {
            if (pDialog != null) pDialog?.cancel()
            Logger.debug(TAG,""+it.result_code)
            findNavController().navigate(R.id.paymentFragment)

        }
    }

    private fun getTranscationApi(apikey: String) {
        val gpsTracker = GPSTracker(requireContext())
        if (gpsTracker.getLatitude() != 0.0 && gpsTracker.getLongitude() != 0.0) {
            val latitude = DecimalFormat("##.######").format(gpsTracker.getLatitude()).toDouble()
            val longitude = DecimalFormat("##.######").format(gpsTracker.getLongitude()).toDouble()
            latitudeStr = latitude.toString()
            longitudeStr = longitude.toString()
        }
        var marketCode: String
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)
                .equals(Constants.moto)
        )
            marketCode = Constants.marketCode.M.name
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.terminalValues)
                .equals(Constants.retail)
        )
            marketCode = Constants.marketCode.R.name
        else marketCode = Constants.marketCode.E.name

        val transaction = TransactionRequest(
            amountCharge,
            "CC",
            "2",
            Utils.getCurrentDateTime()!!,
            "0",
            "",
            Utils.getLocalIpAddress()!!,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceCode),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.deviceId),
            marketCode,
            Constants.referrerUrl,
            "",
            null,
            getCardData(),
            "",
            null,
            getAdditionalDataFiles()
        )
        transactionViewModel?.transaction(apikey, transaction)
    }

    private fun getCardData(): CardRequest? {
        if (cardNumber.isEmpty()) return null
        var trackData: String
        if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track1).isNotEmpty())
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track1)
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track2)
                .isNotEmpty()
        )
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track2)
        else if (AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track3)
                .isNotEmpty()
        )
            trackData = AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.track3)
        else trackData = ""
        val card = CardRequest(
            cardNumber,
            cardCVC,
            cardMMYY,
            "",
            trackData,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.entryMode),
            "",
            "",
            "",
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.ksn)
        )

        return card
    }

    private fun getAdditionalDataFiles(): ArrayList<AdditionalDataFiles> {
        val additionalDataFiles = ArrayList<AdditionalDataFiles>()
        val additional_data = AdditionalDataFiles(
            Constants.user,
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId)
        )
        val additional_data_source = AdditionalDataFiles(Constants.source, Constants.onepayGoApp)
        val additional_data_location =
            AdditionalDataFiles(Constants.location, latitudeStr + ";" + longitudeStr)
        additionalDataFiles.add(additional_data)
        additionalDataFiles.add(additional_data_source)
        additionalDataFiles.add(additional_data_location)
        return additionalDataFiles
    }
}