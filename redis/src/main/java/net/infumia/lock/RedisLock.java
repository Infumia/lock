package net.infumia.lock;

import io.lettuce.core.api.StatefulRedisConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class RedisLock implements Lock {

    /* User provided */
    private final String key;
    private final Duration acquireTimeout;
    private final long acquireResolutionTimeMillis;

    /* System provided */
    private final RedisLockConnection connection;

    private boolean holdingLock;

    public RedisLock(
        /* User provided */
        final String key,
        final Duration acquireTimeout,
        final Duration expiryTimeout,
        final Executor executor,
        final long acquireResolutionTimeMillis,
        /* System provided */
        final UUID lockInstanceId,
        final StatefulRedisConnection<String, String> connection
    ) {
        this.key = key;
        this.acquireTimeout = acquireTimeout;
        this.acquireResolutionTimeMillis = acquireResolutionTimeMillis;

        this.connection = new RedisLockConnection(
            lockInstanceId.toString(),
            connection,
            expiryTimeout,
            executor
        );
    }

    @Override
    public boolean acquire() {
        return this.acquire(this.acquireTimeout);
    }

    @Override
    public CompletableFuture<Boolean> acquireAsync() {
        return this.acquireAsync(this.acquireTimeout);
    }

    @Override
    public boolean acquire(final Duration timeout) {
        return this.connection.withLock(() -> this.acquireSafe(timeout));
    }

    @Override
    public CompletableFuture<Boolean> acquireAsync(final Duration timeout) {
        return this.connection.withLockAsync(() -> this.acquireSafe(timeout));
    }

    @Override
    public boolean renew() {
        return this.connection.withLock(this::renewSafe);
    }

    @Override
    public CompletableFuture<Boolean> renewAsync() {
        return this.connection.withLockAsync(this::renewSafe);
    }

    @Override
    public boolean release() {
        return this.connection.withLock(this::releaseSafe);
    }

    @Override
    public CompletableFuture<Boolean> releaseAsync() {
        return this.connection.withLockAsync(this::releaseSafe);
    }

    @Override
    public boolean isLocked() {
        return this.connection.withLock(() -> this.holdingLock);
    }

    @Override
    public CompletableFuture<Boolean> isLockedAsync() {
        return this.connection.withLockAsync(() -> this.holdingLock);
    }

    private boolean acquireSafe(final Duration timeout) {
        if (this.holdingLock) {
            throw new IllegalStateException("This lock is not reentrant!");
        }
        final Instant deadline = Instant.now().plus(timeout);
        final Set<String> keys = Collections.singleton(this.key);
        while (Instant.now().isBefore(deadline)) {
            if (!this.connection.tryAcquire(keys).isEmpty()) {
                this.holdingLock = true;
                return true;
            }
            try {
                Thread.sleep(this.acquireResolutionTimeMillis);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private boolean renewSafe() {
        if (!this.holdingLock) {
            return false;
        }
        if (this.connection.tryRenew(Collections.singleton(this.key)).isEmpty()) {
            this.holdingLock = false;
            return false;
        }
        return true;
    }

    private boolean releaseSafe() {
        if (!this.holdingLock) {
            return false;
        }
        final boolean succeed = !this.connection.tryRelease(
            Collections.singleton(this.key)
        ).isEmpty();
        this.holdingLock = false;
        return succeed;
    }
}
