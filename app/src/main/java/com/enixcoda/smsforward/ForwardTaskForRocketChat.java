package com.enixcoda.smsforward;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForwardTaskForRocketChat extends AsyncTask<Void, Void, String> {

    private static final String TAG = "RocketChatTask";
    private static final String POST_MESSAGE_ENDPOINT = "/api/v1/chat.postMessage";

    @NonNull private final String baseUrl;
    @NonNull private final String userId;
    @NonNull private final String authToken;
    @NonNull private final String channelName;

    public ForwardTaskForRocketChat(String baseUrl, String userId, String token, String channel) {
        this.baseUrl = baseUrl;
        this.userId = userId;
        this.authToken = token;
        this.channelName = channel;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            // Send a message
            return sendMessage(authToken);
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

    private String sendMessage(String authToken) throws IOException {
        URL url = new URL(baseUrl + POST_MESSAGE_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Auth-Token", authToken);
        connection.setRequestProperty("X-User-Id", userId);
        connection.setDoOutput(true);

        String jsonInputString = "{\"channel\": \"" + channelName + "\", \"text\": \"Hello, world!\"}";

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(jsonInputString.getBytes());
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null) {
            Log.d(TAG, "Message sent successfully: " + result);
        } else {
            Log.e(TAG, "Failed to send message.");
        }
    }
}
