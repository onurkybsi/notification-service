package org.kybprototyping.notificationservice.adapter.emailsender

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("emailsender")
@EnableConfigurationProperties(SmtpProperties::class)
internal class SpringConfiguration {
    @Bean
    internal fun jakartaMimeMessageBuilder(smtpProperties: SmtpProperties) =
        JakartaMimeMessageBuilder(smtpProperties)

    @Bean
    internal fun jakartaSender(mimeMessageBuilder: JakartaMimeMessageBuilder) =
        JakartaSender(mimeMessageBuilder)
}
