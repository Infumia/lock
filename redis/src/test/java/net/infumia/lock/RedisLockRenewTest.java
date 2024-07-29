package net.infumia.lock;

import com.google.common.base.Preconditions;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RedisLockRenewTest extends RedisLockAbstractTest {
    private static final RedisLockRenewTest INSTANCE = new RedisLockRenewTest();

    public static void main(final String[] args) throws Exception {
        RedisLockRenewTest.INSTANCE.start();
    }

    @Override
    protected void run() throws Exception {
        final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        final Lock lock = this.lockProvider.create(
            "test_lock",
            singleThreadExecutor,
            Duration.ofSeconds(1L),
            Duration.ofSeconds(1L),
            500L
        );

        Preconditions.checkState(lock.acquire(), "didn't acquire");
        Thread.sleep(100L);
        Preconditions.checkState(lock.renew(), "didn't renew");
        Thread.sleep(100L);
        Preconditions.checkState(lock.renew(), "didn't renew");
        Thread.sleep(100L);
        Preconditions.checkState(lock.release(), "didn't release");

        Preconditions.checkState(lock.acquire(), "didn't acquire");
        Thread.sleep(1005L);
        Preconditions.checkState(!lock.renew(), "renewed despite acquired");

        Preconditions.checkState(lock.acquire(), "didn't acquire");
        Thread.sleep(1005L);
        Preconditions.checkState(!lock.release(), "released despite acquired");
    }
}
