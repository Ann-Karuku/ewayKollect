package com.example.ewaykollect

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class AIFAQService {
    private val client = OkHttpClient()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    fun getAIFaqs(callback: (List<FAQItem>) -> Unit) {
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Generate 20 frequently asked questions about e-waste recycling along with their answers.")
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
                Log.e("API Error", "Request failed: ${e.message}")
                callback(emptyList())  // Return an empty list on failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    Log.d("API Response", it)
                    try {
                        val jsonResponse = JSONObject(it)

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

                                            val faqs = parseFaqs(faqText)
                                            callback(faqs)
                                            return
                                        }
                                    }
                                }
                            }
                        }
                        // If any required field is missing, log an error
                        Log.e("JSON Error", "Required fields missing in API response")
                    } catch (e: JSONException) {
                        Log.e("JSON Parsing Error", "Error parsing JSON: ${e.message}")
                    }
                } ?: Log.e("API Error", "Response body is null")
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
}

data class FAQItem(val question: String, val answer: String)
