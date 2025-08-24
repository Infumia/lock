@file:JvmName("LockExtensions")

package net.infumia.lock

import java.time.Duration

/**
 * Executes the given block while holding the lock. Automatically acquires the lock before execution
 * and releases it afterward.
 *
 * @param block the block to execute while holding the lock
 * @return the result of executing the block
 * @throws IllegalStateException if the lock cannot be acquired
 */
inline fun <T> Lock.withLock(block: () -> T): T {
    if (!acquire()) {
        throw IllegalStateException("Failed to acquire the lock")
    }
    try {
        return block()
    } finally {
        release()
    }
}

/**
 * Executes the given block while holding the lock with a timeout. Automatically acquires the lock
 * before execution and releases it afterward.
 *
 * @param timeout the maximum time to wait for the lock
 * @param block the block to execute while holding the lock
 * @return the result of executing the block
 * @throws IllegalStateException if the lock cannot be acquired within the timeout
 */
inline fun <T> Lock.withLock(timeout: Duration, block: () -> T): T {
    if (!acquire(timeout)) {
        throw IllegalStateException("Failed to acquire the lock within timeout: $timeout")
    }
    try {
        return block()
    } finally {
        release()
    }
}

/**
 * Attempts to execute the given block with the lock, returning null if acquisition fails. This is a
 * non-blocking variant that does not throw exceptions on lock acquisition failure.
 *
 * @param block the block to execute while holding the lock
 * @return the result of executing the block, or null if the lock could not be acquired
 */
inline fun <T> Lock.tryWithLock(block: () -> T): T? =
    if (acquire()) {
        try {
            block()
        } finally {
            release()
        }
    } else {
        null
    }

/**
 * Attempts to execute the given block with the lock and timeout, returning null if acquisition
 * fails. This is a non-blocking variant that does not throw exceptions on lock acquisition failure.
 *
 * @param timeout the maximum time to wait for the lock
 * @param block the block to execute while holding the lock
 * @return the result of executing the block, or null if the lock could not be acquired within the
 *   timeout
 */
inline fun <T> Lock.tryWithLock(timeout: Duration, block: () -> T): T? =
    if (acquire(timeout)) {
        try {
            block()
        } finally {
            release()
        }
    } else {
        null
    }

/**
 * Extension function that provides a safe way to check if the lock is currently held without
 * throwing exceptions.
 *
 * @return true if the lock is currently held, false otherwise
 */
fun Lock.isHeld(): Boolean =
    try {
        isLocked()
    } catch (_: Exception) {
        false
    }

/**
 * Extension function that attempts to renew the lock safely.
 *
 * @return true if the lock was successfully renewed, false otherwise
 */
fun Lock.tryRenew(): Boolean =
    try {
        renew()
    } catch (_: Exception) {
        false
    }

/**
 * Extension function that attempts to release the lock safely.
 *
 * @return true if the lock was successfully released, false otherwise
 */
fun Lock.tryRelease(): Boolean =
    try {
        release()
    } catch (_: Exception) {
        false
    }
