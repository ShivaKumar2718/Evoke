package com.siva.evoke.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EventViewModel(private val eventDao: EventDao) : ViewModel() {

    val events = eventDao.getAllEvents()

    fun insertEvent(event: Event)=viewModelScope.launch {
        eventDao.insertEvent(event)
    }

    fun updateEvent(event: Event)=viewModelScope.launch {
        eventDao.updateEvent(event)
    }

    fun deleteEvent(event: Event)=viewModelScope.launch {
        eventDao.deleteEvent(event)
    }

}