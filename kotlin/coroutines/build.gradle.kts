import net.infumia.gradle.applyPublish

plugins { kotlin("jvm") }

applyPublish("kotlin-coroutines")

dependencies {
    compileOnly(project(":common"))
    compileOnly(libs.kotlinx.coroutines)
}
