# lock
[![Maven Central Version](https://img.shields.io/maven-central/v/net.infumia/lock)](https://central.sonatype.com/artifact/net.infumia/lock)
## How to Use (Developers)
### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Base module
    implementation "net.infumia:lock:VERSION"

    // Pub/Sub using Redis (Optional)
    implementation "net.infumia:lock-redis:VERSION"
    // Required, https://mvnrepository.com/artifact/io.lettuce/lettuce-core/
    implementation "io.lettuce:lettuce-core:6.3.2.RELEASE"

    // Kotlin extensions (Optional)
    implementation "net.infumia:lock-kotlin:VERSION"

    // Kotlin coroutines (Optional)
    implementation "net.infumia:lock-kotlin-coroutines:VERSION"
    // Required, https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core/
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1"
}
```
### Code
```kotlin
fun main() {
}
```
