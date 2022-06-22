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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onepay.onepaygo.R
import com.onepay.onepaygo.adapter.HistoryAdapter
import com.onepay.onepaygo.api.RetrofitInstance
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
import com.onepay.onepaygo.model.ReportRecords
import com.onepay.onepaygo.model.TransactionHistoryViewModel


class TransactionHistoryFragment : Fragment(), ItemSelectedInterface, CallbackInterface {
    private val TAG = TransactionHistoryFragment::class.java.name

    private lateinit var binding: FragmentTransactionHistoryBinding
    var transactionHistoryViewModel: TransactionHistoryViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null
    private var transactionHistoryResponseData = ArrayList<ReportRecords>()

    private lateinit var sharedPreferences: SharedPreferences
    private var pDialog: Dialog? = null
    private var strSearchDate: String? = ""
    private var amount: String? = ""
    private var cardNumber: String? = ""
    private var customerName: String? = ""
    private var transactionId: String? = ""
    private var username: String? = ""
    private var customerId: String? = ""
    private lateinit var layoutManager: LinearLayoutManager
    private var searchSelectedType: Int = 0
    var historyAdapter: HistoryAdapter? = null
    private var isLoading: Boolean = false
    private var limit = 20
    private var offsetValue = 0

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
        RetrofitInstance.init()
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
        updateValues()
        historyAdapter = HistoryAdapter(transactionHistoryResponseData, this, requireContext())
        layoutManager = LinearLayoutManager(context)
        binding.recHistoryList.layoutManager = layoutManager
        binding.recHistoryList.adapter = historyAdapter
        val adapter = ArrayAdapter(requireContext(), R.layout.text_view, arraySearch)
        binding.spSearch.adapter = adapter
        binding.spSearch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                try {
                    binding.etSearch.setText("")
                    selectUpdate(p2)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                try {
                    updateValues()
                    binding.etSearch.setText(arraySource.get(p2).name)
                    readingDB()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        binding.imgSearch.setOnClickListener {
            if (binding.etSearch.text.isNullOrEmpty()) return@setOnClickListener
            else {
                updateValues()
                Utils.hideKeyboard(requireActivity())
                readingDB()
            }
        }
        binding.imgDate.setOnClickListener {
            val experienceBottom = CustomCalendarFragment(strSearchDate)
            experienceBottom.show(requireActivity().supportFragmentManager, experienceBottom.tag)
            experienceBottom.setItemClickListener {
                strSearchDate = it
                updateValues()
                readingDB()
                sendReq()
            }
        }

        binding.recHistoryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                try {
                    if (!isLoading) {
                        val visibleItemCount = recyclerView.getChildCount()
                        val totalItemCount = layoutManager.getItemCount()
                        val firstVisibleItemIndex = layoutManager.findFirstVisibleItemPosition()
                        if ((totalItemCount - visibleItemCount) <= firstVisibleItemIndex && limit <= TransactionHistoryDataSource.getTransactionHistoryList().size) {
                            binding.llLoader.root.visibility = View.VISIBLE
                            offsetValue = TransactionHistoryDataSource.getTransactionHistoryList().size//transactionHistoryResponseData.size
                            limit = offsetValue + 20
                            isLoading = true
                            readingDB()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        readingDB()

    }


    fun readingDB() {
        try {
            transactionHistoryViewModel?.readingDBData(searchSelectedType, Utils.getDateSearch(strSearchDate!!), limit, offsetValue, binding.etSearch.text.toString() + "%", sharedPreferences.getInt(PreferencesKeys.terminalValuesId, 0))

        } catch (e: Exception) {
            e.message
        }
    }

    fun selectUpdate(searchValue: Int) {
        try {
            searchSelectedType = searchValue
            binding.llSearch.visibility = View.GONE
            binding.llSource.visibility = View.GONE
            when (searchValue) {
                Constants.SearchType.All.value -> {
                    updateValues()
                    readingDB()
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
                    updateValues()
                    readingDB()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(list: ArrayList<ReportRecords>) {
        try {
            transactionHistoryResponseData = list
            if (list.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recHistoryList.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recHistoryList.visibility = View.VISIBLE
            }
            historyAdapter?.transactionList = list
            binding.recHistoryList.post {
                historyAdapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setTerminalDataListener() {

        transactionHistoryViewModel?.transactionHistoryResponse?.observe(viewLifecycleOwner) {
            pDialog?.cancel()
            updateValues()
            readingDB()
        }

        transactionHistoryViewModel?.transactionHistoryDB?.observeForever(androidx.lifecycle.Observer
        {
            binding.llLoader.root.visibility = View.GONE
            if (it != null && it.isNotEmpty()) {
                TransactionHistoryDataSource.setTransactionHistory(it)
                val temp = TransactionHistoryDataSource.getTransactionHistoryList()
                updateUI(temp)
            } else if (!isLoading) updateUI(ArrayList())
            isLoading = false

        })

        transactionHistoryViewModel?.messageError?.observe(viewLifecycleOwner)
        {
            if (transactionHistoryViewModel?.messageError?.value.isNullOrEmpty()) return@observe
            if (transactionHistoryViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.refresh_token))
            } else {
                pDialog?.cancel()

                Logger.toast(context, transactionHistoryViewModel?.messageError?.value!!)
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(viewLifecycleOwner)
        {
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
        if (Utils.isConnectingToInternet(requireContext())) {
            sendReq()
        } else Logger.toast(requireContext(), resources.getString(R.string.network_error))

    }

    fun sendReq() {
        binding.tvDate.text = Utils.getCurrentFromMMMDDYYYYDate(strSearchDate!!)
        transactionHistoryViewModel?.transactionHistory(sharedPreferences, strSearchDate!!, amount!!, cardNumber!!, customerName!!, transactionId!!, username!!, customerId!!, "")

    }

    override fun onItemSelected(pos: Int, msg: BluetoothDevice?) {
        try {
            TransactionHistoryDataSource.setTransaction(transactionHistoryResponseData.get(pos))
            activity?.startActivity(Intent(activity, HistoryDetailsActivity::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCallback(msg: String?) {
        context?.startActivity(Intent(context, LoginActivity::class.java))
        activity?.finishAffinity()
    }

    fun updateValues() {
        TransactionHistoryDataSource.setTransactionHistory(arrayListOf<ReportRecords>())
        limit = 20
        offsetValue = 0
    }
}