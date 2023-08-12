import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint.gradle) apply false
    alias(libs.plugins.nexus.publish)
}

group = "io.github.elkhoudiry"
version = "0.0.1"

subprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.gradle.get().pluginId)

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude("*.gradle.kts")
            exclude {
                it.file.path.contains("${buildDir}/generated/")
            }
        }
    }
}

nexusPublishing {
    this.repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            getProperty("ossrhUsername", "OSSRH_USERNAME")
            getProperty("ossrhPassword", "OSSRH_PASSWORD")

            username = extra["ossrhUsername"]?.toString()
            password = extra["ossrhPassword"]?.toString()
        }
    }
}

fun getProperty(localProperty: String, environmentVariable: String) {
    val secretPropsFile = project.rootProject.file("local.properties")
    if (secretPropsFile.exists()) {
        secretPropsFile
            .reader()
            .use { Properties().apply { load(it) } }
            .filter { (name, _) -> name == localProperty }
            .onEach { (name, value) -> extra[name.toString()] = value }
    } else {
        extra[localProperty] = System.getenv(environmentVariable)
    }
}