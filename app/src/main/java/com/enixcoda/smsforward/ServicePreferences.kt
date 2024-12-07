package com.enixcoda.smsforward

/**
 * Data class representing SMS preferences.
 *
 * @property enableSMS Boolean indicating if SMS is enabled.
 * @property targetNumber The target phone number for SMS.
 */
data class SMSPreferences(
    val enableSMS: Boolean,
    val targetNumber: String
) {
    /**
     * Checks if SMS preferences are valid.
     *
     * @return Boolean indicating if SMS preferences are valid.
     */
    fun isValid(): Boolean {
        return enableSMS && targetNumber.isNotEmpty()
    }
}

/**
 * Data class representing Web preferences.
 *
 * @property enableWeb Boolean indicating if Web is enabled.
 * @property targetWeb The target URL for Web.
 */
data class WebPreferences(
    val enableWeb: Boolean,
    val targetWeb: String
) {
    /**
     * Checks if Web preferences are valid.
     *
     * @return Boolean indicating if Web preferences are valid.
     */
    fun isValid(): Boolean {
        return enableWeb && targetWeb.isNotEmpty()
    }
}

/**
 * Data class representing Telegram preferences.
 *
 * @property enableTelegram Boolean indicating if Telegram is enabled.
 * @property targetTelegram The target Telegram ID.
 * @property telegramToken The API token for Telegram.
 */
data class TelegramPreferences(
    val enableTelegram: Boolean,
    val targetTelegram: String,
    val telegramToken: String
) {
    /**
     * Checks if Telegram preferences are valid.
     *
     * @return Boolean indicating if Telegram preferences are valid.
     */
    fun isValid(): Boolean {
        return enableTelegram && targetTelegram.isNotEmpty() && telegramToken.isNotEmpty()
    }
}

/**
 * Data class representing RocketChat preferences.
 *
 * @property enableRocketChat Boolean indicating if RocketChat is enabled.
 * @property rocketChatBaseUrl The base URL for RocketChat.
 * @property rocketChatUserId The user ID for RocketChat.
 * @property rocketChatToken The authentication token for RocketChat.
 * @property rocketChatChannel The channel name for RocketChat.
 */
data class RocketChatPreferences(
    val enableRocketChat: Boolean,
    val rocketChatBaseUrl: String,
    val rocketChatUserId: String,
    val rocketChatToken: String,
    val rocketChatChannel: String
) {
    /**
     * Checks if RocketChat preferences are valid.
     *
     * @return Boolean indicating if RocketChat preferences are valid.
     */
    fun isValid(): Boolean {
        return enableRocketChat &&
                rocketChatBaseUrl.isNotEmpty() &&
                rocketChatUserId.isNotEmpty() &&
                rocketChatToken.isNotEmpty() &&
                rocketChatChannel.isNotEmpty()
    }
}

/**
 * Data class representing Twilio preferences.
 *
 * @property enableTwilio Boolean indicating if Twilio is enabled.
 * @property twilioAccountSid The account SID for Twilio.
 * @property twilioAuthToken The authentication token for Twilio.
 * @property twilioFromNumber The sender phone number for Twilio.
 * @property twilioToNumber The recipient phone number for Twilio.
 */
data class TwilioPreferences(
    val enableTwilio: Boolean,
    val twilioAccountSid: String,
    val twilioAuthToken: String,
    val twilioFromNumber: String,
    val twilioToNumber: String
) {
    /**
     * Checks if Twilio preferences are valid.
     *
     * @return Boolean indicating if Twilio preferences are valid.
     */
    fun isValid(): Boolean {
        return enableTwilio &&
                twilioAccountSid.isNotEmpty() &&
                twilioAuthToken.isNotEmpty() &&
                twilioFromNumber.isNotEmpty() &&
                twilioToNumber.isNotEmpty()
    }
}

/**
 * Data class representing Email preferences.
 *
 * @property enableEmail Boolean indicating if Email is enabled.
 * @property smtpHost The SMTP host for Email.
 * @property smtpPort The SMTP port for Email.
 * @property smtpUser The SMTP user for Email.
 * @property smtpPassword The SMTP password for Email.
 * @property fromEmail The sender email address.
 * @property toEmail The recipient email address.
 */
data class EmailPreferences(
    val enableEmail: Boolean,
    val smtpHost: String,
    val smtpPort: String,
    val smtpUser: String,
    val smtpPassword: String,
    val fromEmail: String,
    val toEmail: String
) {
    /**
     * Checks if Email preferences are valid.
     *
     * @return Boolean indicating if Email preferences are valid.
     */
    fun isValid(): Boolean {
        return enableEmail &&
                smtpHost.isNotEmpty() &&
                smtpPort.isNotEmpty() &&
                smtpUser.isNotEmpty() &&
                smtpPassword.isNotEmpty() &&
                fromEmail.isNotEmpty() &&
                toEmail.isNotEmpty()
    }
}