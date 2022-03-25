package com.onepay.miura.activity

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import com.onepay.miura.adapter.HeaderAdapter
import com.onepay.miura.adapter.TerminalAdapter
import com.onepay.miura.common.Logger
import com.onepay.miura.common.PreferencesKeys
import com.onepay.miura.common.Utils
import com.onepay.miura.data.AppSharedPreferences
import com.onepay.miura.data.TerminalDataSource
import com.onepay.miura.databinding.FragmentSettingsBinding
import com.onepay.miura.model.TerminalViewModel


class SettingFragment : Fragment() {
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
                Logger.toast(context, terminalViewModel?.messageError?.value)
            }
            val headerAdapter = HeaderAdapter(sharedPreferences)
            val terminalAdapter =
                TerminalAdapter(TerminalDataSource.getTerminalList(), sharedPreferences)
            val concatAdapter = ConcatAdapter(headerAdapter, terminalAdapter)
            binding.recTerminalList.adapter = concatAdapter
        }
    }

}