package com.example.ewaykollect

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.json.JSONArray
import org.json.JSONObject

class RecyclersFragment : Fragment() {

    private lateinit var mapWebView: WebView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firestoreListener: ListenerRegistration
    private var userLocation: Pair<Double, Double>? = null
    private var allRecyclers: List<RecyclerItem> = emptyList()
    private val categories = listOf("Popular", "Highest Rated", "Nearest")

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.fragment_recyclers, container, false)

        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize WebView for the map
        mapWebView = root.findViewById(R.id.mapWebView)
        this.mapWebView.settings.javaScriptEnabled = true
        mapWebView.webViewClient = WebViewClient()
        mapWebView.loadUrl("file:///android_asset/map.html")

        // Wait for the WebView to load before updating the map
        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setupMap()
            }
        }

        val searchView = root.findViewById<SearchView>(R.id.search_recycler)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterRecyclers(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterRecyclers(newText ?: "")
                return true
            }
        })

        fetchRecyclers()

        return root
    }

    private fun setupMap() {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLocation = Pair(location.longitude, location.latitude)
                        updateMapCenter(location.longitude, location.latitude, 12)
                        updateMapMarkers()
                    } else {
                        // Fallback to a default location (Nairobi, Kenya)
                        userLocation = Pair(36.817223, -1.286389)
                        updateMapCenter(36.817223, -1.286389, 12)
                        updateMapMarkers()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    101
                )
                // Fallback to a default location (Nairobi, Kenya)
                userLocation = Pair(36.817223, -1.286389)
                updateMapCenter(36.817223, -1.286389, 12)
                updateMapMarkers()
            }
    }

    private fun updateMapCenter(lon: Double, lat: Double, zoom: Int) {
        mapWebView.evaluateJavascript("setMapCenter($lon, $lat, $zoom);") { }
    }

    private fun fetchRecyclers() {
        val collectionRef = firestore.collection("Recyclers")
        firestoreListener = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Error fetching recyclers: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            allRecyclers = snapshot?.documents?.mapNotNull { doc ->
                RecyclerItem(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    location = "${doc.get("location.subCounty")}, ${doc.get("location.county")}",
                    acceptedTypes = doc.get("acceptedTypes") as? List<String> ?: emptyList(),
                    contact = doc.get("contact.phone")?.toString() ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    coordinates = Pair(
                        doc.get("coordinates.longitude") as? Double ?: 0.0,
                        doc.get("coordinates.latitude") as? Double ?: 0.0
                    ),
                    popularityScore = doc.getLong("popularityScore")?.toInt() ?: 0
                )
            } ?: emptyList()

            updateMapMarkers()
            updateRecyclerGroups(recyclers = allRecyclers)
        }
    }

    private fun updateMapMarkers(recyclers: List<RecyclerItem> = allRecyclers) {
        val markersArray = JSONArray()
        recyclers.forEach { recycler ->
            val marker = JSONObject().apply {
                put("lon", recycler.coordinates.first)
                put("lat", recycler.coordinates.second)
                put("name", recycler.name)
                put("description", recycler.location)
            }
            markersArray.put(marker)
        }
        val markersJson = markersArray.toString()
        mapWebView.evaluateJavascript("addMarkers($markersJson);") { }
    }

    private fun filterRecyclers(query: String) {
        val filteredList = if (query.isEmpty()) {
            allRecyclers
        } else {
            allRecyclers.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true) ||
                        it.acceptedTypes.any { type -> type.contains(query, ignoreCase = true) }
            }
        }
        updateRecyclerGroups(recyclers = filteredList)
        updateMapMarkers(filteredList)
    }

    private fun updateRecyclerGroups(recyclers: List<RecyclerItem> = allRecyclers) {
        val verticalRecyclerGroups = view?.findViewById<LinearLayout>(R.id.verticalRecyclerContainer)
        verticalRecyclerGroups?.removeAllViews()

        categories.forEach { group ->
            val groupTitle = TextView(requireContext()).apply {
                text = "$group Recyclers"
                textSize = 18f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                setPadding(16, 16, 16, 8)
            }
            verticalRecyclerGroups?.addView(groupTitle)

            val recyclerView = RecyclerView(requireContext()).apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = RecyclerAdapter(
                    when (group) {
                        "Popular" -> recyclers.sortedByDescending { it.popularityScore }.take(5)
                        "Highest Rated" -> recyclers.sortedByDescending { it.rating }.take(5)
                        "Nearest" -> userLocation?.let { location ->
                            recyclers.sortedBy { recycler ->
                                val recyclerLocation = Location("").apply {
                                    latitude = recycler.coordinates.second
                                    longitude = recycler.coordinates.first
                                }
                                val userLoc = Location("").apply {
                                    latitude = location.second
                                    longitude = location.first
                                }
                                userLoc.distanceTo(recyclerLocation)
                            }.take(5)
                        } ?: recyclers.take(5)
                        else -> recyclers.take(5)
                    }
                )
            }
            verticalRecyclerGroups?.addView(recyclerView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener.remove()
    }
}