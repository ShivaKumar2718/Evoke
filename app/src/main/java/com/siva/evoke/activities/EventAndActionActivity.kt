package com.siva.evoke.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.siva.evoke.R
import com.siva.evoke.databinding.ActivityEventAndActionBinding
import com.siva.evoke.db.Event
import com.siva.evoke.db.EventDatabase
import com.siva.evoke.db.EventViewModel
import com.siva.evoke.db.EventViewModelFactory
import com.siva.evoke.utils.*
import java.util.*


class EventAndActionActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityEventAndActionBinding
    private var eventType: String = ""
    private lateinit var tts: TextToSpeech
    private var connects: Boolean = false
    private var level: String = "Equals 50%"
    private var action1: String = ""
    private var action2: Boolean = false
    private var IsItFromUpdate: Boolean = false
    private var selectedLevel: String = "Equals"
    private lateinit var eventViewModel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventAndActionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventDao = EventDatabase.getDatabaseInstance(applicationContext).eventDao()
        val factory = EventViewModelFactory(eventDao)
        eventViewModel = ViewModelProvider(this,factory)[EventViewModel::class.java]

        tts = TextToSpeech(this,this)

        binding.apply {
            if (intent.extras!= null){
                if (intent.getBooleanExtra("update",false)) {
                    IsItFromUpdate = true
                    binding.tvNext.text = "Done"
                }

                if (IsItFromUpdate) {
                    if (intent.getStringExtra("EVENT_TYPE") != null) {
                        if (intent.getStringExtra("EVENT_TYPE") == "charger") {
                            showChargerLay()
                            if (intent.getBooleanExtra("connects", false)) {
                                initialLay.radioConnect.isChecked = true
                                connects = true
                            } else {
                                initialLay.radioDisconnect.isChecked = true
                                connects = false
                            }
                        } else {
                            binding.initialLay.seekBar.progress =
                                intent.getIntExtra("level", 0)
                            selectLevelAccordingly(intent.getStringExtra("text"))
                        }
                    }else if (intent?.getStringExtra("ACTION_TYPE")=="battery_saver" ||
                        intent?.getStringExtra("ACTION_TYPE")=="speak_aloud"){
                        actionsLay.root.show()
                        initialLay.root.hide()
                        if (intent?.getStringExtra("ACTION_TYPE")=="battery_saver"){
                            actionsLay.toggle.isChecked = intent.getBooleanExtra("saver",false)
                        }else{
                            actionsLay.etSpeak.setText(intent.getStringExtra("speak"))
                            actionsLay.etSpeak.requestFocus()
                        }
                    }

                } else {
                    if (intent?.getStringExtra("from_where")=="charger"){
                        showChargerLay()
                        initialLay.radioConnect.isChecked = true
                        connects = true
                    }else{
                        eventType = "Battery Level"
                    }
                }
            }

            actionsLay.toggle.setOnCheckedChangeListener { buttonView, isChecked ->
                binding.actionsLay.etSpeak.text?.clear()
                binding.actionsLay.etSpeak.clearFocus()
            }

            initialLay.seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    initialLay.tvEquals.text = "Equals $progress%"
                    initialLay.tvRises.text = "Rises Above $progress%"
                    initialLay.tvFalls.text = "Falls Below $progress%"
                    if (progress==100){
                        initialLay.tvRises.isEnabled = false
                        initialLay.tvRises.alpha = 0.5f
                        selectField(binding.initialLay.tvEquals)
                        deSelectField(binding.initialLay.tvFalls)
                        deSelectField(binding.initialLay.tvRises)
                    }else{
                        initialLay.tvRises.isEnabled = true
                        initialLay.tvRises.alpha = 1f
                    }

                    if (progress==0){
                        initialLay.tvFalls.isEnabled = false
                        initialLay.tvFalls.alpha = 0.5f
                        selectField(binding.initialLay.tvEquals)
                        deSelectField(binding.initialLay.tvFalls)
                        deSelectField(binding.initialLay.tvRises)
                    }else{
                        initialLay.tvFalls.isEnabled = true
                        initialLay.tvFalls.alpha = 1f
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })

            tvNext.setOnClickListener(this@EventAndActionActivity)
            tvBack.setOnClickListener(this@EventAndActionActivity)
            actionsLay.root.setOnClickListener(this@EventAndActionActivity)
            initialLay.tvEquals.setOnClickListener(this@EventAndActionActivity)
            initialLay.tvRises.setOnClickListener(this@EventAndActionActivity)
            initialLay.tvFalls.setOnClickListener(this@EventAndActionActivity)
            initialLay.radioConnect.setOnClickListener(this@EventAndActionActivity)
            initialLay.radioDisconnect.setOnClickListener(this@EventAndActionActivity)

            actionsLay.ivPlay.setOnClickListener {
                Log.d("Play","working")
                val text = actionsLay.etSpeak.text.toString()
                speakOut(text)
            }

            actionsLay.etSpeak.onFocusChangeListener =
                OnFocusChangeListener { v, hasFocus ->
                    if (hasFocus){
                        actionsLay.saverLay.slideDown(this@EventAndActionActivity)
                        tvNext.isEnabled = false
                        tvNext.alpha = 0.5f
                    }
                    else
                    {
                        actionsLay.saverLay.slideUp(this@EventAndActionActivity)
                        actionsLay.ivPlay.hide()
                        actionsLay.etSpeak.text?.clear()
                        tvNext.isEnabled = true
                        tvNext.alpha = 1f
                    }
                }

            actionsLay.etSpeak.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.isNotEmpty() == true){
                        actionsLay.ivPlay.show()
                        tvNext.isEnabled = true
                        tvNext.alpha = 1f
                    }else{
                        tvNext.isEnabled = false
                        tvNext.alpha = 0.5f
                        actionsLay.ivPlay.hide()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

    }

    private fun selectLevelAccordingly(stringExtra: String?) {
        if (stringExtra != null) {
            deSelectField(binding.initialLay.tvRises)
            deSelectField(binding.initialLay.tvFalls)
            deSelectField(binding.initialLay.tvEquals)
            val str_array = stringExtra.split(" ")
            binding.initialLay.tvEquals.text = "Equals ${str_array[str_array.size-1]}"
            binding.initialLay.tvRises.text = "Rises Above ${str_array[str_array.size-1]}"
            binding.initialLay.tvFalls.text = "Falls Below ${str_array[str_array.size-1]}"
            level =
                if (stringExtra.contains("equals")){
                    selectField(binding.initialLay.tvEquals)
                    "Equals ${str_array[str_array.size-1]}"
                }else if(stringExtra.contains("rises")){
                    selectField(binding.initialLay.tvRises)
                    "Rises Above ${str_array[str_array.size-1]}"
                }else{
                    selectField(binding.initialLay.tvFalls)
                    "Falls Below ${str_array[str_array.size-1]}"
                }
        }
    }

    private fun showChargerLay() {
        binding.initialLay.tvEventType.text = "Charger"
        binding.initialLay.tvEventType.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.charger,theme),null,null,null)
        eventType = "Charger"
        binding.initialLay.seekBar.hide()
        binding.initialLay.layWithLevels.hide()
        binding.initialLay.layWithConnects.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.actionsLay.root.visibility == View.VISIBLE && !IsItFromUpdate){
            binding.actionsLay.root.hide()
            binding.initialLay.root.show()
            binding.tvNext.text = "Next"
        }else{
            finish()
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.tv_back){
            if (binding.actionsLay.root.visibility == View.VISIBLE && !IsItFromUpdate){
                binding.actionsLay.root.hide()
                binding.initialLay.root.show()
                binding.tvNext.text = "Next"
            }else{
                finish()
            }
        }
        if (v?.id == R.id.actions_lay){
            hideKeyboard()
            action1 = ""
            if(binding.actionsLay.etSpeak.hasFocus())
                binding.actionsLay.etSpeak.clearFocus()
        }

        if (v?.id == R.id.tv_next){
            if (IsItFromUpdate){
                val intent = Intent()
                if (!binding.actionsLay.root.isVisible){
                    if (binding.initialLay.layWithConnects.isVisible){
                        intent.putExtra("EVENT_TYPE","charger")
                        intent.putExtra("connects",connects)
                    }else{
                        intent.putExtra("EVENT_TYPE","battery_level")
                        intent.putExtra("level",getSelectedLevel())
                    }
                }else{
                   intent.putExtra("action1",binding.actionsLay.etSpeak.text.toString().trim())
                    intent.putExtra("action2",binding.actionsLay.toggle.isChecked)
                }
                setResult(Activity.RESULT_OK,intent)
                finish()
            }else{
                if (binding.initialLay.root.isVisible){
                    if (binding.initialLay.tvEventType.text == "Battery Level" || binding.initialLay.tvEventType.text == "Charger"){
                        binding.initialLay.root.hide()
                        binding.actionsLay.root.show()
                        binding.tvNext.text = "Done"
                    }
                }
                else if (binding.actionsLay.root.isVisible){
                    if (binding.actionsLay.etSpeak.text.toString().trim().isNotEmpty()){
                        action1 = binding.actionsLay.etSpeak.text.toString().trim()
                    }else{
                        action2 = binding.actionsLay.toggle.isChecked
                    }
                    insertEventInDb();
                    Log.i("Details",eventType+"  "+level+"  "+connects+"  "+action1+"  "+action2)
                }
            }
        }

        if (v?.id == R.id.tv_equals){
            selectField(binding.initialLay.tvEquals)
            deSelectField(binding.initialLay.tvFalls)
            deSelectField(binding.initialLay.tvRises)
        }

        if (v?.id == R.id.tv_rises){
            selectField(binding.initialLay.tvRises)
            deSelectField(binding.initialLay.tvFalls)
            deSelectField(binding.initialLay.tvEquals)
        }
        if (v?.id == R.id.tv_falls){
            selectField(binding.initialLay.tvFalls)
            deSelectField(binding.initialLay.tvEquals)
            deSelectField(binding.initialLay.tvRises)
        }

        if (v?.id == R.id.radio_connect){
            binding.initialLay.radioDisconnect.isChecked = false
            connects = true
        }
        if (v?.id == R.id.radio_disconnect){
            binding.initialLay.radioConnect.isChecked = false
            connects = false
        }
    }

    private fun insertEventInDb() {
        val event : Event =
            if (eventType.contains("Battery") ){
                Event(0,eventType,getSelectedLevel(),false,action1,action2,true)
            }else
                Event(0,eventType,"NA",connects,action1,action2,true)

        eventViewModel.insertEvent(event)
        startActivity(Intent(this@EventAndActionActivity,MainActivity::class.java))
        finish()
    }

    private fun getSelectedLevel(): String {
        if (selectedLevel.contains("Equals")){
            return binding.initialLay.tvEquals.text.toString()
        }else if(selectedLevel.contains("Rises")){
            return binding.initialLay.tvRises.text.toString()
        }else if(selectedLevel.contains("Falls")){
            return binding.initialLay.tvFalls.text.toString()
        }
        return "NA"
    }

    private fun selectField(tv: AppCompatTextView) {
        tv.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.tick_icon),null)
        level = tv.text.toString()
        selectedLevel = tv.text.toString()
        Log.d("Level",level)
    }

    private fun deSelectField(tv: AppCompatTextView) {
        tv.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)
    }

    override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        // Don't forget to shutdown tts!
        tts.stop();
        tts.shutdown();
        super.onDestroy()
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            val result =
                tts.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("TTS", "This Language is not supported")
            } else {
                binding.actionsLay.ivPlay.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }

    }
}