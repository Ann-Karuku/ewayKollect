package com.example.ewaykollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class RecyclerDetailsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recycler_details, container, false)

        firestore = FirebaseFirestore.getInstance()

        val recyclerItem = arguments?.getSerializable("RECYCLER_ITEM") as? RecyclerItem
            ?: return null

        view.findViewById<TextView>(R.id.detailName).text = recyclerItem.name
        view.findViewById<TextView>(R.id.detailLocation).text = recyclerItem.location
        view.findViewById<TextView>(R.id.detailTypes).text = "Accepts: ${recyclerItem.acceptedTypes.joinToString()}"
        view.findViewById<TextView>(R.id.detailRating).text = "Rating: ${recyclerItem.rating}"
        view.findViewById<TextView>(R.id.detailContact).text = "Contact: ${recyclerItem.contact}"
        view.findViewById<TextView>(R.id.detailPopularity).text = "Popularity: ${recyclerItem.popularityScore}"
        Glide.with(this)
            .load(recyclerItem.logoUrl ?: R.drawable.ic_placeholder_logo)
            .placeholder(R.drawable.ic_placeholder_logo)
            .into(view.findViewById(R.id.detailLogo))

        view.findViewById<Button>(R.id.requestPickupButton).setOnClickListener {
            requestPickup(recyclerItem)
        }

        return view
    }

    private fun requestPickup(recyclerItem: RecyclerItem) {
        val request = hashMapOf(
            "companyId" to recyclerItem.id,
            "companyName" to recyclerItem.name,
            "requestTime" to System.currentTimeMillis(),
            "status" to "pending"
        )

        firestore.collection("pickupRequests")
            .add(request)
            .addOnSuccessListener {
                Toast.makeText(context, "Pickup requested for ${recyclerItem.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}