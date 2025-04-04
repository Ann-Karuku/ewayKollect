package com.example.ewaykollect

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText

class FAQsFragment : Fragment() {

    private lateinit var aiService: AIFAQService
    private lateinit var faqContainer: LinearLayout
    private lateinit var searchInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_faqs, container, false)

        faqContainer = root.findViewById(R.id.faq_container)
        searchInput = root.findViewById(R.id.search_faqs)
        aiService = AIFAQService()

        fetchFAQs()

        // Handle search input
        searchInput.addTextChangedListener { text ->
            filterFAQs(text.toString())
        }

        return root
    }

    private fun fetchFAQs() {
        aiService.getAIFaqs { faqList ->
            requireActivity().runOnUiThread {
                Log.d("FAQsFragment", "Received FAQs: $faqList") // Debugging log

                faqContainer.removeAllViews()

                if (faqList.isEmpty()) {
                    Log.e("FAQsFragment", "No FAQs received!")
                }

                for (faq in faqList) {
                    addFaqItem(faq.question, faq.answer)
                }
            }
        }
    }

    private fun addFaqItem(question: String, answer: String) {
        val context = requireContext()

        val faqItem = LayoutInflater.from(context).inflate(R.layout.item_faq, faqContainer, false)

        val questionText = faqItem.findViewById<TextView>(R.id.faq_question)
        val answerText = faqItem.findViewById<TextView>(R.id.faq_answer)

        questionText.text = question
        answerText.text = answer
        answerText.visibility = View.GONE  // Hide answer initially

        // Toggle visibility when clicking the question
        faqItem.setOnClickListener {
            answerText.isVisible = !answerText.isVisible
        }

        faqContainer.addView(faqItem)
    }

    private fun filterFAQs(query: String) {
        for (i in 0 until faqContainer.childCount) {
            val faqItem = faqContainer.getChildAt(i)
            val questionText = faqItem.findViewById<TextView>(R.id.faq_question)

            if (questionText.text.toString().contains(query, ignoreCase = true)) {
                faqItem.visibility = View.VISIBLE
            } else {
                faqItem.visibility = View.GONE
            }
        }
    }
}
