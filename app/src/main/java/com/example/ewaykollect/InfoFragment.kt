package com.example.ewaykollect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible

class InfoFragment : Fragment() {

    private lateinit var aiService: AIFAQService
    private lateinit var infoContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_info, container, false)

        infoContainer = root.findViewById(R.id.info_container)
        aiService = AIFAQService()

        fetchArticleLinks()

        return root
    }

    private fun fetchArticleLinks() {
        aiService.getArticleLinks { articleList ->
            requireActivity().runOnUiThread {
                infoContainer.removeAllViews()
                for (article in articleList) {
                    addArticleItem(article.question, article.answer)
                }
            }
        }
    }

    private fun addArticleItem(title: String, url: String) {
        val context = requireContext()
        val articleItem = LayoutInflater.from(context).inflate(R.layout.item_info, infoContainer, false)

        val titleText = articleItem.findViewById<TextView>(R.id.info_title)
        val urlText = articleItem.findViewById<TextView>(R.id.info_url)

        titleText.text = title
        urlText.text = url
        urlText.visibility = View.GONE

        articleItem.setOnClickListener {
            if (urlText.isVisible) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                urlText.isVisible = true
            }
        }

        infoContainer.addView(articleItem)
    }
}