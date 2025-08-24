@file:JvmName("FlowExtensions")

package net.infumia.lock

import java.time.Duration
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Transforms a Flow to execute each emission within a lock. Each emitted value is processed while
 * holding the lock, ensuring thread-safe emission processing.
 *
 * @param lock the lock to acquire for each emission
 * @return a new Flow that processes each emission within the lock
 */
fun <T> Flow<T>.withLock(lock: Lock): Flow<T> = flow {
    collect { value -> lock.withLockSuspend { emit(value) } }
}

/**
 * Transforms a Flow to execute each emission within a lock, with a timeout. Each emitted value is
 * processed while holding the lock with the specified timeout.
 *
 * @param lock the lock to acquire for each emission
 * @param timeout the timeout for acquiring the lock
 * @return a new Flow that processes each emission within the lock
 * @throws IllegalStateException if the lock cannot be acquired within the timeout
 */
fun <T> Flow<T>.withLock(lock: Lock, timeout: Duration): Flow<T> = flow {
    collect { value -> lock.withLockSuspend(timeout) { emit(value) } }
}

/**
 * Transforms a Flow to try to execute each emission within a lock. Emissions that cannot acquire
 * the lock are dropped silently.
 *
 * @param lock the lock to try to acquire for each emission
 * @return a new Flow that processes emissions that can acquire the lock
 */
fun <T> Flow<T>.tryWithLock(lock: Lock): Flow<T> = flow {
    collect { value -> lock.tryWithLockSuspend { emit(value) } }
}

/**
 * Buffers Flow emissions and processes them in batches with lock protection. Collects emissions
 * until either the batch size is reached or the timeout occurs, then processes the entire batch
 * while holding the lock.
 *
 * @param lock the lock to acquire for batch processing
 * @param batchSize the maximum number of items to collect before processing
 * @param timeout the maximum time to wait before processing a partial batch
 * @return a new Flow that emits lists of batched items
 */
fun <T> Flow<T>.batchWithLock(
    lock: Lock,
    batchSize: Int,
    timeout: Duration = Duration.ofSeconds(1),
): Flow<List<T>> = flow {
    val buffer = mutableListOf<T>()

    collectLatest { value ->
        buffer.add(value)

        if (buffer.size >= batchSize) {
            lock.withLockSuspend { emit(buffer.toList()) }
            buffer.clear()
        } else {
            withTimeoutOrNull(timeout.toMillis()) {
                awaitCancellation()
            }

            if (buffer.isNotEmpty()) {
                lock.withLockSuspend { emit(buffer.toList()) }
                buffer.clear()
            }
        }
    }

    if (buffer.isNotEmpty()) {
        lock.withLockSuspend { emit(buffer.toList()) }
    }
}

/**
 * Collects the flow with lock protection. Each collected value is processed within the lock
 * context.
 *
 * @param lock the lock to acquire during collection
 * @param action the action to perform for each collected value
 */
suspend fun <T> Flow<T>.collectWithLock(lock: Lock, action: suspend (T) -> Unit) {
    collect { value -> lock.withLockSuspend { action(value) } }
}

/**
 * Processes Flow elements serially with lock protection. Ensures that only one element is being
 * processed at a time, maintaining order.
 *
 * @param lock the lock to use for serialization
 * @param transform the transformation to apply to each element
 * @return a new Flow with transformed elements
 */
fun <T, R> Flow<T>.mapSeriallyWithLock(lock: Lock, transform: suspend (T) -> R): Flow<R> = flow {
    collect { value ->
        val result = lock.withLockSuspend { transform(value) }
        emit(result)
    }
}

/**
 * Filters Flow elements with lock protection. The predicate is evaluated while holding the lock.
 *
 * @param lock the lock to acquire during filtering
 * @param predicate the predicate to test each element
 * @return a new Flow containing only elements that match the predicate
 */
fun <T> Flow<T>.filterWithLock(lock: Lock, predicate: suspend (T) -> Boolean): Flow<T> = flow {
    collect { value ->
        val shouldEmit = lock.withLockSuspend { predicate(value) }
        if (shouldEmit) {
            emit(value)
        }
    }
}

/**
 * Reduces Flow elements with lock protection. The accumulation operation is performed while holding
 * the lock for thread safety.
 *
 * @param lock the lock to acquire during reduction
 * @param initial the initial value for the accumulation
 * @param operation the accumulation operation
 * @return the final accumulated value
 */
suspend fun <T, R> Flow<T>.reduceWithLock(
    lock: Lock,
    initial: R,
    operation: suspend (acc: R, value: T) -> R,
): R {
    var accumulator = initial
    collect { value -> accumulator = lock.withLockSuspend { operation(accumulator, value) } }
    return accumulator
}

/**
 * Combines two flows with lock protection. The combination function is executed while holding the
 * lock.
 *
 * @param other the other flow to combine with
 * @param lock the lock to acquire during combination
 * @param transform the function to combine values from both flows
 * @return a new Flow containing combined values
 */
fun <T1, T2, R> Flow<T1>.combineWithLock(
    other: Flow<T2>,
    lock: Lock,
    transform: suspend (T1, T2) -> R,
): Flow<R> = combine(other) { value1, value2 -> lock.withLockSuspend { transform(value1, value2) } }

/**
 * Zips two flows with lock protection. The zip function is executed while holding the lock.
 *
 * @param other the other flow to zip with
 * @param lock the lock to acquire during zipping
 * @param transform the function to combine paired values
 * @return a new Flow containing zipped values
 */
fun <T1, T2, R> Flow<T1>.zipWithLock(
    other: Flow<T2>,
    lock: Lock,
    transform: suspend (T1, T2) -> R,
): Flow<R> = zip(other) { value1, value2 -> lock.withLockSuspend { transform(value1, value2) } }

/**
 * Creates a Flow that emits values from a shared resource protected by a lock. Useful for creating
 * flows from resources that require synchronized access.
 *
 * @param lock the lock protecting the resource
 * @param producer the function that produces values from the protected resource
 * @return a Flow that safely accesses the protected resource
 */
fun <T> flowWithLock(lock: Lock, producer: suspend () -> T): Flow<T> = flow {
    val value = lock.withLockSuspend { producer() }
    emit(value)
}

/**
 * Creates a Flow that continuously polls a protected resource. Useful for monitoring resources that
 * require synchronized access.
 *
 * @param lock the lock protecting the resource
 * @param interval the polling interval
 * @param producer the function that produces values from the protected resource
 * @return a Flow that polls the protected resource at regular intervals
 */
fun <T> flowPollingWithLock(lock: Lock, interval: Duration, producer: suspend () -> T): Flow<T> =
    flow {
        while (currentCoroutineContext().isActive) {
            val value = lock.withLockSuspend { producer() }
            emit(value)
            delay(interval.toMillis())
        }
    }
