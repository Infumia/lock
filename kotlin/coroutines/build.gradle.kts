import net.infumia.gradle.publish

publish("kotlin-coroutines")

dependencies {
    compileOnly(project(":common"))

    compileOnly(libs.kotlinx.coroutines)
}
