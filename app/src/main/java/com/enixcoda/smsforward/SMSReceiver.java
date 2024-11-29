package com.enixcoda.smsforward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SMSReceiver", "onReceive: action " + intent.getAction());
        if (!intent.getAction().equals(android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
            return;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        final boolean enableSMS = sharedPreferences.getBoolean(context.getString(R.string.key_enable_sms), false);
        final String targetNumber = sharedPreferences.getString(context.getString(R.string.key_target_sms), "");

        final boolean enableWeb = sharedPreferences.getBoolean(context.getString(R.string.key_enable_web), false);
        final String targetWeb = sharedPreferences.getString(context.getString(R.string.key_target_web), "");

        final boolean enableTelegram = sharedPreferences.getBoolean(context.getString(R.string.key_enable_telegram), false);
        final String targetTelegram = sharedPreferences.getString(context.getString(R.string.key_target_telegram), "");
        final String telegramToken = sharedPreferences.getString(context.getString(R.string.key_telegram_apikey), "");

        final boolean enableRocketChat = sharedPreferences.getBoolean(context.getString(R.string.key_enable_rocket_chat), false);
        final String rocketChatBaseUrl = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_base_url), "");
        final String rocketChatUserId = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_user_id), "");
        final String rocketChatToken = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_auth_key), "");
        final String rocketChatChannel = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_channel), "");

        final boolean enableTwilio = sharedPreferences.getBoolean(context.getString(R.string.key_enable_twilio), false);
        final String twilioAccountSid = sharedPreferences.getString(context.getString(R.string.key_twilio_account_sid), "");
        final String twilioAuthToken = sharedPreferences.getString(context.getString(R.string.key_twilio_auth_token), "");
        final String twilioFromNumber = sharedPreferences.getString(context.getString(R.string.key_twilio_from), "");
        final String twilioToNumber = sharedPreferences.getString(context.getString(R.string.key_twilio_to), "");

        Log.d("SMSReceiver", "onReceive: enableSMS = " + enableSMS);
        Log.d("SMSReceiver", "onReceive: enableTelegram = " + enableTelegram);
        Log.d("SMSReceiver", "onReceive: enableRocketChat = " + enableRocketChat);
        Log.d("SMSReceiver", "onReceive: enableWeb = " + enableWeb);
        Log.d("SMSReceiver", "onReceive: enableTwilio = " + enableTwilio);

        if (!enableSMS && !enableTelegram && !enableRocketChat && !enableWeb && !enableTwilio) {
            Log.d("SMSReceiver", "onReceive: SMS Forwarding is disabled");
            return;
        } else {
            Log.d("SMSReceiver", "onReceive: SMS Forwarding is enabled");
        }

        final Bundle bundle = intent.getExtras();
        final Object[] pduObjects = (Object[]) bundle.get("pdus");
        if (pduObjects == null) return;

        for (Object messageObj : pduObjects) {
            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) messageObj, (String) bundle.get("format"));
            String senderNumber = currentMessage.getDisplayOriginatingAddress();
            String rawMessageContent = currentMessage.getDisplayMessageBody();

            if (senderNumber.equals(targetNumber)) {
                // reverse message
                String formatRegex = "To (\\+?\\d+?):\\n((.|\\n)*)";
                if (rawMessageContent.matches(formatRegex)) {
                    String forwardNumber = rawMessageContent.replaceFirst(formatRegex, "$1");
                    String forwardContent = rawMessageContent.replaceFirst(formatRegex, "$2");
                    Forwarder.sendSMS(forwardNumber, forwardContent);
                }
            } else {
                // normal message, forwarded
                if (enableSMS && !targetNumber.isEmpty()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding SMS to " + targetNumber);
                    Forwarder.forwardViaSMS(senderNumber, rawMessageContent, targetNumber);
                }

                if (enableTelegram && !targetTelegram.isEmpty() && !telegramToken.isEmpty()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Telegram to " + targetTelegram);
                    Forwarder.forwardViaTelegram(senderNumber, rawMessageContent, targetTelegram, telegramToken);
                }

                if (enableRocketChat &&
                        !rocketChatBaseUrl.isEmpty() &&
                        !rocketChatUserId.isEmpty() &&
                        !rocketChatChannel.isEmpty() &&
                        !rocketChatToken.isEmpty()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding RocketChat to " + rocketChatChannel);
                    Forwarder.forwardViaRocketChat(rocketChatBaseUrl, rocketChatUserId, rocketChatToken, rocketChatChannel);
                }

                if (enableTwilio &&
                        !twilioAccountSid.isEmpty() &&
                        !twilioAuthToken.isEmpty() &&
                        !twilioFromNumber.isEmpty() &&
                        !twilioToNumber.isEmpty()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Twilio to " + twilioToNumber);

                    Forwarder.forwardViaTwilio(twilioAccountSid, twilioAuthToken, twilioFromNumber, twilioToNumber, rawMessageContent);
                }

                if (enableWeb && !targetWeb.isEmpty()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Web to " + targetWeb);
                    Forwarder.forwardViaWeb(senderNumber, rawMessageContent, targetWeb);
                }
            }
        }
    }
}
