package com.example.ewaykollect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter(
    private var recyclers: List<RecyclerItem>
) : RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.recyclerName)
        val location: TextView = itemView.findViewById(R.id.recyclerLocation)
        val types: TextView = itemView.findViewById(R.id.recyclerTypes)
        val rating: TextView = itemView.findViewById(R.id.recyclerRating)
        val logo: ImageView = itemView.findViewById(R.id.recyclerLogo)
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
        Glide.with(holder.itemView.context)
            .load(recycler.logoUrl ?: R.drawable.ic_placeholder_logo)
            .placeholder(R.drawable.ic_placeholder_logo)
            .into(holder.logo)
        holder.itemView.setOnClickListener {
            val bundle = bundleOf("RECYCLER_ITEM" to recycler)
            it.findNavController().navigate(R.id.action_recyclersFragment_to_recyclerDetailsFragment, bundle)
        }
    }

    fun updateList(newList: List<RecyclerItem>) {
        recyclers = newList
        notifyDataSetChanged()
    }
}