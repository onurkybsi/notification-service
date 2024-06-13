package org.kybprototyping.notificationservice.domain.common.interfaces

import org.springframework.transaction.annotation.Transactional

/**
 * Defines the function annotated with this annotation as transactional.
 *
 * The underlying infrastructure should begin a transaction before the function execution
 * for all the infrastructure components which can be executed transactional, and commit after the execution.
 * If an exception occurs during the execution, the transaction should be rolled back.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Transactional // TODO: Temporary solution, infrastructure should implement the logic when it sees the annotation!
annotation class Transactional