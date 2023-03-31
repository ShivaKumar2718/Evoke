package com.siva.evoke.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {

    @Insert
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM EVENT_DATA_TABLE")
    fun getAllEvents() : LiveData<List<Event>>

    @Query("SELECT * FROM EVENT_DATA_TABLE WHERE event_type = :event_type")
    fun getEventsInDb(event_type:String) : List<Event>
}