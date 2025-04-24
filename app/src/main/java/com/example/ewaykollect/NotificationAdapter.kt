package com.example.ewaykollect

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageTextView: TextView = view.findViewById(R.id.textViewNotificationMessage)
        val timestampTextView: TextView = view.findViewById(R.id.textViewNotificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.messageTextView.text = notification.message
        holder.timestampTextView.text = android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", notification.timestamp)

        // Highlight unread notifications
        val backgroundColor = if (notification.read) {
            android.R.color.white
        } else {
            R.color.other_green
        }
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, backgroundColor))

        holder.itemView.setOnClickListener { onItemClick(notification) }
    }

    override fun getItemCount() = notifications.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<NotificationItem>) {
        notifications = newList
        notifyDataSetChanged()
    }
}