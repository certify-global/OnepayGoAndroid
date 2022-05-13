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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.onepay.onepaygo.R
import com.onepay.onepaygo.adapter.HeaderAdapter
import com.onepay.onepaygo.adapter.TerminalAdapter
import com.onepay.onepaygo.api.RetrofitInstance
import com.onepay.onepaygo.callback.CallbackInterface
import com.onepay.onepaygo.common.Constants
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.data.TerminalDataSource
import com.onepay.onepaygo.data.TransactionDataSource
import com.onepay.onepaygo.databinding.FragmentSettingsBinding
import com.onepay.onepaygo.model.RefreshTokenViewModel
import com.onepay.onepaygo.model.TerminalViewModel


class SettingFragment : Fragment(), CallbackInterface {
    private val TAG = SettingFragment::class.java.name

    private lateinit var binding: FragmentSettingsBinding
    var terminalViewModel: TerminalViewModel? = null
    var refreshTokenViewModel: RefreshTokenViewModel? = null

    private lateinit var sharedPreferences: SharedPreferences
    private var pDialog: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        terminalViewModel = ViewModelProvider(this).get(TerminalViewModel::class.java)
        refreshTokenViewModel = ViewModelProvider(this).get(RefreshTokenViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        RetrofitInstance.init(context)
        refreshTokenViewModel?.init(requireContext())
        if(Utils.isConnectingToInternet(requireContext())) {
            pDialog?.show()
            terminalViewModel?.terminal(
                AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.access_token),
                AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.gatewayId),
                AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId)
            )
        }else Logger.toast(requireContext(), resources.getString(R.string.network_error))
        setTerminalDataListener()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)!!
        pDialog = Utils.showDialog(context)
        Utils.checkLocation(requireContext(), sharedPreferences)
        (activity as HomeActivity).updateTitle(resources.getString(R.string.tv_settings))
        TransactionDataSource.setIsHome(false)
        Utils.enableBluetooth()
    }

    private fun updateUI() {
        val headerAdapter = HeaderAdapter(sharedPreferences, TerminalDataSource.getTerminalList().size, this)
        val terminalAdapter =
            TerminalAdapter(TerminalDataSource.getTerminalList(), sharedPreferences,this)
        val concatAdapter = ConcatAdapter(headerAdapter, terminalAdapter)
        binding.recTerminalList.adapter = concatAdapter
    }

    private fun setTerminalDataListener() {

        terminalViewModel?.mlTerminalResponse?.observe(viewLifecycleOwner) {
            if (terminalViewModel?.messageError?.value.equals("401")) {
                refreshTokenViewModel?.refreshToken(
                    AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.refresh_token
                    )
                )
                return@observe
            }
            pDialog?.cancel()
            if (it == null) {
                Logger.toast(context, terminalViewModel?.messageError?.value!!)
            }
            if (TerminalDataSource.getTerminalList().size == 0) {
                AppSharedPreferences.writeSp(sharedPreferences, PreferencesKeys.terminalValues, "")
                AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.terminalName,"")

                Utils.openDialogVoid(requireContext(),resources.getString(R.string.no_active_terminal),"",this)
            }
            updateUI()
        }

        refreshTokenViewModel?.refreshTokenResponse?.observe(viewLifecycleOwner) {
            if (it == null) {
                pDialog?.cancel()
                Utils.logOut(requireContext(), this)
            } else {
                terminalViewModel?.terminal(
                    AppSharedPreferences.readString(
                        sharedPreferences,
                        PreferencesKeys.access_token
                    ),
                    AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.gatewayId),
                    AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.userId)
                )
            }
        }
    }

    override fun onCallback(msg: String?) {
        if(msg.equals(Constants.navUpdate)){
                (activity as HomeActivity).updateLeftMenu()
            }else if (msg.equals(Constants.logout)) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finishAffinity()
        } else if (msg.equals(Constants.DeviceType.MAGTEK.name)) {
            startActivity(Intent(context, TDynamoDeviceActivity::class.java))
        } else if (msg.equals(Constants.DeviceType.MIURA.name)) {
            startActivity(Intent(context, MiuraDeviceActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        Logger.debug(TAG, "msg!!"+AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.selectedDevice))
        if (TransactionDataSource.getIsChargeFragment() == true && AppSharedPreferences.readString(sharedPreferences, PreferencesKeys.selectedDevice).isNotEmpty()) {
            activity?.findNavController(R.id.nav_left_menu_container)?.navigate(R.id.chargeFragment)
        }
    }



}