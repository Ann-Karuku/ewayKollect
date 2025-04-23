package com.example.ewaykollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(context, "Please sign in", Toast.LENGTH_SHORT).show()
            return null
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationAdapter(emptyList()) { notification ->
            markAsRead(notification)
        }
        recyclerView.adapter = notificationAdapter

        fetchNotifications()

        return view
    }

    private fun fetchNotifications() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    NotificationItem(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        type = doc.getString("type") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0,
                        read = doc.getBoolean("read") ?: false
                    )
                } ?: emptyList()
                notificationAdapter.updateData(notifications)
                if (notifications.isEmpty()) {
                    Toast.makeText(context, "No notifications", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun markAsRead(notification: NotificationItem) {
        if (!notification.read) {
            firestore.collection("notifications")
                .document(notification.id)
                .update("read", true)
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}