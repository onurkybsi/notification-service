package org.kybprototyping.notificationservice.domain.common.exception

/**
 * Represents the exceptions occurred during handling a use case.
 */
class UseCaseException(
    message: String,
    val dueToDataInvalidity: Boolean = false,
    val failures: Any? = null
): RuntimeException(message)

/**
 * Builds a [UseCaseException] with context of data invalidity.
 *
 * @param message exception message
 * @param failures object contains the details of the invalidity
 * @return built [UseCaseException]
 */
fun dataInvalidity(message: String, failures: Any): UseCaseException {
    return UseCaseException(message, true, failures)
}