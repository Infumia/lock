package net.infumia.lock;

import io.lettuce.core.RedisURI;

abstract class RedisLockAbstractTest {
    private final RedisURI uri = RedisURI.Builder.redis("localhost").build();
    private final RedisClientProviderImpl clientProvider = new RedisClientProviderImpl(this.uri);
    protected final LockProvider lockProvider = new RedisLockProvider(this.clientProvider);

    protected void start() throws Exception {
        this.clientProvider.init();
        this.lockProvider.initialize();
        System.out.println("Test started for " + this.getClass().getSimpleName());
        this.run();
        System.out.println("Test finished for " + this.getClass().getSimpleName());
    }

    protected abstract void run() throws Exception;
}
