package com.siva.evoke.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Event::class], version = 1, exportSchema = false)
abstract class EventDatabase : RoomDatabase() {

    abstract fun eventDao():EventDao
    companion object{
        private var INSTANCE : EventDatabase? = null
        fun getDatabaseInstance(context: Context): EventDatabase{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        EventDatabase::class.java,
                        "event_database").build()
                }
                return instance
            }
        }
    }
}