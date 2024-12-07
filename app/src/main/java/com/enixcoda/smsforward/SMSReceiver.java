package com.enixcoda.smsforward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.Keep;

/**
 * BroadcastReceiver to handle incoming SMS messages and forward them via various methods.
 */
@Keep
public class SMSReceiver extends BroadcastReceiver {

    /**
     * Called when an SMS message is received.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SMSReceiver", "onReceive: action " + intent.getAction());
        if (!intent.getAction().equals(android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
            return;

        PreferencesLoader preferencesLoader = new PreferencesLoader(context);

        SMSPreferences smsPreferences = preferencesLoader.loadSMSPreferences();
        WebPreferences webPreferences = preferencesLoader.loadWebPreferences();
        TelegramPreferences telegramPreferences = preferencesLoader.loadTelegramPreferences();
        RocketChatPreferences rocketChatPreferences = preferencesLoader.loadRocketChatPreferences();
        TwilioPreferences twilioPreferences = preferencesLoader.loadTwilioPreferences();
        EmailPreferences emailPreferences = preferencesLoader.loadEmailPreferences();

        Log.d("SMSReceiver", "onReceive: enableSMS = " + smsPreferences.getEnableSMS());
        Log.d("SMSReceiver", "onReceive: enableTelegram = " + telegramPreferences.getEnableTelegram());
        Log.d("SMSReceiver", "onReceive: enableRocketChat = " + rocketChatPreferences.getEnableRocketChat());
        Log.d("SMSReceiver", "onReceive: enableWeb = " + webPreferences.getEnableWeb());
        Log.d("SMSReceiver", "onReceive: enableTwilio = " + twilioPreferences.getEnableTwilio());
        Log.d("SMSReceiver", "onReceive: enableEmail = " + emailPreferences.getEnableEmail());

        if (!smsPreferences.getEnableSMS() && !telegramPreferences.getEnableTelegram() && !rocketChatPreferences.getEnableRocketChat() && !webPreferences.getEnableWeb() && !twilioPreferences.getEnableTwilio()) {
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

            if (senderNumber.equals(smsPreferences.getTargetNumber())) {
                // reverse message
                String formatRegex = "To (\\+?\\d+?):\\n((.|\\n)*)";
                if (rawMessageContent.matches(formatRegex)) {
                    String forwardNumber = rawMessageContent.replaceFirst(formatRegex, "$1");
                    String forwardContent = rawMessageContent.replaceFirst(formatRegex, "$2");
                    Forwarder.sendSMS(forwardNumber, forwardContent);
                }
            } else {
                // normal message, forward via all enabled methods

                if (smsPreferences.isValid()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding SMS to " + smsPreferences.getTargetNumber());
                    Forwarder.forwardViaSMS(senderNumber, rawMessageContent, smsPreferences.getTargetNumber());
                }

                if (telegramPreferences.isValid()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Telegram to " + telegramPreferences.getTargetTelegram());
                    Forwarder.forwardViaTelegram(senderNumber, rawMessageContent, telegramPreferences.getTargetTelegram(), telegramPreferences.getTelegramToken());
                }

                if (rocketChatPreferences.isValid()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding RocketChat to " + rocketChatPreferences.getRocketChatChannel());
                    Forwarder.forwardViaRocketChat(rocketChatPreferences.getRocketChatBaseUrl(), rocketChatPreferences.getRocketChatUserId(), rocketChatPreferences.getRocketChatToken(), rocketChatPreferences.getRocketChatChannel());
                }

                if (twilioPreferences.isValid()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Twilio to " + twilioPreferences.getTwilioToNumber());
                    Forwarder.forwardViaTwilio(twilioPreferences.getTwilioAccountSid(), twilioPreferences.getTwilioAuthToken(), twilioPreferences.getTwilioFromNumber(), twilioPreferences.getTwilioToNumber(), rawMessageContent);
                }

                if (webPreferences.isValid()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Web to " + webPreferences.getTargetWeb());
                    Forwarder.forwardViaWeb(senderNumber, rawMessageContent, webPreferences.getTargetWeb());
                }

                if (webPreferences.isValid()) {
                    Log.d("SMSReceiver", "onReceive: Forwarding Email to " + emailPreferences.getToEmail());
                    Forwarder.forwardViaEmail(senderNumber, rawMessageContent, emailPreferences);
                }
            }
        }
    }
}
