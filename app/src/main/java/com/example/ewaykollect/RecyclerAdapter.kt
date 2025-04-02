package com.example.ewaykollect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(
    private var recyclers: List<RecyclerItem>
) : RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.recyclerName)
        val location: TextView = itemView.findViewById(R.id.recyclerLocation)
        val types: TextView = itemView.findViewById(R.id.recyclerTypes)
        val rating: TextView = itemView.findViewById(R.id.recyclerRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
       return recyclers.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val recycler = recyclers[position]
        holder.name.text = recycler.name
        holder.location.text = recycler.location
        holder.types.text = "Accepts: ${recycler.acceptedTypes.joinToString()}"
        holder.rating.text = "Rating: ${recycler.rating}"
    }

    fun updateList(newList: List<RecyclerItem>) {
        recyclers = newList
        notifyDataSetChanged()
    }
}

