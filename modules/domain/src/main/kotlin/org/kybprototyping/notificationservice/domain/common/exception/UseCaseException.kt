package org.kybprototyping.notificationservice.domain.common.exception

/**
 * Represents the exceptions occurred during handling a use case.
 */
class UseCaseException(
    override val message: String,
    val dueToDataInvalidity: Boolean = false,
    val dueToNonExistentData: Boolean = false,
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
    return UseCaseException(message, dueToDataInvalidity = true, failures = failures)
}

/**
 * Builds a [UseCaseException] with context of non-existent data.
 *
 * @param message exception message
 * @return built [UseCaseException]
 */
fun nonExistentData(message: String): UseCaseException {
    return UseCaseException(message, dueToNonExistentData = true)
}