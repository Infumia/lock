import net.infumia.gradle.publish

publish("redis")

dependencies { compileOnly(project(":common")) }
