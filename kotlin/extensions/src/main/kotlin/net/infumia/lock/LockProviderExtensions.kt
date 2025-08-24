@file:JvmName("LockProviderExtensions")

package net.infumia.lock

import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Creates a lock and executes the block with it. The lock is automatically created with default
 * settings.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use for the lock operations
 * @param block the block to execute with the created lock
 * @return the result of executing the block
 */
inline fun <T> LockProvider.withLock(
    identifier: String,
    executor: Executor,
    block: (Lock) -> T,
): T = create(identifier, executor).let(block)

/**
 * Creates a lock with timeout and executes the block with it.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use for the lock operations
 * @param timeout the timeout for the lock
 * @param block the block to execute with the created lock
 * @return the result of executing the block
 */
inline fun <T> LockProvider.withLock(
    identifier: String,
    executor: Executor,
    timeout: Duration,
    block: (Lock) -> T,
): T = create(identifier, executor, timeout).let(block)

/**
 * Creates a lock with full configuration and executes the block with it.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use for the lock operations
 * @param acquireTimeout the timeout for acquiring the lock
 * @param expiryTime the expiry time for the lock
 * @param block the block to execute with the created lock
 * @return the result of executing the block
 */
inline fun <T> LockProvider.withLock(
    identifier: String,
    executor: Executor,
    acquireTimeout: Duration,
    expiryTime: Duration,
    block: (Lock) -> T,
): T = create(identifier, executor, acquireTimeout, expiryTime).let(block)

/**
 * DSL for lock configuration and execution. Provides a fluent API for configuring locks before
 * execution.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use for the lock operations
 * @param configure the configuration block for the lock
 * @param block the block to execute with the configured lock
 * @return the result of executing the block
 */
inline fun <T> LockProvider.lock(
    identifier: String,
    executor: Executor,
    configure: LockBuilder.() -> Unit = {},
    block: (Lock) -> T,
): T = block(LockBuilder().apply(configure).createLock(this, identifier, executor))

/**
 * Builder class for fluent lock configuration. Provides a DSL-style API for configuring lock
 * parameters.
 */
class LockBuilder {
    /** The timeout for acquiring the lock. */
    var acquireTimeout: Duration? = null

    /** The expiry time for the lock. */
    var expiryTime: Duration? = null

    /** The acquire resolution time in milliseconds. */
    var acquireResolutionTimeMillis: Long? = null

    /**
     * Sets the acquire timeout using a Duration.
     *
     * @param duration the timeout duration
     */
    fun acquireTimeout(duration: Duration) {
        acquireTimeout = duration
    }

    /**
     * Sets the acquire timeout using amount and time unit.
     *
     * @param amount the amount of time
     * @param unit the time unit
     */
    fun acquireTimeout(amount: Long, unit: TimeUnit) {
        acquireTimeout = unit.asDuration(amount)
    }

    /**
     * Sets the expiry time using a Duration.
     *
     * @param duration the expiry duration
     */
    fun expiryTime(duration: Duration) {
        expiryTime = duration
    }

    /**
     * Sets the expiry time using amount and time unit.
     *
     * @param amount the amount of time
     * @param unit the time unit
     */
    fun expiryTime(amount: Long, unit: TimeUnit) {
        expiryTime = unit.asDuration(amount)
    }

    /**
     * Sets the acquire resolution time in milliseconds.
     *
     * @param millis the resolution time in milliseconds
     */
    fun acquireResolutionTime(millis: Long) {
        acquireResolutionTimeMillis = millis
    }

    /**
     * Sets the acquire resolution time using amount and time unit.
     *
     * @param amount the amount of time
     * @param unit the time unit
     */
    fun acquireResolutionTime(amount: Long, unit: TimeUnit) {
        acquireResolutionTimeMillis = unit.toMillis(amount)
    }

    /**
     * Creates a lock with the configured parameters.
     *
     * @param provider the lock provider
     * @param identifier the lock identifier
     * @param executor the executor
     * @return the configured lock
     */
    fun createLock(provider: LockProvider, identifier: String, executor: Executor): Lock =
        when {
            acquireTimeout != null && expiryTime != null && acquireResolutionTimeMillis != null -> {
                provider.create(
                    identifier,
                    executor,
                    acquireTimeout!!,
                    expiryTime!!,
                    acquireResolutionTimeMillis!!,
                )
            }
            acquireTimeout != null && expiryTime != null -> {
                provider.create(identifier, executor, acquireTimeout!!, expiryTime!!)
            }
            acquireTimeout != null -> {
                provider.create(identifier, executor, acquireTimeout!!)
            }
            else -> {
                provider.create(identifier, executor)
            }
        }
}

/**
 * Extension function to create a lock with simplified syntax. Uses default executor and provides
 * DSL configuration.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use
 * @param block the configuration block
 * @return the configured lock
 */
fun LockProvider.createLock(
    identifier: String,
    executor: Executor,
    block: LockBuilder.() -> Unit = {},
): Lock = LockBuilder().apply(block).createLock(this, identifier, executor)

/**
 * Extension function to safely create a lock that handles initialization. Ensures the provider is
 * initialized before creating the lock.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use
 * @return the created lock
 */
fun LockProvider.safeCreate(identifier: String, executor: Executor): Lock {
    try {
        initialize()
    } catch (_: Exception) {
        // Provider might already be initialized
    }
    return create(identifier, executor)
}

/**
 * Extension function to safely create a lock with timeout that handles initialization.
 *
 * @param identifier the unique identifier for the lock
 * @param executor the executor to use
 * @param timeout the timeout for the lock
 * @return the created lock
 */
fun LockProvider.safeCreate(identifier: String, executor: Executor, timeout: Duration): Lock {
    try {
        initialize()
    } catch (_: Exception) {
        // Provider might already be initialized
    }
    return create(identifier, executor, timeout)
}

private fun TimeUnit.asDuration(amount: Long): Duration =
    when (this) {
        TimeUnit.NANOSECONDS -> Duration.ofNanos(amount)
        TimeUnit.MICROSECONDS -> Duration.ofNanos(amount * 1000)
        TimeUnit.MILLISECONDS -> Duration.ofMillis(amount)
        TimeUnit.SECONDS -> Duration.ofSeconds(amount)
        TimeUnit.MINUTES -> Duration.ofMinutes(amount)
        TimeUnit.HOURS -> Duration.ofHours(amount)
        TimeUnit.DAYS -> Duration.ofDays(amount)
    }
