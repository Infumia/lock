package net.infumia.lock;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Lock {
    CompletableFuture<Boolean> acquireAsync();

    CompletableFuture<Boolean> acquireAsync(Duration timeout);

    CompletableFuture<Boolean> renewAsync();

    boolean release();

    CompletableFuture<Boolean> releaseAsync();

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
}
