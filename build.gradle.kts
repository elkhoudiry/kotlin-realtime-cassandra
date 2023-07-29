import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.nexus.publish)
}

group = "io.github.elkhoudiry"
version = "0.0.1"

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