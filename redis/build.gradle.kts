import net.infumia.gradle.publish

publish("redis")

dependencies {
    compileOnly(project(":common"))
    compileOnly(libs.redis)

    testImplementation(project(":common"))
    testImplementation(libs.redis)
    testImplementation(libs.guava)
}
