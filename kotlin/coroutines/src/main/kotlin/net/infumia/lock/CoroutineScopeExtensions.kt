@file:JvmName("CoroutineScopeExtensions")

package net.infumia.lock

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.*

/**
 * Creates a coroutine scope that automatically manages lock lifecycle. The scope inherits from the
 * parent scope but adds lock management capabilities.
 *
 * @param parentScope the parent coroutine scope
 * @param dispatcher the coroutine dispatcher to use (defaults to Dispatchers.Default)
 * @return a new CoroutineScope with lock management
 */
fun Lock.asCoroutineScope(
    parentScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): CoroutineScope {
    val job = SupervisorJob(parentScope.coroutineContext[Job])
    return CoroutineScope(
        parentScope.coroutineContext + job + dispatcher + LockCoroutineContext(this)
    )
}

/**
 * Coroutine context element that carries lock information. This allows accessing the current lock
 * from within coroutine contexts.
 */
data class LockCoroutineContext(val lock: Lock) :
    AbstractCoroutineContextElement(LockCoroutineContext) {
    companion object Key : CoroutineContext.Key<LockCoroutineContext>
}

/**
 * Extension to get the current lock from coroutine context. Returns null if no lock is associated
 * with the current coroutine context.
 */
val CoroutineScope.currentLock: Lock?
    get() = coroutineContext[LockCoroutineContext]?.lock

/**
 * Extension to get the current lock from coroutine context. Returns null if no lock is associated
 * with the current coroutine context.
 */
val CoroutineContext.currentLock: Lock?
    get() = this[LockCoroutineContext]?.lock

/**
 * Launches a coroutine with lock protection. The coroutine will only execute if the lock can be
 * acquired.
 *
 * @param lock the lock to acquire before executing the coroutine
 * @param context additional coroutine context
 * @param start coroutine start option
 * @param block the suspending block to execute
 * @return the Job representing the launched coroutine
 */
fun CoroutineScope.launchWithLock(
    lock: Lock,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job = launch(context + LockCoroutineContext(lock), start) { lock.withLockSuspend { block() } }

/**
 * Launches a coroutine that attempts to acquire the lock without throwing exceptions. The coroutine
 * will execute only if the lock can be acquired, otherwise it completes without executing the
 * block.
 *
 * @param lock the lock to try to acquire
 * @param context additional coroutine context
 * @param start coroutine start option
 * @param block the suspending block to execute if lock is acquired
 * @return the Job representing the launched coroutine
 */
fun CoroutineScope.tryLaunchWithLock(
    lock: Lock,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job = launch(context + LockCoroutineContext(lock), start) { lock.tryWithLockSuspend { block() } }

/**
 * Creates a Deferred that will compute a value while holding the lock. The computation will only
 * start if the lock can be acquired.
 *
 * @param lock the lock to acquire before computation
 * @param context additional coroutine context
 * @param start coroutine start option
 * @param block the suspending computation block
 * @return the Deferred representing the computation
 */
fun <T> CoroutineScope.asyncWithLock(
    lock: Lock,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T,
): Deferred<T> =
    async(context + LockCoroutineContext(lock), start) { lock.withLockSuspend { block() } }

/**
 * Creates a Deferred that will attempt to compute a value while holding the lock. Returns null if
 * the lock cannot be acquired.
 *
 * @param lock the lock to try to acquire
 * @param context additional coroutine context
 * @param start coroutine start option
 * @param block the suspending computation block
 * @return the Deferred representing the computation, may return null if lock cannot be acquired
 */
fun <T> CoroutineScope.tryAsyncWithLock(
    lock: Lock,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T,
): Deferred<T?> =
    async(context + LockCoroutineContext(lock), start) { lock.tryWithLockSuspend { block() } }

/**
 * Creates a scope that automatically releases locks when cancelled. This ensures proper cleanup of
 * locks in case of coroutine cancellation.
 *
 * @param locks the locks to manage
 * @param parentScope the parent scope
 * @param dispatcher the dispatcher to use
 * @return a managed coroutine scope
 */
fun createManagedLockScope(
    locks: List<Lock>,
    parentScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): CoroutineScope {
    val job = SupervisorJob(parentScope.coroutineContext[Job])
    val scope = CoroutineScope(parentScope.coroutineContext + job + dispatcher)

    job.invokeOnCompletion { cause ->
        if (cause != null) {
            scope.launch(SupervisorJob()) { locks.forEach { it.tryReleaseAwait() } }
        }
    }

    return scope
}

/**
 * Extension function that creates a cancellation-safe lock operation. Ensures that locks are
 * properly released even if the coroutine is cancelled.
 *
 * @param onCancellation optional callback when operation is cancelled
 * @param block the operation to perform with the lock
 * @return the result of the operation
 */
suspend fun <T> Lock.withCancellationSafety(
    onCancellation: (suspend () -> Unit)? = null,
    block: suspend () -> T,
): T = coroutineScope {
    val currentJob = coroutineContext[Job]

    currentJob?.invokeOnCompletion {
        if (it != null && it is CancellationException) {
            launch(SupervisorJob()) {
                onCancellation?.invoke()
                tryReleaseAwait()
            }
        }
    }

    withLockSuspend(block)
}
