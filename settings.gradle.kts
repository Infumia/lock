plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

rootProject.name = "lock"

include("common", "redis", "kotlin-extensions", "kotlin-coroutines")

project(":kotlin-extensions").projectDir = file("kotlin/extensions")

project(":kotlin-coroutines").projectDir = file("kotlin/coroutines")
