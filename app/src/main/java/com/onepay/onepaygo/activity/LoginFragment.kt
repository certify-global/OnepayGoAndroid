package com.onepay.onepaygo.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onepay.onepaygo.BuildConfig
import com.onepay.onepaygo.R
import com.onepay.onepaygo.common.Logger
import com.onepay.onepaygo.common.PreferencesKeys
import com.onepay.onepaygo.common.Utils
import com.onepay.onepaygo.data.AppSharedPreferences
import com.onepay.onepaygo.databinding.FragmentLoginBinding
import com.onepay.onepaygo.model.LoginViewModel
import com.onepay.onepaygo.model.TerminalViewModel
import kotlin.Boolean
import kotlin.CharSequence
import kotlin.Int


class LoginFragment : Fragment() {
    var loginViewModel: LoginViewModel? = null
    var terminalViewModel: TerminalViewModel? = null
    var sharedPreferences : SharedPreferences? = null
    private lateinit var binding: FragmentLoginBinding

    private var pDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        terminalViewModel = ViewModelProvider(this).get(TerminalViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loginViewModel?.init(requireContext())
        terminalViewModel?.init(requireContext())
        setClickListener()
        setLoginDataListener()
    }

    private fun initView() {
        sharedPreferences = AppSharedPreferences.getSharedPreferences(context)
        pDialog = Utils.showDialog(context)
        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                resetUI()
            }
        })
        binding.etPassWord.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                resetUI()
            }
        })
    }

    private fun setClickListener() {
        binding.tvLogin.setOnClickListener(View.OnClickListener {
            if (loginValidations()) {
                if (Utils.isConnectingToInternet(requireContext())) {
                    if (pDialog != null) pDialog!!.show()
                    loginViewModel?.login(
                        binding.etUserName.text.toString(),
                        binding.etPassWord.text.toString()
                    )
                } else {
                    Logger.toast(requireContext(), resources.getString(R.string.network_error))
                }
            }
        })
        binding.forgotPassWord.setOnClickListener(View.OnClickListener {
            if (Utils.isConnectingToInternet(requireContext())) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("loadURL", BuildConfig.ENDPOINT_URL + "forgot-password")
                startActivity(intent)
            } else {
                Logger.toast(requireContext(), resources.getString(R.string.network_error))
            }
        })
        binding.tvRegister.setOnClickListener(View.OnClickListener {
            if (Utils.isConnectingToInternet(requireContext())) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("loadURL", "https://onepay.com/merchant-signup/")
                startActivity(intent)
            } else {
                Logger.toast(requireContext(), resources.getString(R.string.network_error))
            }
        })
    }

    private fun loginValidations(): Boolean {
        if (binding.etUserName.text.isEmpty()) {
            binding.etUserName.setBackgroundResource(R.drawable.edit_text_border_read)
            binding.etUserName.hint = resources.getString(R.string.enter_user_name)
            binding.etUserName.setHintTextColor(resources.getColor(R.color.red_light, null))
            return false
        }
        if (binding.etPassWord.text.isEmpty()) {
            binding.etPassWord.setBackgroundResource(R.drawable.edit_text_border_read)
            binding.etPassWord.hint = resources.getString(R.string.enter_password)
            binding.etPassWord.setHintTextColor(resources.getColor(R.color.red_light, null))
            return false
        }
        return true
    }

    private fun setLoginDataListener() {
        loginViewModel?.mlLoginResponse?.observe(viewLifecycleOwner) {

            if (it == null || it.IsTrue == null ) {
                pDialog?.cancel()
                binding.tvError.text = loginViewModel?.messageError?.value
                binding.tvError.visibility = View.VISIBLE
            } else if (it.IsTrue.equals("true")) {
                    val access_token = "Bearer " + it.access_token
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.userName,it.userName)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.emailConfirmed,it.emailConfirmed)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.gatewayId,it.GatewayId)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.userId,it.UserId)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.saveLogin,true)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.access_token,access_token)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.token_type,it.token_type)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.refresh_token,it.refresh_token)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.userType,it.UserType)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.terminalId,it.terminalId)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.authxUserId,it.authxUserId)
                    AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.email,it.email)
                    terminalViewModel?.terminal(access_token, it.GatewayId!!, it.UserId)
                } else {
                    pDialog?.cancel()
                    binding.tvError.text = it.Response
                    binding.tvError.visibility = View.VISIBLE
                }

        }
        terminalViewModel?.mlTerminalResponse?.observe(viewLifecycleOwner){
            pDialog?.cancel()
            if(it == null){
            Logger.toast(context,terminalViewModel?.messageError?.value)
            }else {
             for( item in terminalViewModel?.mlTerminalResponse?.value!!){
            if(AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.terminalValues).isEmpty()){
               AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.terminalValues,item.TerminalType)
                AppSharedPreferences.writeSp(sharedPreferences,PreferencesKeys.gatewayterminalId,item.Id.toString())

            }
                }
                if(AppSharedPreferences.readString(sharedPreferences,PreferencesKeys.terminalValues).isEmpty()){
                    Logger.toast(context,resources.getString(R.string.no_active_terminal))
                }else{
                    context?.startActivity(Intent(context, HomeActivity::class.java))
                }
            }
        }
    }

    private fun resetUI() {
        binding.etPassWord.setBackgroundResource(R.drawable.edit_text_border)
        binding.tvError.visibility = View.INVISIBLE
        binding.etUserName.setBackgroundResource(R.drawable.edit_text_border)
        binding.etUserName.hint = resources.getString(R.string.et_username)
        binding.etPassWord.hint = resources.getString(R.string.et_password)
        binding.etUserName.setHintTextColor(resources.getColor(R.color.gray_dark, null))
        binding.etPassWord.setHintTextColor(resources.getColor(R.color.gray_dark, null))

    }
}