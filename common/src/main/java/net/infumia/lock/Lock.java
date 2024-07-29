package net.infumia.lock;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Lock {
    boolean acquire();

    CompletableFuture<Boolean> acquireAsync();

    boolean acquire(Duration timeout);

    CompletableFuture<Boolean> acquireAsync(Duration timeout);

    boolean renew();

    CompletableFuture<Boolean> renewAsync();

    boolean release();

    CompletableFuture<Boolean> releaseAsync();

    boolean isLocked();

    CompletableFuture<Boolean> isLockedAsync();

    default <T> CompletableFuture<T> withLockAsync(final Supplier<T> action) {
        return this.acquireAsync()
            .thenApply(acquired -> {
                if (!acquired) {
                    throw new IllegalStateException("Failed to acquire the lock!");
                }
                try {
                    return action.get();
                } finally {
                    this.release();
                }
            });
    }

    default CompletableFuture<?> withLockAsync(final Runnable action) {
        return this.withLockAsync(() -> {
                action.run();
                return null;
            });
    }

    default <T> T withLock(final Supplier<T> action) {
        if (!this.acquire()) {
            throw new IllegalStateException("Failed to acquire the lock!");
        }
        try {
            return action.get();
        } finally {
            this.release();
        }
    }

    default void withLock(final Runnable action) {
        this.withLock(() -> {
                action.run();
                return null;
            });
    }
}
