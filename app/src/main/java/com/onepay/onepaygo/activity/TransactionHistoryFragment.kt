package com.onepay.onepaygo.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.R
import com.onepay.onepaygo.adapter.HistoryAdapter
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.callback.ItemSelectedInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.databinding.FragmentTransactionHistoryBinding
import com.onepay.onepaygo.model.RefreshTokenViewModel
import com.onepay.onepaygo.model.TransactionHistoryViewModel


class TransactionHistoryFragment : Fragment(), ItemSelectedInterface, CallbackInterface {
    private val TAG = TransactionHistoryFragment::class.java.name

    private lateinit var binding: FragmentTransactionHistoryBinding
    var transactionHistoryViewModel: TransactionHistoryViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null
    private var transactionHistoryResponseData = arrayListOf<RetrieveTransactionApiResponse>()

    private lateinit var sharedPreferences: SharedPreferences
    private var pDialog: Dialog? = null
    private var strSearchDate: String? = ""
    private var amount: String? = ""
    private var cardNumber: String? = ""
    private var customerName: String? = ""
    private var transactionId: String? = ""
    private var username: String? = ""
    private var customerId: String? = ""
    private var searchSelectedType: Int = 0
    var historyAdapter: HistoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionHistoryBinding.inflate(layoutInflater)
        transactionHistoryViewModel =
            ViewModelProvider(this).get(TransactionHistoryViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        strSearchDate = Utils.getCurrentFromDate()
        initView()
        RetrofitInstance.init(context)
        refreshTokenViewModel?.init(requireContext())
        setTerminalDataListener()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        pDialog = Utils.showDialog(context)
        Utils.hideKeyboard(requireActivity())
        Utils.checkLocation(requireContext(), sharedPreferences)
        TransactionDataSource.setIsHome(false)
        val arraySearch = resources.getStringArray(R.array.search)
        TransactionHistoryDataSource.setTransactionHistory(arrayListOf<RetrieveTransactionApiResponse>())
        historyAdapter = HistoryAdapter(transactionHistoryResponseData, this, requireContext())
        binding.recHistoryList.adapter = historyAdapter
        val adapter = ArrayAdapter(requireContext(), R.layout.text_view, arraySearch)
        binding.spSearch.adapter = adapter
        binding.spSearch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                binding.etSearch.setText("")
                selectUpdate(p2)

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
        binding.spSearch.setSelection(Constants.SearchType.SourceApplication.value)
        val arraySource = Constants.SourceApplicationSearch.values()
        val adapterSource = ArrayAdapter(requireContext(), R.layout.text_view, arraySource)
        binding.spSourceApplicationSearch.adapter = adapterSource
        binding.spSourceApplicationSearch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                binding.etSearch.setText(arraySource.get(p2).name)
                val temp = TransactionHistoryDataSource.searchFilter(searchSelectedType,arraySource.get(p2).name, sharedPreferences.getInt(PreferencesKeys.terminalValuesId,0))
                updateUI(temp as ArrayList<RetrieveTransactionApiResponse>)

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
        binding.imgSearch.setOnClickListener {
            if (binding.etSearch.text.isNullOrEmpty()) return@setOnClickListener
            else {
                Utils.hideKeyboard(requireActivity())
                val temp = TransactionHistoryDataSource.searchFilter(searchSelectedType, binding.etSearch.text.toString(), sharedPreferences.getInt(PreferencesKeys.terminalValuesId,0))
                updateUI(temp as ArrayList<RetrieveTransactionApiResponse>)

            }
        }
        binding.imgDate.setOnClickListener {
            val experienceBottom = CustomCalendarFragment(strSearchDate)
            experienceBottom.show(requireActivity().supportFragmentManager, experienceBottom.tag)
            experienceBottom.setItemClickListener {
                strSearchDate = it
                pDialog?.show()
                sendReq()
            }
        }
    }

