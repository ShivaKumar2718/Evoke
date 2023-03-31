package com.siva.evoke.receivers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.siva.evoke.R
import com.siva.evoke.db.Event
import com.siva.evoke.db.EventDao
import com.siva.evoke.db.EventDatabase
import com.siva.evoke.services.TTS

@RequiresApi(Build.VERSION_CODES.O)
class ChargerPlugInReceiver : BroadcastReceiver() {

    private var NOTIFICATION_ID = 2
    private val CHANNEL_ID = "PluginReceiverChannel"
    private var db: EventDatabase? = null
    private var eventDao: EventDao? = null

    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)

        //Creating the reference for the dao to access the db
        db = EventDatabase.getDatabaseInstance(context.applicationContext)
        eventDao = db?.eventDao()

        val status = intent!!.action
        Log.d("Inside","OnReceive")
        val intent = Intent(context,TTS::class.java)

        when (status) {
            Intent.ACTION_POWER_CONNECTED -> {
                //Charger Connected
                Thread {
                    eventDao?.let { checkWhetherToNotifyIfConnected(it.getEventsInDb("Charger"),intent,context) }
                    // Do something with the data here
                }.start()
                Log.d("Connected","true")
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                //Charger DisConnected
                Log.d("Connected","false")
                Thread {
                    eventDao?.let { checkWhetherToNotifyIfDisConnected(it.getEventsInDb("Charger"),intent,context) }
                    // Do something with the data here
                }.start()
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                Thread{
                    eventDao?.let {
                        checkWhetherToNotifyIfBatteryLevelChanges(batteryLevel,
                            it.getEventsInDb("Battery Level"),intent,context)
                    }
                }.start()
                Log.d("Battery", "Battery level changed to $batteryLevel%")
            }
        }
        //closing the connection of db so that resources will be free
        db?.close()
    }


    private fun checkWhetherToNotifyIfBatteryLevelChanges(
        batteryLevel: Int,
        eventsInDb: List<Event>,
        intent: Intent,
        context: Context
    ) {
        for (event in eventsInDb){
            if (event.level.contains("Equals")){
                //when battery level equals
                if (batteryLevel == getLevelOfBattery(event.level)){
                    performRequiredAction(event,context,"Battery Level Equals $batteryLevel%",intent)
                }
            }else if(event.level.contains("Rises")){
                //when battery level rises
                if (batteryLevel == getLevelOfBattery(event.level) + 1){
                    performRequiredAction(event,context,"Battery Level Rises Above $batteryLevel%",intent)
                }
            }else if (event.level.contains("Falls")){
                //when battery level falls
                if (batteryLevel == getLevelOfBattery(event.level) - 1 ){
                    performRequiredAction(event,context,"Battery Level Falls Below $batteryLevel%",intent)
                }
            }
        }
    }

    private fun getLevelOfBattery(string: String): Int{
        val str_array = string.split(" ")
        return str_array[str_array.size-1].removeSuffix("%").toInt()
    }


    private fun performRequiredAction(event: Event,context: Context,s:String,intent: Intent) {
        if (event.action1.isEmpty()){
            //turn battery saver on/off
            if (event.action2){
                //Saver On
                turnBatterySaverOn(context,s)
            }else{
                //Saver Off
                turnBatterySaverOff(context,s)
            }
        }else{
            intent.putExtra("STATUS",event.action1)
        }
        if (intent.extras != null) context.startService(intent)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "BATTERY SAVER NOTIFY",
                    NotificationManager.IMPORTANCE_LOW
                )
            val manager: NotificationManager? = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun triggerNotification(context: Context,title:String,text:String){
        val builder: Notification.Builder =
            Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.play_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
        val notificationManager: NotificationManagerCompat =
            NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun checkWhetherToNotifyIfDisConnected(events: List<Event>, intent: Intent, context: Context) {
        Log.d("DisConnected", events.size.toString())
        for (event in events){
            if (event.isActive){
                if (!event.connects){
                    //if Disconnected
                    if (event.action1.isNotEmpty()){
                        intent.putExtra("STATUS",event.action1)
                    }else{
                        //turn battery saver on/off
                        if (event.action2){
                            //Saver On
                            turnBatterySaverOn(context,"Charger Disconnected")
                        }else{
                            //Saver Off
                            turnBatterySaverOff(context,"Charger Disconnected")
                        }
                    }
                }
            }
        }
        if (intent.extras != null) context.startService(intent)
    }

    private fun turnBatterySaverOff(context: Context,s:String) {
        // Check if battery saver is currently enabled
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        val isBatterySaverEnabled = powerManager?.isPowerSaveMode

        if (isBatterySaverEnabled!!){
            Log.d("Inside","Battery Saver")
            //notify user to turn off
            triggerNotification(context,s,"Please turn off the battery save mode!!")
        }
    }

    private fun turnBatterySaverOn(context: Context,s:String) {
        // Check if battery saver is currently enabled
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        val isBatterySaverEnabled = powerManager?.isPowerSaveMode

        if (!isBatterySaverEnabled!!){
            Log.d("Inside","Battery Saver")
            //notify user to turn on
            triggerNotification(context,s,"Please turn on the battery save mode")
        }
    }

    private fun checkWhetherToNotifyIfConnected(events: List<Event>, intent: Intent, context: Context) {
        Log.d("Connected", events.size.toString())
        for (event in events){
            if (event.isActive){
                if (event.connects){
                    //if connected
                    if (event.action1.isNotEmpty()){
                        intent.putExtra("STATUS",event.action1)
                    }else{
                        //turn battery saver on/off
                        if (event.action2){
                            //Saver On
                            turnBatterySaverOn(context,"Charger Connected")
                        }else{
                            //Saver Off
                            turnBatterySaverOff(context,"Charger Connected")
                        }
                    }
                }
            }
        }
                if (intent.extras != null) context.startService(intent)
    }
}