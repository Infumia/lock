@file:JvmName("CoroutineLockExtensions")

package net.infumia.lock

import java.time.Duration
import kotlinx.coroutines.future.await

/**
 * Suspending version of acquire(). Suspends the current coroutine until the lock is acquired or the
 * operation fails.
 *
 * @return true if the lock was acquired, false otherwise
 */
suspend fun Lock.acquireAwait(): Boolean = acquireAsync().await()

/**
 * Suspending version of acquire(timeout). Suspends the current coroutine until the lock is acquired
 * within the timeout or the operation fails.
 *
 * @param timeout the maximum time to wait for the lock
 * @return true if the lock was acquired within the timeout, false otherwise
 */
suspend fun Lock.acquireAwait(timeout: Duration): Boolean = acquireAsync(timeout).await()

/**
 * Suspending version of renew(). Suspends the current coroutine until the lock is renewed or the
 * operation fails.
 *
 * @return true if the lock was renewed, false otherwise
 */
suspend fun Lock.renewAwait(): Boolean = renewAsync().await()

/**
 * Suspending version of release(). Suspends the current coroutine until the lock is released or the
 * operation fails.
 *
 * @return true if the lock was released, false otherwise
 */
suspend fun Lock.releaseAwait(): Boolean = releaseAsync().await()

/**
 * Suspending version of isLocked(). Suspends the current coroutine until the lock status is
 * determined.
 *
 * @return true if the lock is currently held, false otherwise
 */
suspend fun Lock.isLockedAwait(): Boolean = isLockedAsync().await()

/**
 * Executes the given suspending block while holding the lock. Supports structured concurrency and
 * proper cancellation. The lock is automatically acquired before execution and released afterward.
 *
 * @param block the suspending block to execute while holding the lock
 * @return the result of executing the block
 * @throws IllegalStateException if the lock cannot be acquired
 */
suspend fun <T> Lock.withLockSuspend(block: suspend () -> T): T {
    if (!acquireAwait()) {
        throw IllegalStateException("Failed to acquire the lock")
    }
    try {
        return block()
    } finally {
        releaseAwait()
    }
}

/**
 * Executes the given suspending block while holding the lock with timeout. Supports structured
 * concurrency and proper cancellation.
 *
 * @param timeout the maximum time to wait for the lock
 * @param block the suspending block to execute while holding the lock
 * @return the result of executing the block
 * @throws IllegalStateException if the lock cannot be acquired within the timeout
 */
suspend fun <T> Lock.withLockSuspend(timeout: Duration, block: suspend () -> T): T {
    if (!acquireAwait(timeout)) {
        throw IllegalStateException("Failed to acquire the lock within timeout: $timeout")
    }
    try {
        return block()
    } finally {
        releaseAwait()
    }
}

/**
 * Attempts to execute the given suspending block with the lock, returning null if acquisition
 * fails. This is a non-blocking variant that does not throw exceptions on lock acquisition failure.
 * Supports structured concurrency and proper cancellation.
 *
 * @param block the suspending block to execute while holding the lock
 * @return the result of executing the block, or null if the lock could not be acquired
 */
suspend fun <T> Lock.tryWithLockSuspend(block: suspend () -> T): T? =
    if (acquireAwait()) {
        try {
            block()
        } finally {
            releaseAwait()
        }
    } else {
        null
    }

/**
 * Attempts to execute the given suspending block with the lock and timeout, returning null if
 * acquisition fails. This is a non-blocking variant that does not throw exceptions on lock
 * acquisition failure. Supports structured concurrency and proper cancellation.
 *
 * @param timeout the maximum time to wait for the lock
 * @param block the suspending block to execute while holding the lock
 * @return the result of executing the block, or null if the lock could not be acquired within the
 *   timeout
 */
suspend fun <T> Lock.tryWithLockSuspend(timeout: Duration, block: suspend () -> T): T? =
    if (acquireAwait(timeout)) {
        try {
            block()
        } finally {
            releaseAwait()
        }
    } else {
        null
    }

/**
 * Extension function that safely attempts to renew the lock in a suspending context.
 *
 * @return true if the lock was successfully renewed, false otherwise
 */
suspend fun Lock.tryRenewAwait(): Boolean =
    try {
        renewAwait()
    } catch (_: Exception) {
        false
    }

/**
 * Extension function that safely attempts to release the lock in a suspending context.
 *
 * @return true if the lock was successfully released, false otherwise
 */
suspend fun Lock.tryReleaseAwait(): Boolean =
    try {
        releaseAwait()
    } catch (_: Exception) {
        false
    }

/**
 * Extension function that safely checks if the lock is held in a suspending context.
 *
 * @return true if the lock is currently held, false otherwise or if an error occurs
 */
suspend fun Lock.isHeldAwait(): Boolean =
    try {
        isLockedAwait()
    } catch (_: Exception) {
        false
    }
