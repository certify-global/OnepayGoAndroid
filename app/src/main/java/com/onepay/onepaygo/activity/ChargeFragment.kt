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
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.databinding.FragmentChargeBinding
import com.onepay.onepaygo.R
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChargeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setClickListener()
        //setLoginDataListener()
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
            } else if(binding.includePayment.etMmYy.text!!.isNotEmpty())
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
            binding.includePayment.root.visibility = View.VISIBLE
            binding.etCharge.isEnabled = false
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
            if(isSwipe){
                setDefaultPayment()
                isSwipe = false
                binding.includePayment.tvConnectSwipe.visibility = View.GONE
                binding.includePayment.viewPrograce.visibility = View.GONE

                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_swipe_arrow)
            }else{
                binding.includePayment.imgSwipeArrow.setImageResource(R.drawable.ic_arrow_up)
                isSwipe = true
                binding.includePayment.tvConnectSwipe.visibility = View.VISIBLE
            }
        })
        binding.includePayment.tvConnectSwipe.setOnClickListener(View.OnClickListener {
            if(AppSharedPreferences.readBoolean(sharedPreferences,PreferencesKeys.deviceStatus)){

            }else{

                Utils.openDialogDevice(requireContext(),requireActivity())
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
            binding.includePayment.tvProceedPayment.alpha = 1f
            binding.includePayment.tvProceedPayment.isEnabled = true
        }

    }
    private fun setDefaultPayment(){
        binding.includePayment.tvProceedPayment.alpha = .5f
        binding.includePayment.tvProceedPayment.isEnabled = false
    }
}