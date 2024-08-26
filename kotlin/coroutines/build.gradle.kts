import net.infumia.gradle.applyPublish

applyPublish("kotlin-coroutines")

dependencies {
    compileOnly(project(":common"))

    compileOnly(libs.kotlinx.coroutines)
}
