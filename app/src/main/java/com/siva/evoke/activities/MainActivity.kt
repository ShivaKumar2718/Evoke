package com.siva.evoke.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.siva.evoke.R
import com.siva.evoke.adapter.EventsAdapter
import com.siva.evoke.databinding.ActivityMainBinding
import com.siva.evoke.db.Event
import com.siva.evoke.db.EventDatabase
import com.siva.evoke.db.EventViewModel
import com.siva.evoke.db.EventViewModelFactory
import com.siva.evoke.fragments.EventTypeBottomSheet
import com.siva.evoke.services.ChargerPluginService
import com.siva.evoke.utils.hide
import com.siva.evoke.utils.show
import com.siva.evoke.utils.slideDown
import com.siva.evoke.utils.slideUp


class MainActivity : AppCompatActivity() , EventsAdapter.OnEventsClick, View.OnClickListener {
    private lateinit var binding:ActivityMainBinding
    private lateinit var eventViewModel: EventViewModel
    private lateinit var speak_text : String
    private var connects: Boolean = false
    private lateinit var event: Event

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventDao = EventDatabase.getDatabaseInstance(applicationContext).eventDao()
        val factory = EventViewModelFactory(eventDao)
        eventViewModel = ViewModelProvider(this,factory)[EventViewModel::class.java]

        val serviceIntent = Intent(this@MainActivity, ChargerPluginService::class.java)
        startForegroundService(serviceIntent)

