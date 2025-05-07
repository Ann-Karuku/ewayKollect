package com.example.ewaykollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class ChatMessage(val text: String, val isSent: Boolean, val timestamp: String)

class SupportFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: MaterialButton
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_support, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.chatRecyclerView)
        messageEditText = view.findViewById(R.id.messageEditText)
        sendButton = view.findViewById(R.id.sendButton)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Listen for messages
        db.collection("support_chats").document("user1").collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SupportFragment", "Firestore error: ${error.message}")
                    messages.addAll(getMockSupportMessages())
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }
                messages.clear()
                snapshot?.documents?.forEach { doc ->
                    val message = doc.toObject(ChatMessage::class.java)
                    message?.let { messages.add(it) }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }

        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                val timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                val message = ChatMessage(text, true, timestamp)
                db.collection("support_chats").document("user1").collection("messages")
                    .add(message)
                    .addOnFailureListener { e ->
                        Log.e("SupportFragment", "Failed to send message: ${e.message}")
                    }
                messageEditText.text.clear()
            }
        }
    }

    private fun getMockSupportMessages(): List<ChatMessage> {
        val timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        return listOf(
            ChatMessage("Welcome to EwayKollect support! How can we help with e-waste recycling?", false, timestamp),
            )
    }
}