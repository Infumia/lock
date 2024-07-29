import net.infumia.gradle.publish

publish("kotlin")

dependencies { compileOnly(project(":common")) }
