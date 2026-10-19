package com.pooja.assistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiHelper {
    // এখানে আপনার Google AI Studio-র API Key বসাতে পারবেন
    var apiKey: String = "YOUR_GEMINI_API_KEY_HERE"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
    তোমার নাম পূজা (Pooja)। তুমি একজন মিষ্টি, বুদ্ধিমান ও নম্র ফিমেল পার্সোনাল এআই অ্যাসিস্ট্যান্ট।
    তোমার দায়িত্ব:
    ১. ব্যবহারকারীর মনের ভাব ও অসম্পূর্ণ কথা সহজে বুঝে স্বাভাবিকভাবে কথা বলা।
    ২. কোনো মেসেজ বা স্ক্রিনের লেখা এলে নির্দেশমতো বাংলা, হিন্দি বা ইংরেজিতে সুন্দর করে গুছিয়ে বলা।
    ৩. ব্যবহারকারী কোনো ভুল করলে মিষ্টিভাবে সেই ভুল ধরিয়ে দেওয়া ও সঠিক পরামর্শ দেওয়া।
    ৪. ব্যবহারকারী কোনো অ্যাপ খুলতে বললে বা অ্যাকশন নিতে বললে JSON ফরম্যাটে উত্তর দেওয়া:
       {"action": "OPEN_APP", "target": "whatsapp/youtube/etc", "reply": "আমি হোয়াটসঅ্যাপ খুলে দিচ্ছি।"}
       অন্যথায় সাধারণ আলাপের জন্য:
       {"action": "CHAT", "target": "", "reply": "তোমার উত্তর"}
    উত্তরে সর্বদা শুধু এই JSON অবজেক্টটি প্রদান করবে।
    """

    suspend fun getResponse(userCommand: String, screenContext: String): JSONObject = withContext(Dispatchers.IO) {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val promptText = "$SYSTEM_PROMPT\n\n[স্ক্রিনের বর্তমান টেক্সট]: $screenContext\n[ব্যবহারকারীর নির্দেশ]: $userCommand"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text").trim()

            val cleanJson = if (rawText.startsWith("```json")) {
                rawText.removePrefix("```json").removeSuffix("```").trim()
            } else if (rawText.startsWith("```")) {
                rawText.removePrefix("```").removeSuffix("```").trim()
            } else {
                rawText
            }

            JSONObject(cleanJson)
        } catch (e: Exception) {
            JSONObject().apply {
                put("action", "CHAT")
                put("target", "")
                put("reply", "দুঃখিত, সংযোগে কিছুটা সমস্যা হচ্ছে। আপনি কি আবার বলবেন?")
            }
        }
    }
}
