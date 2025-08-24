# lock

[![Maven Central Version](https://img.shields.io/maven-central/v/net.infumia/lock)](https://central.sonatype.com/artifact/net.infumia/lock)

## How to Use

<details>
<summary><strong>Gradle (Kotlin DSL)</strong></summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // Base module
    implementation("net.infumia:lock:VERSION")

    // Redis integration (Optional)
    implementation("net.infumia:lock-redis:VERSION")
    // https://mvnrepository.com/artifact/io.lettuce/lettuce-core/
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")

    // Kotlin extensions (Optional)
    implementation("net.infumia:lock-kotlin:VERSION")

    // Kotlin coroutines (Optional)
    implementation("net.infumia:lock-kotlin-coroutines:VERSION")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
```

</details>

<details>
<summary><strong>Gradle (Groovy)</strong></summary>

```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Base module
    implementation 'net.infumia:lock:VERSION'

    // Redis integration (Optional)
    implementation 'net.infumia:lock-redis:VERSION'
    // https://mvnrepository.com/artifact/io.lettuce/lettuce-core/
    implementation 'io.lettuce:lettuce-core:6.3.2.RELEASE'

    // Kotlin extensions (Optional)
    implementation 'net.infumia:lock-kotlin:VERSION'

    // Kotlin coroutines (Optional)
    implementation 'net.infumia:lock-kotlin-coroutines:VERSION'
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core/
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1'
}
```

</details>

<details>
<summary><strong>Maven</strong></summary>

```xml
<dependencies>
    <!-- Base module -->
    <dependency>
        <groupId>net.infumia</groupId>
        <artifactId>lock</artifactId>
        <version>VERSION</version>
    </dependency>

    <!-- Redis integration (Optional) -->
    <dependency>
        <groupId>net.infumia</groupId>
        <artifactId>lock-redis</artifactId>
        <version>VERSION</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/io.lettuce/lettuce-core/ -->
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
        <version>6.3.2.RELEASE</version>
    </dependency>

    <!-- Kotlin extensions (Optional) -->
    <dependency>
        <groupId>net.infumia</groupId>
        <artifactId>lock-kotlin</artifactId>
        <version>VERSION</version>
    </dependency>

    <!-- Kotlin coroutines (Optional) -->
    <dependency>
        <groupId>net.infumia</groupId>
        <artifactId>lock-kotlin-coroutines</artifactId>
        <version>VERSION</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core/ -->
    <dependency>
        <groupId>org.jetbrains.kotlinx</groupId>
        <artifactId>kotlinx-coroutines-core</artifactId>
        <version>1.8.1</version>
    </dependency>
</dependencies>
```

</details>

## Usage Examples

### Basic Usage

#### Java Example

```java
public class BasicLockExample {
    public static void main(String[] args) {
        // Create Redis client provider
        RedisURI uri = RedisURI.Builder.redis("localhost", 6379).build();
        RedisClientProvider clientProvider = () -> RedisClient.create(uri);

        // Create and initialize lock provider
        RedisLockProvider lockProvider = new RedisLockProvider(clientProvider);
        lockProvider.initialize();

        // Create executor for async operations
        Executor executor = Executors.newCachedThreadPool();

        // Create a lock with default settings
        Lock lock = lockProvider.create("my-resource-lock", executor);

        // Basic lock usage with try-finally
        if (lock.acquire(Duration.ofSeconds(10))) {
            try {
                // Critical section - access shared resource
                System.out.println("Lock acquired, performing critical work...");
                Thread.sleep(2000); // Simulate work
                System.out.println("Work completed!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // Always release the lock
                lock.release();
            }
        } else {
            System.out.println("Failed to acquire lock within timeout");
        }

        // Convenience method - automatic acquire and release
        lock.withLock(() -> {
            System.out.println("Executing in critical section with automatic management");
            // Lock is automatically released even if exception occurs
            return "Task completed";
        });
    }
}
```

#### Kotlin Example

```kotlin
fun main() {
    // Create Redis client provider
    val uri = RedisURI.Builder.redis("localhost", 6379).build()
    val clientProvider = RedisClientProvider { RedisClient.create(uri) }

    // Create and initialize lock provider
    val lockProvider = RedisLockProvider(clientProvider)
    lockProvider.initialize()

    // Create executor for async operations
    val executor = Executors.newCachedThreadPool()

    // Create a lock with default settings
    val lock = lockProvider.create("my-resource-lock", executor)

    // Basic lock usage
    if (lock.acquire(Duration.ofSeconds(10))) {
        try {
            // Critical section
            println("Lock acquired, performing critical work...")
            Thread.sleep(2000) // Simulate work
            println("Work completed!")
        } finally {
            lock.release()
        }
    } else {
        println("Failed to acquire lock within timeout")
    }

    // Using Kotlin extension functions for cleaner syntax
    val result = lock.withLock {
        println("Executing with Kotlin extension")
        "Task result"
    }

    // Safe lock attempt that returns null on failure
    val safeResult = lock.tryWithLock(Duration.ofSeconds(5)) {
        println("Safe lock execution")
        42
    }

    println("Safe result: $safeResult")
}
```

### Asynchronous Operations

#### Java CompletableFuture Example

```java
public class AsyncLockExample {
    public static void demonstrateAsync(Lock lock) {
        // Asynchronous lock acquisition
        CompletableFuture<Boolean> acquireFuture = lock.acquireAsync(Duration.ofSeconds(10));

        acquireFuture.thenAccept(acquired -> {
            if (acquired) {
                try {
                    System.out.println("Async lock acquired");
                    // Perform async work here
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.release();
                }
            } else {
                System.out.println("Failed to acquire lock asynchronously");
            }
        }).exceptionally(throwable -> {
            System.err.println("Lock acquisition failed: " + throwable.getMessage());
            return null;
        });

        // Using withLockAsync for automatic management
        CompletableFuture<String> result = lock.withLockAsync(() -> {
            // This runs asynchronously with automatic lock management
            try {
                Thread.sleep(500);
                return "Async operation completed";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Operation interrupted";
            }
        });

        result.thenAccept(System.out::println)
              .exceptionally(throwable -> {
                  System.err.println("Async operation failed: " + throwable.getMessage());
                  return null;
              });
    }
}
```

#### Kotlin Coroutines Example

```kotlin
suspend fun demonstrateCoroutines(lock: Lock) {
    // Suspending lock acquisition
    if (lock.acquireAwait(Duration.ofSeconds(10))) {
        try {
            println("Coroutine lock acquired")
            delay(1000) // Non-blocking delay
            println("Coroutine work completed")
        } finally {
            lock.releaseAwait()
        }
    } else {
        println("Failed to acquire lock in coroutine")
    }

    // Using suspending withLock extension
    val result = lock.withLockSuspend {
        println("Executing in suspending critical section")
        delay(500) // Suspending function calls allowed
        "Coroutine result"
    }

    println("Result: $result")

    // Safe coroutine lock attempt
    val safeResult = lock.tryWithLockSuspend(Duration.ofSeconds(5)) {
        delay(200)
        "Safe coroutine operation"
    }

    println("Safe coroutine result: $safeResult")
}

// Example of using coroutines in a real application
fun main() = runBlocking {
    val uri = RedisURI.Builder.redis("localhost").build()
    val clientProvider = RedisClientProvider { RedisClient.create(uri) }
    val lockProvider = RedisLockProvider(clientProvider)
    lockProvider.initialize()

    val executor = Executors.newCachedThreadPool()
    val lock = lockProvider.create("coroutine-lock", executor)

    // Launch multiple coroutines competing for the same lock
    val jobs = (1..3).map { id ->
        launch {
            lock.withLockSuspend {
                println("Coroutine $id acquired lock")
                delay(1000)
                println("Coroutine $id releasing lock")
            }
        }
    }

    jobs.joinAll()
    println("All coroutines completed")
}
```

### Advanced Usage Patterns

#### Lock Renewal for Long-Running Operations

```java
public class LockRenewalExample {
    public static void longRunningTaskWithRenewal(Lock lock) {
        if (lock.acquire(Duration.ofSeconds(10))) {
            ScheduledExecutorService renewalExecutor = Executors.newSingleThreadScheduledExecutor();

            // Schedule periodic renewal every 30 seconds
            var renewalTask = renewalExecutor.scheduleAtFixedRate(() -> {
                if (lock.isLocked()) {
                    boolean renewed = lock.renew();
                    System.out.println("Lock renewal: " + (renewed ? "SUCCESS" : "FAILED"));
                }
            }, 30, 30, TimeUnit.SECONDS);

            try {
                // Simulate long-running work (2 minutes)
                System.out.println("Starting long-running task...");
                Thread.sleep(120_000);
                System.out.println("Long-running task completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                renewalTask.cancel(true);
                renewalExecutor.shutdown();
                lock.release();
            }
        }
    }
}
```

#### Error Handling and Best Practices

```kotlin
class RobustLockingExample {

    // Robust lock handling with proper error management
    fun robustLockOperation(lock: Lock, operation: () -> String): String? {
        return try {
            lock.tryWithLock(Duration.ofSeconds(5)) {
                try {
                    operation()
                } catch (e: Exception) {
                    println("Operation failed: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Lock operation failed: ${e.message}")
            null
        }
    }

    // Coroutine-based robust locking with timeout
    suspend fun robustCoroutineLocking(lock: Lock, operation: suspend () -> String): String? {
        return try {
            withTimeout(10.seconds) {
                lock.tryWithLockSuspend {
                    try {
                        operation()
                    } catch (e: Exception) {
                        println("Coroutine operation failed: ${e.message}")
                        null
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("Lock operation timed out")
            null
        } catch (e: Exception) {
            println("Lock operation failed: ${e.message}")
            null
        }
    }

    // Safe lock status checking
    fun safeLockStatus(lock: Lock): String {
        return when {
            lock.isHeld() -> "Lock is held"
            else -> "Lock is not held or status unknown"
        }
    }
}
```

### Configuration Examples

#### Custom Redis Client Setup

```java
public class CustomRedisConfiguration {
    public static RedisLockProvider createCustomLockProvider() {
        // Configure Redis URI with authentication and SSL
        RedisURI uri = RedisURI.Builder
            .redis("redis.example.com", 6380)
            .withPassword("your-password")
            .withSsl(true)
            .withDatabase(1)
            .build();

        // Configure custom client resources
        ClientResources resources = DefaultClientResources.builder()
            .ioThreadPoolSize(4)
            .computationThreadPoolSize(4)
            .build();

        // Create client provider with custom configuration
        RedisClientProvider clientProvider = () -> {
            RedisClient client = RedisClient.create(resources, uri);
            // Additional client configuration
            client.setDefaultTimeout(Duration.ofSeconds(5));
            return client;
        };

        RedisLockProvider lockProvider = new RedisLockProvider(clientProvider);
        lockProvider.initialize();

        return lockProvider;
    }

    public static Lock createHighPerformanceLock(RedisLockProvider provider) {
        Executor executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "lock-executor");
            t.setDaemon(true);
            return t;
        });

        // Create lock with optimized settings
        return provider.create(
            "high-perf-lock",
            executor,
            Duration.ofSeconds(30),    // Acquire timeout
            Duration.ofMinutes(5),     // Lock expiry
            50L                        // Fast polling (50ms)
        );
    }
}
```

#### Kotlin DSL-Style Configuration

```kotlin
class LockConfiguration {

    fun createOptimizedLockProvider(): RedisLockProvider {
        val uri = RedisURI.Builder
            .redis("localhost", 6379)
            .withDatabase(0)
            .withTimeout(Duration.ofSeconds(5))
            .build()

        val clientProvider = RedisClientProvider {
            RedisClient.create(uri).apply {
                setDefaultTimeout(Duration.ofSeconds(3))
            }
        }

        return RedisLockProvider(clientProvider).also { it.initialize() }
    }

    fun RedisLockProvider.createConfiguredLock(
        name: String,
        acquireTimeout: Duration = Duration.ofSeconds(15),
        expiryTime: Duration = Duration.ofMinutes(2),
        pollingInterval: Long = 100L
    ): Lock {
        val executor = Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "lock-$name").apply {
                isDaemon = true
            }
        }

        return create(name, executor, acquireTimeout, expiryTime, pollingInterval)
    }
}

// Usage example
fun main() {
    val config = LockConfiguration()
    val lockProvider = config.createOptimizedLockProvider()

    val userLock = lockProvider.createConfiguredLock(
        name = "user-operations",
        acquireTimeout = Duration.ofSeconds(10),
        expiryTime = Duration.ofMinutes(1)
    )

    userLock.withLock {
        println("Performing user operation...")
        // Critical user operation
    }
}
```

## Best Practices

1. **Always use try-finally or withLock methods** to ensure locks are released
2. **Set appropriate timeouts** based on your operation duration
3. **Use renewal for long-running operations** to prevent lock expiry
4. **Choose appropriate polling intervals** - smaller for time-sensitive operations
5. **Handle failures gracefully** - lock acquisition can fail
6. **Use dedicated executors** for async operations to avoid blocking
7. **Configure Redis properly** for production use with appropriate persistence and clustering
8. **Monitor lock usage** to identify bottlenecks and contention
9. **Use Kotlin extensions and coroutines** for cleaner, more maintainable code
10. **Test lock behavior** under concurrent access scenarios

