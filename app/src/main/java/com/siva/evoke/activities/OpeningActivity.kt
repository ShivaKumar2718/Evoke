package com.siva.evoke.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.siva.evoke.R
import com.siva.evoke.databinding.ActivityOpeningBinding

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
            handler.removeCallbacksAndMessages(null)
            startActivity(Intent(this@OpeningActivity, MainActivity::class.java))
        }
        handler.postDelayed(runnable,1000)
    }
}