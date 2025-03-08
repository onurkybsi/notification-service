package org.kybprototyping.notificationservice.adapter

import arrow.atomic.AtomicInt
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.kybprototyping.notificationservice.adapter.CoroutineDispatcherProviderImpl.Companion.buildCoroutineDispatcher
import java.util.Collections
import java.util.stream.IntStream.range

internal class CoroutineDispatcherProviderImplTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "null, 5, 5, 10",
            "null, null, 10, 10",
            "5, 5, 5, 10"
        ],
        nullValues = ["null"]
    )
    fun `should build CoroutineDispatcher by given parameters`(
        corePoolSize: Int?,
        maxPoolSize: Int?,
        expectedNumOfThreadsUtilized: Int,
        expectedNumOfJobsCompleted: Int,
    ): Unit = runBlocking {
        // given
        val dispatcher = buildCoroutineDispatcher("my-super-pool", corePoolSize, maxPoolSize)
        val scope = CoroutineScope(dispatcher)

        // when
        val threadIdsUtilized = Collections.synchronizedSet(HashSet<Long>())
        val numOfJobsCompleted = AtomicInt(0)
        range(1, 11)
            .mapToObj { i ->
                scope.launch {
                    println("$i started...")
                    Thread.sleep(100)
                    threadIdsUtilized.add(Thread.currentThread().id)
                    numOfJobsCompleted.incrementAndGet()
                    println("$i finished")
                }
            }
            .toList()
            .joinAll()

        // then
        assertThat(threadIdsUtilized.size).isEqualTo(expectedNumOfThreadsUtilized)
        assertThat(numOfJobsCompleted.get()).isEqualTo(expectedNumOfJobsCompleted)
    }
}