package net.infumia.lock;

import io.lettuce.core.RedisClient;

/**
 * The interface for providing {@link RedisClient}.
 */
public interface RedisClientProvider {
    /**
     * Provides the redis client for creating pub/sub connections.
     *
     * @return the redis client.
     */
    RedisClient provide();
}
