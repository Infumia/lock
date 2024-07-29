package net.infumia.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaPlugin

fun Project.applyCommon(javaVersion: Int = 8, sources: Boolean = true, javadoc: Boolean = true) {
    apply<JavaPlugin>()

    if (name.contains("kotlin")) {
        apply<DokkaPlugin>()
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    repositories.mavenCentral()

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion = JavaLanguageVersion.of(javaVersion) }
    }

    if (javadoc) {
        val javadocJar by
            tasks.creating(Jar::class) {
                dependsOn("javadoc")
                archiveClassifier.set("javadoc")
                from(javadoc)
            }
    }

    if (sources) {
        val sourceSets = extensions.getByType<JavaPluginExtension>().sourceSets
        val sourcesJar by
            tasks.creating(Jar::class) {
                dependsOn("classes")
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }
    }
}
