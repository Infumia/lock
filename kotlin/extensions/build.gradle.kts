import net.infumia.gradle.applyPublish

plugins { kotlin("jvm") }

applyPublish("kotlin")

dependencies {
    compileOnly(project(":common"))
}
