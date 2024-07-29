package net.infumia.lock;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class RedisLockConsistencyTest extends RedisLockAbstractTest {
    private static final RedisLockConsistencyTest INSTANCE = new RedisLockConsistencyTest();

    public static void main(final String[] args) throws Exception {
        RedisLockConsistencyTest.INSTANCE.start();
    }

    @Override
    protected void run() throws Exception {
        final Executor executor = Executors.newCachedThreadPool();
        final Accessor accessor = new Accessor();

        final List<Thread> threads = IntStream.rangeClosed(0, 9)
            .mapToObj(value -> {
                final Lock lock = this.lockProvider.create("test_lock", executor);
                return new Thread(() -> this.testThread(accessor, lock));
            })
            .collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (final Thread thread : threads) {
            thread.join();
        }

        final int failCount = accessor.failCount.get();
        System.out.println("Fail count = " + failCount + ", access count = " + accessor.accessCount.get());
    }

    private void testThread(final Accessor accessor, final Lock lock) {
        System.out.println("Thread starting " + Thread.currentThread().getName());
        int iterations = 100;
        while (iterations-- > 0) {
            lock.withLockAsync(accessor::access).join();
            RedisLockConsistencyTest.randomSleep();
        }
        System.out.println("Thead finished " + Thread.currentThread().getName());
    }

    private static final class Accessor {
        private final AtomicBoolean beingAccessed = new AtomicBoolean(false);
        private final AtomicInteger failCount = new AtomicInteger(0);
        private final AtomicInteger accessCount = new AtomicInteger(0);

        public void access() {
            this.accessCount.incrementAndGet();

            if (this.beingAccessed.get()) {
                this.failCount.incrementAndGet();
                return;
            }

            this.beingAccessed.set(true);
            RedisLockConsistencyTest.randomSleep();
            this.beingAccessed.set(false);
        }
    }

    private static void randomSleep() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10));
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
