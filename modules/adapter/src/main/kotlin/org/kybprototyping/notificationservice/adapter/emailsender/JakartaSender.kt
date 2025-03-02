package org.kybprototyping.notificationservice.adapter.emailsender

import arrow.core.right
import org.kybprototying.notificationservice.common.runExceptionCatching
import org.kybprototyping.notificationservice.domain.port.EmailSenderPort
import jakarta.mail.Transport

internal class JakartaSender(private val mimeMessageBuilder: JakartaMimeMessageBuilder) : EmailSenderPort {
    // TODO: Anyway to make this async?
    override suspend fun send(
        from: String,
        to: String,
        subject: String,
        content: String,
    ) =
        runExceptionCatching {
            val mimeMessage = mimeMessageBuilder.build(from, to, subject, content)
            Transport.send(mimeMessage).right()
        }
}
