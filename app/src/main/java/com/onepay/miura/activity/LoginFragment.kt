package com.onepay.miura.activity

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.internal.TextWatcherAdapter
import com.onepay.miura.R
import com.onepay.miura.common.Logger
import com.onepay.miura.common.Utils
import com.onepay.miura.databinding.FragmentLoginBinding
import com.onepay.miura.model.LoginViewModel


class LoginFragment : Fragment() {
    var loginViewModel: LoginViewModel? = null
    private lateinit var binding: FragmentLoginBinding

    private var pDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loginViewModel?.init(requireContext())
        setClickListener()
        setLoginDataListener()
    }

    private fun initView() {
        pDialog = Utils.showDialog(requireContext())
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
    }

    private fun loginValidations(): Boolean {
        if (binding.etUserName.text.isEmpty()) {
            binding.etUserName.setBackgroundResource(R.drawable.edit_text_border_read)
            binding.etUserName.hint = resources.getString(R.string.enter_user_name)
            binding.etUserName.setHintTextColor(resources.getColor(R.color.red_light,null))
            return false
        }
        if (binding.etPassWord.text.isEmpty()) {
            binding.etPassWord.setBackgroundResource(R.drawable.edit_text_border_read)
            binding.etPassWord.hint = resources.getString(R.string.enter_password)
            binding.etPassWord.setHintTextColor(resources.getColor(R.color.red_light,null))
            return false
        }
        return true
    }

    private fun setLoginDataListener() {
        loginViewModel?.mlLoginResponse?.observe(viewLifecycleOwner,{
            pDialog?.cancel()
          if(it.IsTrue.equals("false")){
              binding.tvError.text = it.Response
              binding.tvError.visibility = View.VISIBLE
          }else{

          }
        })
    }
    private fun resetUI(){
        binding.etPassWord.setBackgroundResource(R.drawable.edit_text_border)
        binding.tvError.visibility = View.GONE
        binding.etUserName.setBackgroundResource(R.drawable.edit_text_border)
        binding.etUserName.hint = resources.getString(R.string.et_username)
        binding.etPassWord.hint = resources.getString(R.string.et_password)
        binding.etUserName.setHintTextColor(resources.getColor(R.color.gray_dark,null))
        binding.etPassWord.setHintTextColor(resources.getColor(R.color.gray_dark,null))

    }
}