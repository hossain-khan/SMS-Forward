package com.enixcoda.smsforward

import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ForwardTaskForWeb(
    private val senderNumber: String,
    private val message: String,
    private val endpoint: String
) {
    private val client: OkHttpClient = OkHttpClient()
    private val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType()

    fun send() {
        val bodyJson = JSONObject()
        bodyJson.put("from", senderNumber)
        bodyJson.put("message", message)
        val body: RequestBody = bodyJson.toString().toRequestBody(jsonMediaType)

        val request: Request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("WebTask", "Failed to send SMS: ${e.message}", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("WebTask", "Failed to send SMS: ${response.body?.string()}")
                    } else {
                        Log.d(
                            "WebTask",
                            "SMS sent successfully. Response: ${response.body?.string()}"
                        )
                    }
                }
            }
        })
    }
}
