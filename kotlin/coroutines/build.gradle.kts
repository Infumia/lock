import net.infumia.gradle.applyPublish

plugins { kotlin("jvm") }

applyPublish("kotlin-coroutines")

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":kotlin-extensions"))
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.kotlinx.coroutines)

    testImplementation(project(":common"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
