package com.enixcoda.smsforward

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


/**
 * A task for forwarding SMS messages using the Twilio API.
 *
 * @property accountSid The Account SID for Twilio.
 * @property authToken The authentication token for Twilio.
 * @property fromNumber The phone number sending the SMS.
 * @property toNumber The phone number receiving the SMS.
 * @property message The message content to be sent.
 */
class ForwardTaskForTwilio(
    private val accountSid: String,
    private val authToken: String,
    private val fromNumber: String,
    private val toNumber: String,
    private val message: String
) {

    private val client = OkHttpClient()

    fun sendTwilioSms() {
        /**
         * Twilio API URL for sending SMS
         *
         * curl 'https://api.twilio.com/2010-04-01/Accounts/ACC-SIC/Messages.json' -X POST \
         * --data-urlencode 'To=+1647XYZMNO' \
         * --data-urlencode 'From=+1XYZMNOPQ' \
         * --data-urlencode 'Body=Message' \
         * -u ACC-SID:AuthToken
         */
        val url = "https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json"
        val requestBody = FormBody.Builder()
            .add("From", fromNumber)
            .add("To", toNumber)
            .add("Body", message)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", Credentials.basic(accountSid, authToken))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TwilioTask", "Failed to send SMS: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("TwilioTask", "SMS sent successfully: ${response.body?.string()}")
                } else {
                    Log.e("TwilioTask", "Failed to send SMS: ${response.message}")
                }
            }
        })
    }
}