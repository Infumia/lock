package net.infumia.gradle

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

fun Project.publish(
    moduleName: String? = null,
    javaVersion: Int = 8,
    sources: Boolean = true,
    javadoc: Boolean = true
) {
    applyCommon(javaVersion, sources, javadoc)
    apply<MavenPublishPlugin>()

    val projectName = "lock${if (moduleName == null) "" else "-$moduleName"}"
    val signRequired = project.hasProperty("sign-required")

    extensions.configure<MavenPublishBaseExtension> {
        coordinates(project.group.toString(), projectName, project.version.toString())
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
        if (signRequired) {
            signAllPublications()
        }

        pom {
            name.set(projectName)
            description.set("")
            url.set("https://github.com/Infumia/lock")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://mit-license.org/license.txt")
                }
            }
            developers {
                developer {
                    id.set("portlek")
                    name.set("Hasan Demirtaş")
                    email.set("utsukushihito@outlook.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/infumia/lock.git")
                developerConnection.set("scm:git:ssh://github.com/infumia/lock.git")
                url.set("https://github.com/infumia/lock/")
            }
        }
    }
}
