package org.kybprototyping.notificationservice.domain.port

import arrow.core.Either
import org.kybprototying.notificationservice.common.Failure
import org.kybprototying.notificationservice.common.UnexpectedFailure

/**
 * Represents the API that sends emails.
 */
interface EmailSenderPort {
    /**
     * Sends an email by given values.
     *
     * @param from email sender address
     * @param to email recipient address
     * @param subject subject of the email
     * @param content content of the email
     * @return [UnexpectedFailure] if something went unexpectedly wrong
     */
    suspend fun send(
        from: String,
        to: String,
        subject: String,
        content: String,
    ): Either<Failure, Unit>
}
