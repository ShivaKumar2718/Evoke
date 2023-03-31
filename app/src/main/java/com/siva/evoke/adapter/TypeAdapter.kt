package com.siva.evoke.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.siva.evoke.R
import com.siva.evoke.interfaces.OnItemsClick
import com.siva.evoke.databinding.EventTypeLayBinding
import com.siva.evoke.model.Types

class TypeAdapter(var types: ArrayList<Types>,val context: Context, val onItemsClick: OnItemsClick) : RecyclerView.Adapter<TypeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = EventTypeLayBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return types.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(types[position],onItemsClick,context)
    }

    class ViewHolder(private val binding: EventTypeLayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(types: Types,onItemsClick: OnItemsClick,context: Context){
            binding.tvType.text = types.type
            binding.tvEg.text = types.example

            if(types.type.contains("Battery")){
                binding.ivType.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.battery_level,null))
            }else {
                binding.ivType.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.charger,null))
            }

            binding.root.setOnClickListener {
                onItemsClick.onItemClick(adapterPosition)
            }
        }
    }
}