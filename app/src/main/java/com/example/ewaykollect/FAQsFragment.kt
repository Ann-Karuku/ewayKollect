package com.example.ewaykollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FAQsFragment : Fragment() {

    private lateinit var searchFAQs: EditText
    private lateinit var faqAnswer: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_faqs, container, false)
        // Set the title
        (activity as AppCompatActivity).supportActionBar?.title = "FAQs"

        searchFAQs=root.findViewById(R.id.search_faqs)
        faqAnswer=root.findViewById(R.id.faq_answer_1)

        val aiService=AIFAQService()

        val btnSubmitQuery: FloatingActionButton=root.findViewById(R.id.btn_submit_question)
        btnSubmitQuery.setOnClickListener{
            val query = searchFAQs.text.toString()

            if (query.isNotEmpty()) {
                aiService.getAIResponse(query) { response ->
                    // Update the UI with the AI response
                    if (response != null) {
                        faqAnswer.text = response
                        faqAnswer.visibility = View.VISIBLE
                    } else {
                        faqAnswer.text = "No answer available. Please try again."
                        faqAnswer.visibility = View.VISIBLE
                    }
                }
            }
        }


        return root
    }

}