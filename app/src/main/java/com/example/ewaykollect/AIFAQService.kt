package com.example.ewaykollect

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

data class FAQItem(val question: String, val answer: String)

class AIFAQService {
    private val client = OkHttpClient()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    fun getAIFaqs(callback: (List<FAQItem>) -> Unit) {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Generate 10 frequently asked questions about e-waste recycling along with their answers. Format each FAQ with 'Q: ' for the question and 'A: ' for the answer, separated by a newline. Example:\nQ: What is e-waste?\nA: E-waste refers to discarded electronic devices.")
                        })
                    })
                })
            })
        }.toString()

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AIFAQService", "FAQ request failed: ${e.message}")
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    try {
                        Log.d("AIFAQService", "FAQ API Response: $responseBody")
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("error")) {
                            Log.e("AIFAQService", "FAQ API error: ${jsonResponse.getJSONObject("error").getString("message")}")
                            callback(emptyList())
                            return
                        }
                        if (jsonResponse.has("candidates")) {
                            val candidatesArray = jsonResponse.getJSONArray("candidates")
                            if (candidatesArray.length() > 0) {
                                val firstCandidate = candidatesArray.getJSONObject(0)
                                if (firstCandidate.has("content")) {
                                    val contentObject = firstCandidate.getJSONObject("content")
                                    if (contentObject.has("parts")) {
                                        val partsArray = contentObject.getJSONArray("parts")
                                        if (partsArray.length() > 0) {
                                            val faqText = partsArray.getJSONObject(0).getString("text")
                                            Log.d("AIFAQService", "FAQ Text: $faqText")
                                            val faqs = parseFaqs(faqText)
                                            Log.d("AIFAQService", "Parsed FAQs: $faqs")
                                            callback(faqs)
                                            return
                                        }
                                    }
                                }
                            }
                        }
                        Log.e("AIFAQService", "Invalid FAQ response structure")
                        callback(emptyList())
                    } catch (e: JSONException) {
                        Log.e("AIFAQService", "FAQ JSON parsing error: ${e.message}")
                        callback(emptyList())
                    }
                } ?: run {
                    Log.e("AIFAQService", "FAQ response body is null")
                    callback(emptyList())
                }
            }
        })
    }

    fun getArticleLinks(callback: (List<FAQItem>) -> Unit) {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Provide 10 links to reputable articles about e-waste and recycling, with a title and URL for each. Format as a list with each item starting with 'Title: ' followed by 'URL: ' on a new line. Example:\nTitle: E-Waste Crisis\nURL: https://example.com/ewaste-crisis")
                        })
                    })
                })
            })
        }.toString()

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AIFAQService", "Article request failed: ${e.message}")
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    try {
                        Log.d("AIFAQService", "Article API Response: $responseBody")
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("error")) {
                            Log.e("AIFAQService", "Article API error: ${jsonResponse.getJSONObject("error").getString("message")}")
                            callback(emptyList())
                            return
                        }
                        if (jsonResponse.has("candidates")) {
                            val candidatesArray = jsonResponse.getJSONArray("candidates")
                            if (candidatesArray.length() > 0) {
                                val firstCandidate = candidatesArray.getJSONObject(0)
                                if (firstCandidate.has("content")) {
                                    val contentObject = firstCandidate.getJSONObject("content")
                                    if (contentObject.has("parts")) {
                                        val partsArray = contentObject.getJSONArray("parts")
                                        if (partsArray.length() > 0) {
                                            val articleText = partsArray.getJSONObject(0).getString("text")
                                            Log.d("AIFAQService", "Article Text: $articleText")
                                            val articles = parseArticles(articleText)
                                            Log.d("AIFAQService", "Parsed Articles: $articles")
                                            callback(articles)
                                            return
                                        }
                                    }
                                }
                            }
                        }
                        Log.e("AIFAQService", "Invalid Article response structure")
                        callback(emptyList())
                    } catch (e: JSONException) {
                        Log.e("AIFAQService", "Article JSON parsing error: ${e.message}")
                        callback(emptyList())
                    }
                } ?: {
                    Log.e("AIFAQService", "Article response body is null")
                    callback(emptyList())
                }()
            }
        })
    }

    private fun parseFaqs(responseText: String): List<FAQItem> {
        val faqList = mutableListOf<FAQItem>()
        val lines = responseText.split("\n")

        var question = ""
        var answer = ""

        for (line in lines) {
            if (line.startsWith("Q: ")) {
                if (question.isNotEmpty() && answer.isNotEmpty()) {
                    faqList.add(FAQItem(question, answer))
                    answer = ""
                }
                question = line.removePrefix("Q: ").trim()
            } else if (line.startsWith("A: ")) {
                answer = line.removePrefix("A: ").trim()
            }
        }

        if (question.isNotEmpty() && answer.isNotEmpty()) {
            faqList.add(FAQItem(question, answer))
        }

        return faqList
    }

    private fun parseArticles(responseText: String): List<FAQItem> {
        val articleList = mutableListOf<FAQItem>()
        val lines = responseText.split("\n")

        var title = ""
        var url = ""

        for (line in lines) {
            if (line.startsWith("Title: ")) {
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    articleList.add(FAQItem(title, url))
                    url = ""
                }
                title = line.removePrefix("Title: ").trim()
            } else if (line.startsWith("URL: ")) {
                url = line.removePrefix("URL: ").trim()
            }
        }

        if (title.isNotEmpty() && url.isNotEmpty()) {
            articleList.add(FAQItem(title, url))
        }

        return articleList
    }
}