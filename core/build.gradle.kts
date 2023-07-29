plugins {
    id("common.module")
    id("jvm.target.library")
    id("maven-publish")
}

kotlin {
    val publicationsFromMainHost = listOf(jvm()).map { it.name } + "kotlinMultiplatform"
    publishing {
        repositories {
            maven {
                val repository =
                    getLocalProperty("github.repository") as String?
                        ?: System.getenv("GITHUB_REPOSITORY")
                val user = getLocalProperty("github.user") as String?
                    ?: System.getenv("GITHUB_ACTOR")
                val token = getLocalProperty("github.token") as String?
                    ?: System.getenv("GITHUB_TOKEN")

                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$repository")
                credentials {
                    username = user
                    password = token
                }
            }
        }
        publications {
            matching { it.name in publicationsFromMainHost }.all {
                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>().matching { it.publication == targetPublication }
                if (this is MavenPublication){
                    this.groupId = rootProject.group.toString()
                    this.artifactId = name
                    this.version = rootProject.version.toString()
                }
            }
        }
    }
}

jvmDependencies {
    implementation(libs.cassandra.driver.core)
    implementation(libs.cassandra.driver.query.builder)
}