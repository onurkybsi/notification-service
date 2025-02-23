package org.kybprototyping.notificationservice.adapter.repository.common

import arrow.core.Either
import arrow.core.left
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kybprototyping.notificationservice.adapter.repository.PostgreSQLContainerRunner
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.JooqImpl
import org.kybprototyping.notificationservice.adapter.repository.notificationtemplate.Tables.NOTIFICATION_TEMPLATE
import org.kybprototyping.notificationservice.domain.common.Failure
import org.kybprototyping.notificationservice.domain.common.UnexpectedFailure
import org.kybprototyping.notificationservice.domain.model.NotificationChannel
import org.kybprototyping.notificationservice.domain.model.NotificationLanguage
import org.kybprototyping.notificationservice.domain.model.NotificationType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@SpringBootTest(
    classes = [
        R2dbcAutoConfiguration::class,
        R2dbcTransactionManagerAutoConfiguration::class,
        TransactionalExecutorSpringConfiguration::class,
        JooqImpl::class,
    ],
    properties = [
        "ports.notification-template-repository.impl=jooq",
        "logging.level.org.springframework.r2dbc=TRACE",
    ],
)
internal class TransactionalExecutorSpringTransactionManagerImplIntegrationTest() : PostgreSQLContainerRunner() {
    @Autowired
    private lateinit var templateRepositoryPort: JooqImpl

    @Autowired
    private lateinit var transactionAwareDSLContextProxy: TransactionAwareDSLContextProxy

    @Autowired
    private lateinit var transactionManager: ReactiveTransactionManager

    @Autowired
    private lateinit var underTest: TransactionalExecutorSpringTransactionManagerImpl

    @BeforeEach
    fun setUp() {
        runBlocking {
            transactionAwareDSLContextProxy.dslContext().deleteFrom(NOTIFICATION_TEMPLATE).awaitSingle()
        }
    }

    @Test
    fun `should commit when no exception occurred and Failure returned`() =
        runTest {
            // given

            // when
            underTest.execute {
                templateRepositoryPort.create(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.WELCOME,
                    language = NotificationLanguage.EN,
                    subject = StringUtils.EMPTY,
                    content = StringUtils.EMPTY,
                )
                templateRepositoryPort.create(
                    channel = NotificationChannel.EMAIL,
                    type = NotificationType.PASSWORD_RESET,
                    language = NotificationLanguage.EN,
                    subject = StringUtils.EMPTY,
                    content = StringUtils.EMPTY,
                )
            }

            // then
            templateRepositoryPort.findBy(
                channel = null,
                type = NotificationType.WELCOME,
                language = null,
            ).shouldBeRight()
            templateRepositoryPort.findBy(
                channel = null,
                type = NotificationType.PASSWORD_RESET,
                language = null,
            ).shouldBeRight()
        }

    @Test
    fun `should not commit when exception occurred`() =
        runTest {
            // given

            // when
            val actual =
                assertThrows<RuntimeException> {
                    underTest.execute<Either<Failure, Any>> {
                        templateRepositoryPort.create(
                            channel = NotificationChannel.EMAIL,
                            type = NotificationType.WELCOME,
                            language = NotificationLanguage.EN,
                            subject = StringUtils.EMPTY,
                            content = StringUtils.EMPTY,
                        )
                        throw RuntimeException("Something went wrong during the transaction!")
                    }
                }

            // then
            assertThat(actual.message).isEqualTo("Something went wrong during the transaction!")
            templateRepositoryPort.findBy(null, null, null).shouldBeRight(emptyList())
        }

    @Test
    fun `should not commit when Failure returned`() =
        runTest {
            // given

            // when
            val actual =
                underTest.execute {
                    templateRepositoryPort.create(
                        channel = NotificationChannel.EMAIL,
                        type = NotificationType.WELCOME,
                        language = NotificationLanguage.EN,
                        subject = StringUtils.EMPTY,
                        content = StringUtils.EMPTY,
                    )
                    UnexpectedFailure("Something went wrong during the transaction!").left()
                }

            // then
            actual.shouldBeLeft(UnexpectedFailure("Something went wrong during the transaction!"))
            templateRepositoryPort.findBy(null, null, null).shouldBeRight(emptyList())
        }

    @Test
    fun `should not commit when Spring started transaction failed even if the execution is successful `() =
        runTest {
            // given
            val transactionalOperator = TransactionalOperator.create(transactionManager)

            // when
            assertThrows<RuntimeException> {
                transactionalOperator.executeAndAwait {
                    underTest.execute {
                        templateRepositoryPort.create(
                            channel = NotificationChannel.EMAIL,
                            type = NotificationType.WELCOME,
                            language = NotificationLanguage.EN,
                            subject = StringUtils.EMPTY,
                            content = StringUtils.EMPTY,
                        )
                    }

                    throw RuntimeException("Something went wrong during the transaction!")
                }
            }

            // then
            templateRepositoryPort.findBy(null, null, null).shouldBeRight(emptyList())
        }
}
