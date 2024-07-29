package net.infumia.lock;

import java.time.Duration;
import java.util.concurrent.Executor;

public interface LockProvider {
    void initialize();

    Lock create(
        String identifier,
        Executor executor,
        Duration acquireTimeout,
        Duration expiryTime,
        long acquireResolutionTimeMillis
    );

    default Lock create(
        final String identifier,
        final Executor executor,
        final Duration acquireTimeout,
        final Duration expiryTime
    ) {
        return this.create(
                identifier,
                executor,
                acquireTimeout,
                expiryTime,
                Internal.DEFAULT_ACQUIRE_RESOLUTION_MILLIS
            );
    }

    default Lock create(
        final String identifier,
        final Executor executor,
        final Duration acquireTimeout
    ) {
        return this.create(identifier, executor, acquireTimeout, Internal.DEFAULT_EXPIRY_TIMEOUT);
    }

    default Lock create(final String identifier, final Executor executor) {
        return this.create(identifier, executor, Internal.DEFAULT_ACQUIRE_TIMEOUT);
    }
}
