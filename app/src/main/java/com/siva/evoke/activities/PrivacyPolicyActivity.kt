package com.siva.evoke.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.siva.evoke.databinding.ActivityWebViewBinding
import com.siva.evoke.utils.Constants


class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var  binding : ActivityWebViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnAgree.setOnClickListener{
                val sp = applicationContext.getSharedPreferences(Constants.SHARED_PREF_STORAGE, MODE_PRIVATE)
                val editor = sp.edit()
                editor.putBoolean(Constants.IS_AGREED,true)
                editor.apply()
                startActivity(Intent(this@PrivacyPolicyActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}