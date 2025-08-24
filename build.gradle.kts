import com.diffplug.gradle.spotless.FormatExtension

plugins {
    java
    alias(libs.plugins.spotless)
}

subprojects { apply<JavaPlugin>() }

repositories.mavenCentral()

spotless {
    val subProjects = subprojects.map { it.projectDir.toRelativeString(projectDir) }
    val prettierConfig =
        mapOf(
            "prettier" to "3.6.2",
            "prettier-plugin-java" to "2.7.4",
            "prettier-plugin-toml" to "2.0.6",
            "@prettier/plugin-xml" to "3.4.2",
            "prettier-plugin-properties" to "0.3.0",
        )

    fun FormatExtension.newPrettier(): FormatExtension.PrettierConfig = prettier(prettierConfig)

    isEnforceCheck = false
    lineEndings = com.diffplug.spotless.LineEnding.UNIX

    flexmark {
        target("**/*.md")
        endWithNewline()
        trimTrailingWhitespace()
        flexmark()
    }

    yaml {
        target(".github/**/*.yml")
        endWithNewline()
        trimTrailingWhitespace()
        jackson().yamlFeature("LITERAL_BLOCK_STYLE", true).yamlFeature("SPLIT_LINES", false)
    }

    json {
        target("renovate.json")
        endWithNewline()
        trimTrailingWhitespace()
        jackson()
    }

    format("toml") {
        target("gradle/libs.versions.toml")
        endWithNewline()
        trimTrailingWhitespace()
        newPrettier().config(mapOf("plugins" to listOf("prettier-plugin-toml")))
    }

    format("properties") {
        target("gradle.properties")
        endWithNewline()
        trimTrailingWhitespace()
        newPrettier().config(mapOf("plugins" to listOf("prettier-plugin-properties")))
    }

    java {
        target(*subProjects.map { "$it/src/main/java/**/*.java" }.toTypedArray())
        importOrder()
        removeUnusedImports()
        endWithNewline()
        trimTrailingWhitespace()
        newPrettier()
            .config(
                mapOf(
                    "parser" to "java",
                    "tabWidth" to 4,
                    "useTabs" to false,
                    "printWidth" to 100,
                    "plugins" to listOf("prettier-plugin-java"),
                )
            )
    }

    kotlin {
        target(
            "buildSrc/src/main/kotlin/**/*.kt",
            "buildSrc/**/*.gradle.kts",
            "*.gradle.kts",
            *subProjects.map { "$it/*.gradle.kts" }.toTypedArray(),
            *subProjects.map { "$it/src/**/kotlin/**/*.kt" }.toTypedArray(),
        )
        endWithNewline()
        trimTrailingWhitespace()
        ktfmt().kotlinlangStyle().configure {
            it.setMaxWidth(100)
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
            it.setRemoveUnusedImports(true)
        }
    }
}
