package com.onepay.miura.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.onepay.miura.common.Logger
import com.onepay.miura.common.Utils
import com.onepay.miura.databinding.ActivityWebViewBinding


class WebViewActivity : AppCompatActivity() {
    private val TAG: String = WebViewActivity::class.java.name

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var url: String
    private var dialog: Dialog? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = Utils.showDialog(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        url = getIntent().getStringExtra("loadURL").toString()
        Logger.debug(TAG, url)
        binding.webViewLink.settings.javaScriptEnabled = true
        binding.webViewLink.getSettings().setUseWideViewPort(true)
        binding.webViewLink.getSettings().setLoadWithOverviewMode(true)
        binding.webViewLink.getSettings().setDomStorageEnabled(true)
        binding.webViewLink.setWebViewClient(WebViewController())
        binding.webViewLink.loadUrl(url)
    }

    inner class WebViewController : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            dialog!!.show()
            binding.webViewLink.setVisibility(View.GONE)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)

            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            dialog!!.dismiss()
            binding.webViewLink.setVisibility(View.VISIBLE)

        }

        @Suppress("UNUSED_PARAMETER")
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            dialog?.dismiss()
            binding.webViewLink.setVisibility(View.VISIBLE)
        }

    }
}