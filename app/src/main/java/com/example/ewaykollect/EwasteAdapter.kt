package com.example.ewaykollect

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EwasteAdapter(
    private var ewasteList: List<EwasteItem>,
    private val onItemClick: (EwasteItem) -> Unit = {}
) : RecyclerView.Adapter<EwasteAdapter.EwasteViewHolder>() {

    class EwasteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewEwaste)
        val nameTextView: TextView = itemView.findViewById(R.id.textViewEwasteName)
        val numTextView: TextView = itemView.findViewById(R.id.textViewEwasteNum)
        val typeTextView: TextView = itemView.findViewById(R.id.textViewEwasteType)
        val stateTextView: TextView = itemView.findViewById(R.id.textViewEwasteState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EwasteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ewaste, parent, false)
        return EwasteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EwasteViewHolder, position: Int) {
        val ewasteItem = ewasteList[position]
        holder.nameTextView.text = ewasteItem.name
        holder.numTextView.text = ewasteItem.number
        holder.stateTextView.text = ewasteItem.state
        holder.typeTextView.text = ewasteItem.type

        Glide.with(holder.itemView.context)
            .load(ewasteItem.imageUrl.takeIf { it.isNotEmpty() } ?: R.drawable.baseline_camera)
            .placeholder(R.drawable.baseline_camera)
            .into(holder.imageView)

        holder.itemView.setOnClickListener { onItemClick(ewasteItem) }
    }

    override fun getItemCount() = ewasteList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<EwasteItem>) {
        ewasteList = newList
        notifyDataSetChanged()
    }
}