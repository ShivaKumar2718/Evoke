package com.siva.evoke.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.siva.evoke.R
import com.siva.evoke.activities.EventAndActionActivity
import com.siva.evoke.activities.MainActivity
import com.siva.evoke.adapter.TypeAdapter
import com.siva.evoke.interfaces.OnItemsClick
import com.siva.evoke.model.Types

class EventTypeBottomSheet(var mainActivity: MainActivity) : BottomSheetDialogFragment(), OnItemsClick {

    private val types: ArrayList<Types> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.bottom_sheet_lay,container,false)
        val recyclerView = v.findViewById<RecyclerView>(R.id.recyclerViewForEvents)

//        types.add(Types("Battery Save Mode","Eg: When Battery Save Mode is turned off"))
        types.add(Types("Battery Level","Eg: When Battery Level rises above 95%"))
        types.add(Types("Charger","Eg: When my phone connects to power"))

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        val adapter = TypeAdapter(types,mainActivity,this)
        recyclerView.adapter = adapter

        return v
    }

    override fun onItemClick(adapterPosition: Int) {
        val i = Intent(mainActivity,EventAndActionActivity::class.java)
        i.putExtra("update",false)
        when (types[adapterPosition].type) {
            "Battery Level" -> {
                i.putExtra("from_where","battery_level")
            }
            else -> {
                i.putExtra("from_where","charger")
            }
        }
        startActivity(i)
    }

}