package com.onepay.onepaygo.activity

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.R
import com.onepay.onepaygo.adapter.HistoryAdapter
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.api.response.RetrieveTransactionApiResponse
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.callback.ItemSelectedInterface
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionHistoryBinding.inflate(layoutInflater)
        transactionHistoryViewModel =
            ViewModelProvider(this).get(TransactionHistoryViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    }

    private fun updateUI() {
        transactionHistoryResponseData = TransactionHistoryDataSource.getTransactionHistoryList()
        val historyAdapter = HistoryAdapter(transactionHistoryResponseData, this, requireContext())
        binding.recHistoryList.adapter = historyAdapter
    }

    private fun setTerminalDataListener() {

        transactionHistoryViewModel?.transactionHistoryResponse?.observe(viewLifecycleOwner) {
            Logger.debug(TAG, "transactionHistoryResponse")
            pDialog?.cancel()
            if (it == null) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recHistoryList.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recHistoryList.visibility = View.VISIBLE
                updateUI()
            }
        }
        transactionHistoryViewModel?.messageError?.observe(viewLifecycleOwner) {
            Logger.debug(TAG, "messageError = " + transactionHistoryViewModel?.messageError?.value)
            if (transactionHistoryViewModel?.messageError?.value.isNullOrEmpty()) return@observe
            if (transactionHistoryViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(
                    AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.refresh_token
                    )
                )
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
                transactionHistoryViewModel?.transactionHistory(sharedPreferences)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.debug(TAG, "onResume ")
        if(Utils.isConnectingToInternet(requireContext())) {
            pDialog?.show()
            transactionHistoryViewModel?.transactionHistory(sharedPreferences)
        }else Logger.toast(requireContext(), resources.getString(R.string.network_error))

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