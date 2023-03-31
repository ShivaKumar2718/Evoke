package com.siva.evoke.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EventViewModelFactory(private val eventDao: EventDao):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EventViewModel::class.java))
            return EventViewModel(eventDao) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }

}