    fun selectUpdate(searchValue: Int) {
        searchSelectedType = searchValue
        binding.llSearch.visibility = View.GONE
        binding.llSource.visibility = View.GONE
        when (searchValue) {
            Constants.SearchType.All.value -> {
                updateUI(TransactionHistoryDataSource.getTransactionHistoryList())
            }
            Constants.SearchType.TransactionID.value, Constants.SearchType.CustomerID.value, Constants.SearchType.FirstName.value, Constants.SearchType.LastName.value, Constants.SearchType.Email.value,
            Constants.SearchType.Phone.value,
            Constants.SearchType.TransactionAmount.value,
            Constants.SearchType.CardLast4Digits.value -> {
                binding.llSearch.visibility = View.VISIBLE
            }
            Constants.SearchType.SourceApplication.value -> {
                binding.llSource.visibility = View.VISIBLE
                binding.etSearch.setText(Constants.SourceApplicationSearch.onepayGoApp.name)
                val temp = TransactionHistoryDataSource.searchFilter(searchSelectedType,Constants.SourceApplicationSearch.onepayGoApp.name,  sharedPreferences.getInt(PreferencesKeys.terminalValuesId,0))
                updateUI(temp as ArrayList<RetrieveTransactionApiResponse>)            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(list: ArrayList<RetrieveTransactionApiResponse>) {
        transactionHistoryResponseData = list
        if (transactionHistoryResponseData.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recHistoryList.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recHistoryList.visibility = View.VISIBLE
        }
        historyAdapter?.transactionList = transactionHistoryResponseData
        historyAdapter?.notifyDataSetChanged()
//        historyAdapter = HistoryAdapter(transactionHistoryResponseData, this, requireContext())
//        binding.recHistoryList.adapter = historyAdapter
    }

    private fun setTerminalDataListener() {

        transactionHistoryViewModel?.transactionHistoryResponse?.observe(viewLifecycleOwner) {
            Logger.debug(TAG, "transactionHistoryResponse")
            pDialog?.cancel()
            if (it == null)
                updateUI(arrayListOf<RetrieveTransactionApiResponse>())
            else {
                val emp = TransactionHistoryDataSource.searchFilter(searchSelectedType, binding.etSearch.text.toString(), sharedPreferences.getInt(PreferencesKeys.terminalValuesId,0))
                updateUI(emp as ArrayList<RetrieveTransactionApiResponse>)
            }

        }
        transactionHistoryViewModel?.messageError?.observe(viewLifecycleOwner) {
            Logger.debug(TAG, "messageError = " + transactionHistoryViewModel?.messageError?.value)
            if (transactionHistoryViewModel?.messageError?.value.isNullOrEmpty()) return@observe
            if (transactionHistoryViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.refresh_token))
            } else {
                pDialog?.cancel()

                Logger.toast(context, transactionHistoryViewModel?.messageError?.value!!)
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(viewLifecycleOwner) {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(requireContext(), this)
            } else {
                sendReq()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.debug(TAG, "onResume ")
        if (Utils.isConnectingToInternet(requireContext())) {
            pDialog?.show()
            sendReq()
        } else Logger.toast(requireContext(), resources.getString(R.string.network_error))

    }

    fun sendReq() {
        binding.tvDate.text = Utils.getCurrentFromMMMDDYYYYDate(strSearchDate!!)
        transactionHistoryViewModel?.transactionHistory(
            sharedPreferences,
            strSearchDate!!,
            amount!!,
            cardNumber!!,
            customerName!!,
            transactionId!!,
            username!!,
            customerId!!,
            ""
        )

    }

    override fun onItemSelected(pos: Int, msg: BluetoothDevice?) {
        TransactionHistoryDataSource.setTransaction(transactionHistoryResponseData.get(pos))
        activity?.startActivity(Intent(activity, HistoryDetailsActivity::class.java))

//        val fragmentTransaction: FragmentTransaction =
//            activity?.getSupportFragmentManager()!!.beginTransaction()
//
//                   fragmentTransaction.setCustomAnimations(
//                android.R.anim.slide_in_left, android.R.anim.slide_out_right,
//                R.anim.slide_in_right, R.anim.slide_out_left
//            ) //enter, exit
//
//            fragmentTransaction.replace(R.id.nav_left_menu_container, TransactionHistoryDetailsFragment(), tag)
//                .addToBackStack(tag).commit()
        //  activity?.findNavController(R.id.nav_left_menu_container)?.navigate(R.id.history_details)
    }

    override fun onCallback(msg: String?) {
        context?.startActivity(Intent(context, LoginActivity::class.java))
        activity?.finishAffinity()
    }
}