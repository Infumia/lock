package net.infumia.lock;

import io.lettuce.core.api.StatefulRedisConnection;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executor;

public final class RedisLockProvider implements LockProvider {

    private final RedisClientProvider clientProvider;

    private StatefulRedisConnection<String, String> connection;

    public RedisLockProvider(final RedisClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public void initialize() {
        this.connection = this.clientProvider.provide().connect();
    }

    @Override
    public Lock create(
        final String identifier,
        final Executor executor,
        final Duration acquireTimeout,
        final Duration expiryTime,
        final long acquireResolutionTimeMillis
    ) {
        if (this.connection == null) {
            throw new IllegalStateException(
                "RedisLockProvider is not initialized. Call initialize() method first."
            );
        }
        return new RedisLock(
            RedisInternal.LOCK_PREFIX + identifier,
            acquireTimeout,
            expiryTime,
            executor,
            acquireResolutionTimeMillis,
            UUID.randomUUID(),
            this.connection
        );
    }
}
