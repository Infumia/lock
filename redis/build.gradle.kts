import net.infumia.gradle.applyPublish

applyPublish("redis")

dependencies {
    compileOnly(project(":common"))
    compileOnly(libs.redis)

    testImplementation(project(":common"))
    testImplementation(libs.redis)
    testImplementation(libs.guava)
}
