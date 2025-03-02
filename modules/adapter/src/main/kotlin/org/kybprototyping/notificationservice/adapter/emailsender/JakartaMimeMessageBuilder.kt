package org.kybprototyping.notificationservice.adapter.emailsender

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.kybprototyping.notificationservice.adapter.emailsender.SmtpProperties.Companion.toProperties

internal class JakartaMimeMessageBuilder(private val smtpProperties: SmtpProperties) {
    private val properties = smtpProperties.toProperties()
    private val authenticator = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication =
            PasswordAuthentication(smtpProperties.username, smtpProperties.password)
    }

    internal fun build(from: String, to: String, subject: String, content: String): MimeMessage {
        val message = MimeMessage(
            if(smtpProperties.auth) Session.getDefaultInstance(properties, authenticator)
            else Session.getDefaultInstance(properties)
        )
        message.setFrom(InternetAddress(from))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
        message.subject = subject
        message.setContent(buildHtmlContent(content))
        return message
    }

    private companion object {
        private fun buildHtmlContent(content: String): Multipart {
            val bodyPart = MimeBodyPart()
            bodyPart.setContent(content, "text/html; charset=utf-8")
            return MimeMultipart().also { it.addBodyPart(bodyPart) }
        }
    }
}
