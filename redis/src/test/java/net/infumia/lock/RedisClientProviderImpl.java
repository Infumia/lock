package net.infumia.lock;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

final class RedisClientProviderImpl implements RedisClientProvider {
    private final RedisURI uri;

    private RedisClient client;

    RedisClientProviderImpl(final RedisURI uri) {
        this.uri = uri;
    }

    @Override
    public RedisClient provide() {
        if (this.client == null) {
            throw new IllegalStateException("Use RedisClientProviderImpl#provide() first!");
        }
        return this.client;
    }

    public void init() {
        this.client = RedisClient.create(this.uri);
    }

    public void close() {
        this.client.close();
        this.client = null;
    }
}
