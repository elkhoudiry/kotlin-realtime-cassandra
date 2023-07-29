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
            username = "your-user-token-username"
            password = "your-user-token-password"
        }
    }
}