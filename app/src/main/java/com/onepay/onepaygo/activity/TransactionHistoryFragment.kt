package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.adapter.HistoryAdapter
import com.onepay.onepaygo.callback.ItemSelectedInterface
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TransactionHistoryDataSource
import com.onepay.onepaygo.databinding.FragmentTransactionHistoryBinding
import com.onepay.onepaygo.model.RefreshTokenViewModel
import com.onepay.onepaygo.model.TransactionHistoryViewModel


class TransactionHistoryFragment : Fragment(), ItemSelectedInterface{
    private val TAG = TransactionHistoryFragment::class.java.name

    private lateinit var binding: FragmentTransactionHistoryBinding
    var transactionHistoryViewModel: TransactionHistoryViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null

    private lateinit var sharedPreferences: SharedPreferences
    private var pDialog: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionHistoryBinding.inflate(layoutInflater)
        transactionHistoryViewModel = ViewModelProvider(this).get(TransactionHistoryViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        transactionHistoryViewModel?.init(requireContext())
        refreshTokenViewModel?.init(requireContext())
        pDialog?.show()
        transactionHistoryViewModel?.transactionHistory(sharedPreferences)
        setTerminalDataListener()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        pDialog = Utils.showDialog(context)
        Utils.checkLocation(requireContext(), sharedPreferences)

    }
    private fun updateUI(){
        val historyAdapter = HistoryAdapter(TransactionHistoryDataSource.getTransactionHistoryList(), this,requireContext())
        binding.recHistoryList.adapter = historyAdapter
    }

    private fun setTerminalDataListener() {

        transactionHistoryViewModel?.transactionHistoryResponse?.observe(viewLifecycleOwner) {
            Logger.debug(TAG, "transactionHistoryResponse")
            pDialog?.cancel()
            if (it == null) {
                Logger.toast(context, transactionHistoryViewModel?.messageError?.value!!)
            }
            updateUI()
        }
        transactionHistoryViewModel?.messageError?.observe(viewLifecycleOwner){
            Logger.debug(TAG, "messageError")
            if (transactionHistoryViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.refresh_token))
            }
        }
        refreshTokenViewModel?.refreshTokenResponse?.observe(viewLifecycleOwner) {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(requireContext())
            } else {
                transactionHistoryViewModel?.transactionHistory(sharedPreferences)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onItemSelected(pos: Int, msg: String?) {
    }
}