package com.enixcoda.smsforward

import android.util.Log
import java.util.Properties
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * A class to handle the task of forwarding an email.
 *
 * @property smtpHost The SMTP server host.
 * @property smtpPort The SMTP server port.
 * @property smtpUser The SMTP server username.
 * @property smtpPassword The SMTP server password.
 * @property fromEmail The email address from which the email is sent.
 * @property toEmail The email address to which the email is sent.
 * @property subject The subject of the email.
 * @property body The body content of the email.
 */
class ForwardTaskForEmail(
    private val smtpHost: String,
    private val smtpPort: String,
    private val smtpUser: String,
    private val smtpPassword: String,
    private val fromEmail: String,
    private val toEmail: String,
    private val subject: String,
    private val body: String
) {
    /**
     * Sends the email using the provided SMTP server details.
     */
    fun send() {
        val properties = Properties().apply {
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort)
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }

        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpUser, smtpPassword)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(fromEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = this@ForwardTaskForEmail.subject
                setText(body)
            }

            Transport.send(message)
            Log.d("EmailTask", "Email sent successfully.")
        } catch (e: MessagingException) {
            Log.e("EmailTask", "Failed to send email: ${e.message}", e)
        }
    }
}