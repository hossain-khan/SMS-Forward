package com.enixcoda.smsforward;

import android.telephony.SmsManager;
import android.util.Log;

public class Forwarder {
    static final int MAX_SMS_LENGTH = 120;

    public static void sendSMS(String number, String content) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, content, null, null);
    }

    public static void forwardViaSMS(String senderNumber, String forwardContent, String forwardNumber) {
        String forwardPrefix = String.format("From %s:\n", senderNumber);

        try {
            if ((forwardPrefix + forwardContent).getBytes().length > MAX_SMS_LENGTH) {
                // there is a length limit in SMS, if the message length exceeds it, separate the meta data and content
                sendSMS(forwardNumber, forwardPrefix);
                sendSMS(forwardNumber, forwardContent);
            } else {
                // if it's not too long, combine meta data and content to save money
                sendSMS(forwardNumber, forwardPrefix + forwardContent);
            }
        } catch (RuntimeException e) {
            Log.w(Forwarder.class.toString(), e.toString());
        }
    }

    public static void forwardViaTelegram(String senderNumber, String message, String targetTelegramID, String telegramToken) {
        new ForwardTaskForTelegram(senderNumber, message, targetTelegramID, telegramToken).execute();
    }

    public static void forwardViaRocketChat(String baseUrl, String userId, String token, String channel) {
        new ForwardTaskForRocketChat(baseUrl, userId, token, channel).execute();
    }

    public static void forwardViaTwilio(String accountSid, String authToken, String fromNumber, String toNumber, String message) {
        ForwardTaskForTwilio forwardTaskForTwilio = new ForwardTaskForTwilio(accountSid, authToken, fromNumber, toNumber, message);
        forwardTaskForTwilio.sendTwilioSms();
    }

    public static void forwardViaWeb(String senderNumber, String message, String endpoint) {
        new ForwardTaskForWeb(senderNumber, message, endpoint).send();
    }

    public static void forwardViaEmail(String senderNumber, String message, EmailPreferences emailPref) {
        new ForwardTaskForEmail(
                emailPref.getSmtpHost(),
                emailPref.getSmtpPort(),
                emailPref.getSmtpUser(),
                emailPref.getSmtpPassword(),
                emailPref.getFromEmail(),
                emailPref.getToEmail(),
                "Forwarded SMS message from " + senderNumber,
                message
        ).send();
    }
}
