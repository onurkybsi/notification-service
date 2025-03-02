package org.kybprototyping.notificationservice.adapter.emailsender

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Properties

@ConfigurationProperties(prefix = "ports.email-sender.smtp") // TODO: Get rid of this from here!
internal data class SmtpProperties(
    val host: String,
    val port: Int,
    val auth: Boolean,
    val starttls: Starttls,
    val username: String? = null,
    val password: String? = null
) {
    internal companion object {
        internal fun SmtpProperties.toProperties() =
            Properties().also {
                it["mail.smtp.host"] = this.host
                it["mail.smtp.port"] = this.port
                it["mail.smtp.auth"] = this.auth
                it["mail.smtp.starttls.enable"] = this.starttls.enable
                it["mail.smtp.starttls.required"] = this.starttls.required
            }
    }

    internal data class Starttls(val enable: Boolean, val required: Boolean)
}
