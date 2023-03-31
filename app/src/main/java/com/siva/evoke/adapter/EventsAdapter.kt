package com.siva.evoke.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.siva.evoke.R
import com.siva.evoke.activities.MainActivity
import com.siva.evoke.databinding.EventsLayBinding
import com.siva.evoke.db.Event
import com.siva.evoke.utils.getPixelsFromDp
import com.siva.evoke.utils.hide

class EventsAdapter(var events: List<Event>, private val context: Context, private val onItemsClick: MainActivity) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = EventsLayBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position],onItemsClick,position,events.size,context)
    }

    class ViewHolder(private val binding: EventsLayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(events: Event, onItemsClick: MainActivity, position: Int, size: Int, context: Context){
            if (events.event_type.contains("Battery")){
                binding.tvType.text = "When battery level ${events.level.lowercase()}"
                binding.ivType.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.battery_level))
            }else{
                var connects = ""
                if(events.connects){
                    connects = "connected"
                    binding.ivType.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.charger))
                } else {
                    connects = "disconnected"
                    binding.ivType.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.charger_disconnect))
                }

                binding.tvType.text = "When charger is $connects"
            }

            val battery_saver_flag = if(events.action2)  "On" else "Off"
            if (events.action1.isEmpty()){
                binding.ivAction.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.battery_saver))
            }else{
                binding.ivAction.setImageDrawable(AppCompatResources.getDrawable(context,R.drawable.speak_text))
            }
            if (events.isActive){
                itemView.alpha = 1f
                if (events.action1.isEmpty()){
                    binding.tvAction.text = "Battery Saver $battery_saver_flag"
                }else{
                    binding.tvAction.text = "Speak Aloud"
                }
            }else{
                itemView.alpha = 0.5f
                binding.tvAction.text = "Disabled"
            }

            if (position == size-1){
                binding.view.hide()
                binding.tvAction.setPadding(0,0,0, getPixelsFromDp(15, context))
            }

            binding.root.setOnClickListener {
                onItemsClick.onItemClick(adapterPosition, events)
            }
        }
    }

    interface OnEventsClick {
        fun onItemClick(adapterPosition:Int, events: Event)
    }
}