package com.example.ewaykollect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EwasteAdapter(private val ewasteList: List<EwasteItem>) : RecyclerView.Adapter<EwasteAdapter.EwasteViewHolder>() {

    // ViewHolder class that represents each item view in the RecyclerView
    class EwasteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewEwaste)
        val nameTextView: TextView = itemView.findViewById(R.id.textViewEwasteName)
        val numTextView: TextView = itemView.findViewById(R.id.textViewEwasteNum)
        val typeTextView: TextView = itemView.findViewById(R.id.textViewEwasteType)
        val stateTextView: TextView = itemView.findViewById(R.id.textViewEwasteState)
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EwasteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ewaste, parent, false)
        return EwasteViewHolder(view)
    }

    // Bind the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: EwasteViewHolder, position: Int) {
        val ewasteItem = ewasteList[position]
        holder.nameTextView.text = ewasteItem.name
        holder.numTextView.text = ewasteItem.number.toString()
        holder.stateTextView.text=ewasteItem.state
        holder.typeTextView.text=ewasteItem.type


        // Use Glide to load the image into the ImageView
        Glide.with(holder.itemView.context)
            .load(ewasteItem.imageUrl)
            .into(holder.imageView)
    }

    // Return the size of the dataset
    override fun getItemCount() = ewasteList.size
}
