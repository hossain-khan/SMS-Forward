package com.enixcoda.smsforward

import android.content.Context
import androidx.preference.PreferenceManager

/**
 * A class for loading forwarding service preferences from the shared preferences.
 *
 * @property context The context used to access the shared preferences.
 */
class PreferencesLoader(private val context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Loads the SMS preferences from the shared preferences.
     *
     * @return An instance of [SMSPreferences] containing the loaded preferences.
     */
    fun loadSMSPreferences(): SMSPreferences {
        return SMSPreferences(
            enableSMS = sharedPreferences.getBoolean(context.getString(R.string.key_enable_sms), false),
            targetNumber = sharedPreferences.getString(context.getString(R.string.key_target_sms), "") ?: ""
        )
    }

    /**
     * Loads the Web preferences from the shared preferences.
     *
     * @return An instance of [WebPreferences] containing the loaded preferences.
     */
    fun loadWebPreferences(): WebPreferences {
        return WebPreferences(
            enableWeb = sharedPreferences.getBoolean(context.getString(R.string.key_enable_web), false),
            targetWeb = sharedPreferences.getString(context.getString(R.string.key_target_web), "") ?: ""
        )
    }

    /**
     * Loads the Telegram preferences from the shared preferences.
     *
     * @return An instance of [TelegramPreferences] containing the loaded preferences.
     */
    fun loadTelegramPreferences(): TelegramPreferences {
        return TelegramPreferences(
            enableTelegram = sharedPreferences.getBoolean(context.getString(R.string.key_enable_telegram), false),
            targetTelegram = sharedPreferences.getString(context.getString(R.string.key_target_telegram), "") ?: "",
            telegramToken = sharedPreferences.getString(context.getString(R.string.key_telegram_apikey), "") ?: ""
        )
    }

    /**
     * Loads the RocketChat preferences from the shared preferences.
     *
     * @return An instance of [RocketChatPreferences] containing the loaded preferences.
     */
    fun loadRocketChatPreferences(): RocketChatPreferences {
        return RocketChatPreferences(
            enableRocketChat = sharedPreferences.getBoolean(context.getString(R.string.key_enable_rocket_chat), false),
            rocketChatBaseUrl = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_base_url), "") ?: "",
            rocketChatUserId = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_user_id), "") ?: "",
            rocketChatToken = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_auth_key), "") ?: "",
            rocketChatChannel = sharedPreferences.getString(context.getString(R.string.key_rocket_chat_channel), "") ?: ""
        )
    }

    /**
     * Loads the Twilio preferences from the shared preferences.
     *
     * @return An instance of [TwilioPreferences] containing the loaded preferences.
     */
    fun loadTwilioPreferences(): TwilioPreferences {
        return TwilioPreferences(
            enableTwilio = sharedPreferences.getBoolean(context.getString(R.string.key_enable_twilio), false),
            twilioAccountSid = sharedPreferences.getString(context.getString(R.string.key_twilio_account_sid), "") ?: "",
            twilioAuthToken = sharedPreferences.getString(context.getString(R.string.key_twilio_auth_token), "") ?: "",
            twilioFromNumber = sharedPreferences.getString(context.getString(R.string.key_twilio_from), "") ?: "",
            twilioToNumber = sharedPreferences.getString(context.getString(R.string.key_twilio_to), "") ?: ""
        )
    }

    /**
     * Loads the Email preferences from the shared preferences.
     *
     * @return An instance of [EmailPreferences] containing the loaded preferences.
     */
    fun loadEmailPreferences(): EmailPreferences {
        return EmailPreferences(
            enableEmail = sharedPreferences.getBoolean(context.getString(R.string.key_enable_email), false),
            smtpHost = sharedPreferences.getString(context.getString(R.string.key_smtp_host), "") ?: "",
            smtpPort = sharedPreferences.getString(context.getString(R.string.key_smtp_port), "") ?: "",
            smtpUser = sharedPreferences.getString(context.getString(R.string.key_smtp_user), "") ?: "",
            smtpPassword = sharedPreferences.getString(context.getString(R.string.key_smtp_password), "") ?: "",
            fromEmail = sharedPreferences.getString(context.getString(R.string.key_from_email), "") ?: "",
            toEmail = sharedPreferences.getString(context.getString(R.string.key_to_email), "") ?: ""
        )
    }
}