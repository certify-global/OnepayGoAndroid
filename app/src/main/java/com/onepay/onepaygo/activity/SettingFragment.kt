package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import com.onepay.onepaygo.adapter.HeaderAdapter
import com.onepay.onepaygo.adapter.TerminalAdapter
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TerminalDataSource
import com.onepay.onepaygo.databinding.FragmentSettingsBinding
import com.onepay.onepaygo.model.TerminalViewModel


class SettingFragment : Fragment(), CallbackInterface {
    private val TAG = SettingFragment::class.java.name

    private lateinit var binding: FragmentSettingsBinding
    var terminalViewModel: TerminalViewModel? = null
    private var terminalAdapter: TerminalAdapter? = null
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
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        terminalViewModel = ViewModelProvider(this).get(TerminalViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        terminalViewModel?.init(requireContext())
        pDialog?.show()
        terminalViewModel?.terminal(
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.gatewayId),
            AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId)
        )
        setTerminalDataListener()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        pDialog = Utils.showDialog(context)
        Utils.checkLocation(requireContext(), sharedPreferences)

    }

    private fun setTerminalDataListener() {

        terminalViewModel?.mlTerminalResponse?.observe(viewLifecycleOwner) {
            pDialog?.cancel()
            if (terminalViewModel?.messageError?.value.equals("401")) {

            }
            if (it == null) {
                Logger.toast(context, terminalViewModel?.messageError?.value!!)
            }
            val headerAdapter = HeaderAdapter(sharedPreferences, this)
            val terminalAdapter =
                TerminalAdapter(TerminalDataSource.getTerminalList(), sharedPreferences)
            val concatAdapter = ConcatAdapter(headerAdapter, terminalAdapter)
            binding.recTerminalList.adapter = concatAdapter
        }
    }

    override fun onCallback(msg: String?) {
        if (msg.equals(Constants.DeviceType.MAGTEK.name)) {
            startActivity(Intent(context, TDynamoDeviceActivity::class.java))
        } else startActivity(Intent(context, MiuraDeviceActivity::class.java))

    }

}