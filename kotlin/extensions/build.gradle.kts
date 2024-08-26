import net.infumia.gradle.applyPublish

applyPublish("kotlin")

dependencies { compileOnly(project(":common")) }
