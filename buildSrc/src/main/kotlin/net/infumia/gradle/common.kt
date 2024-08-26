package net.infumia.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaPlugin

fun Project.applyCommon(javaVersion: Int = 8) {
    apply<JavaPlugin>()

    if (name.contains("kotlin")) {
        apply<DokkaPlugin>()
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    repositories.mavenCentral()

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersion)
            vendor = JvmVendorSpec.ADOPTIUM
        }
    }
}
