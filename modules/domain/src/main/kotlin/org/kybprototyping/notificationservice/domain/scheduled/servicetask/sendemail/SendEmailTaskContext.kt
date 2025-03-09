package org.kybprototyping.notificationservice.domain.scheduled.servicetask.sendemail

import org.kybprototyping.notificationservice.domain.usecase.notification.SendEmailInput
import java.time.OffsetDateTime
import java.util.UUID

internal data class SendEmailTaskContext(
    val input: SendEmailInput,
    val failures: List<Failure>? = null,
    val output: Output? = null,
) {
    internal companion object {
        internal fun SendEmailTaskContext.with(vararg failures: Failure) =
            if (this.failures.isNullOrEmpty()) {
                copy(failures = failures.toList())
            } else {
                copy(
                    failures = ArrayList<Failure>()
                        .also { it.addAll(this.failures) }
                        .also { it.addAll(failures) }
                )
            }

        internal fun SendEmailTaskContext.with(output: Output) = copy(output = output)
    }

    internal data class Failure(
        val timestamp: OffsetDateTime,
        val type: FailureType,
        val message: String? = null,
    )

    internal data class Output(
        val id: UUID,
        val status: Status,
        val failureType: FailureType? = null,
    )

    internal enum class Status {
        SUCCESSFUL,
        FAILED,
    }

    internal enum class FailureType {
        TEMPLATE_NOT_FOUND,
        EMAIL_SENDER_FAILURE,
    }
}
