package com.siva.evoke.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.siva.evoke.R
import com.siva.evoke.databinding.ActivityOpeningBinding
import com.siva.evoke.utils.Constants

class OpeningActivity : AppCompatActivity() {
    lateinit var binding: ActivityOpeningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val boltTurnAnim = AnimationUtils.loadAnimation(this@OpeningActivity, R.anim.bolt_turn_anim)
        val slideFromRight = AnimationUtils.loadAnimation(this@OpeningActivity,
            R.anim.slide_from_right
        )
        binding.apply {
            labelLay.startAnimation(boltTurnAnim)
            tvTagline.startAnimation(slideFromRight)
        }

        val handler = Handler()
        val runnable = Runnable {
            var intent: Intent? = null
            handler.removeCallbacksAndMessages(null)
            intent =
                if (applicationContext.getSharedPreferences(Constants.SHARED_PREF_STORAGE, MODE_PRIVATE).getBoolean(Constants.IS_AGREED,false)) {
                    Intent(this@OpeningActivity, MainActivity::class.java)
                }else{
                    Intent(this@OpeningActivity, PrivacyPolicyActivity::class.java)
                }
            startActivity(intent)
            finish()
        }
        handler.postDelayed(runnable,1000)
    }
}