        binding.apply {
            fabBtn.shrink()
            fabBtn.setOnClickListener(){
                val bottomSheet = EventTypeBottomSheet(this@MainActivity)
                bottomSheet.setStyle(STYLE_NORMAL,R.style.BottomSheetDialog)
                bottomSheet.show(supportFragmentManager,"bottom_sheet")
            }

            editLay.tvCancel.setOnClickListener(this@MainActivity)
            editLay.tvWhen.setOnClickListener(this@MainActivity)
            editLay.tvDo.setOnClickListener(this@MainActivity)
            editLay.tvDone.setOnClickListener(this@MainActivity)

            recyclerview.addOnScrollListener(object : OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy<0) fabBtn.extend() else fabBtn.shrink()
                }
            })

            eventViewModel.events.observe(this@MainActivity
            ) { t ->
                if (!t.isNullOrEmpty()) {
                    recyclerview.show()
                    tvNo.hide()
                    recyclerview.setHasFixedSize(true)
                    recyclerview.layoutManager = LinearLayoutManager(this@MainActivity)
                    val eventsAdapter = EventsAdapter(t, this@MainActivity, this@MainActivity)
                    recyclerview.adapter = eventsAdapter
                    eventsAdapter.notifyDataSetChanged()
                }else{
                    recyclerview.hide()
                    tvNo.show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun selectLevelAccordingly(stringExtra: String?): String {
        if (stringExtra != null && stringExtra.contains("battery")) {
            val str_array = stringExtra.split(" ")
            val level =
                if (stringExtra.contains("equals")){
                    "Equals ${str_array[str_array.size-1]}"
                }else if(stringExtra.contains("rises")){
                    "Rises Above ${str_array[str_array.size-1]}"
                }else{
                    "Falls Below ${str_array[str_array.size-1]}"
                }
            return level
        }
        return "NA"
    }

    override fun onItemClick(adapterPosition: Int, events: Event) {
        binding.dimView.show()
        binding.editLay.root.slideUp(this)
        binding.editLay.toggle.isChecked = events.isActive
        event = events
        if (events.event_type.contains("Battery")){
            binding.editLay.tvWhen.text = "When battery level ${events.level.lowercase()}"
            binding.editLay.tvWhen.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.battery_level,null),null,ResourcesCompat.getDrawable(resources,R.drawable.arrow_forward_icon,theme),null)
        }else{
            var connects = ""
            if(events.connects)  {
                connects = "connected"
                binding.editLay.tvWhen.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.charger,null),null,ResourcesCompat.getDrawable(resources,R.drawable.arrow_forward_icon,theme),null)
            } else {
                binding.editLay.tvWhen.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.charger_disconnect,null),null,ResourcesCompat.getDrawable(resources,R.drawable.arrow_forward_icon,theme),null)
                connects = "disconnected"
            }
            binding.editLay.tvWhen.text = "When charger is $connects"
        }

        val battery_saver_flag = if(events.action2)  "On" else "Off"
        if (events.action1.isEmpty()){
            binding.editLay.tvDo.text = "Battery Saver $battery_saver_flag"
            binding.editLay.tvDo.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.battery_saver,null),null,ResourcesCompat.getDrawable(resources,R.drawable.arrow_forward_icon,theme),null)
        }else{
            binding.editLay.tvDo.text = "Speak Aloud"
            binding.editLay.tvDo.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.speak_text,null),null,ResourcesCompat.getDrawable(resources,R.drawable.arrow_forward_icon,theme),null)
            speak_text = events.action1
        }

    }

    private fun getLevelOfBattery(string: String): Int{
        val str_array = string.split(" ")
        return str_array[str_array.size-1].removeSuffix("%").toInt()
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                if (intent.getStringExtra("action1") == null){
                    if (intent.getStringExtra("EVENT_TYPE") == "charger"){
                        connects = intent.getBooleanExtra("connects",false)
                        val connects = if(connects)  {
                            "connected"
                        } else {
                            "disconnected"
                        }
                        binding.editLay.tvWhen.text = "When charger is $connects"
                    }else{
                        binding.editLay.tvWhen.text = "When battery level ${intent.getStringExtra("level")?.lowercase()}"
                    }
                }else{
                    if (intent.getStringExtra("action1") == ""){
                        val saver_flag = if(intent.getBooleanExtra("action2",false)) "On" else "Off"
                        binding.editLay.tvDo.text = "Battery Saver $saver_flag"
                    }else{
                        binding.editLay.tvDo.text = "Speak Aloud"
                        speak_text = intent.getStringExtra("action1")!!
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.tv_cancel){
            binding.dimView.hide()
            binding.editLay.root.slideDown(this)
        }
        if (v?.id == R.id.tv_when){
            val intent = Intent(this@MainActivity,EventAndActionActivity::class.java)
            intent.putExtra("update",true)
            if (binding.editLay.tvWhen.text.toString().contains("battery")){
                intent.putExtra("EVENT_TYPE","battery_level")
                intent.putExtra("level",getLevelOfBattery(binding.editLay.tvWhen.text.toString()))
                intent.putExtra("text",binding.editLay.tvWhen.text.toString())
            }else{
                intent.putExtra("EVENT_TYPE","charger")
                if (binding.editLay.tvWhen.text.toString() == "When charger is connected"){
                    intent.putExtra("connects",true)
                } else{
                    intent.putExtra("connects",false)
                }

            }
            startForResult.launch(intent)
        }

        if (v?.id == R.id.tv_do){
            val intent = Intent(this@MainActivity,EventAndActionActivity::class.java)
            intent.putExtra("update",true)
            if (binding.editLay.tvDo.text.toString().contains("Battery Saver")){
                intent.putExtra("ACTION_TYPE","battery_saver")
                intent.putExtra("saver",binding.editLay.tvDo.text.toString().contains("On"))
            }else{
                intent.putExtra("ACTION_TYPE","speak_aloud")
                intent.putExtra("speak",speak_text)
            }
            startForResult.launch(intent)
        }

        if (v?.id == R.id.tv_done) {
            event.isActive = binding.editLay.toggle.isChecked
            event.level = selectLevelAccordingly(binding.editLay.tvWhen.text.toString())
            event.connects = connects
            if (binding.editLay.tvDo.text.toString().contains("Speak")){
                event.action1 = speak_text
                event.action2 = false
            }else{
                event.action2 = binding.editLay.tvDo.text.toString().contains("On")
                event.action1 = ""
            }
            eventViewModel.updateEvent(event)
            binding.editLay.root.slideDown(this)
            binding.dimView.hide()
        }
    }
}