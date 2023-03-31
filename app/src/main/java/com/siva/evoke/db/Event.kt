package com.siva.evoke.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_data_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
    var event_id: Int,
    var event_type: String,
    var level: String,
    var connects: Boolean,
    var action1: String,
    var action2: Boolean,
    var isActive: Boolean
    )