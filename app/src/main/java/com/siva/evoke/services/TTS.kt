package com.siva.evoke.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*


class TTS : Service(), TextToSpeech.OnInitListener {
    private var mTts: TextToSpeech? = null
    private var spokenText: String = "Have nothing to specify"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Inside","TTS")
        spokenText = intent?.getStringExtra("STATUS").toString()
        mTts = TextToSpeech(this, this)
        return START_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("Inside","Success")
            Log.d("Inside",spokenText)
            val result = mTts!!.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                mTts!!.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null)
            }else{
                Log.d("Inside","Lang Failed")
            }
        }else{
            Log.d("Inside","Failed")
        }
    }

    override fun onDestroy() {
        if (mTts != null) {
            mTts!!.stop()
            mTts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }
}