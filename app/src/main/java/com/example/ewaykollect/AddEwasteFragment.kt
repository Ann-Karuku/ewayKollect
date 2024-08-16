package com.example.ewaykollect
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast

class AddEwasteDialogFragment : DialogFragment() {
    // Categorized list of e-waste items
    private val items = arrayOf(
        "Mobile Phones", "Tablets", "Laptops",
        "Desktop Computers", "Monitors", "Printers",
        "Televisions", "Remote Controls", "DVD Players",
        "Washing Machines", "Refrigerators", "Microwaves",
        "Air Conditioners", "Electric Fans", "Heaters"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate your custom layout for the dialog
        val root: View = inflater.inflate(R.layout.fragment_add_ewaste, container, false)

        // Find the Spinner in the inflated layout
        val spinner = root.findViewById<Spinner>(R.id.spinnerType)

        // Creating an ArrayAdapter using the EwasteItems array and a custom layout
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            items
        )

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter

        return root
    }
}
