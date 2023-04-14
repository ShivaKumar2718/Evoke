package com.siva.evoke.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.siva.evoke.databinding.ActivityWebViewBinding


class WebViewActivity : AppCompatActivity() {
    private lateinit var  binding : ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val webSettings =
                webView.settings
            webSettings.javaScriptEnabled =
                true
            webView.loadUrl("https://evoke-e1a92.web.app")

            tvBack.setOnClickListener{
                finish()
            }
        }
    }
}