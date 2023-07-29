import java.util.Properties

plugins {
    id("common.module")
    id("jvm.target.library")
    id("maven-publish")
    id("signing")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

kotlin {
    val publicationsFromMainHost = listOf(jvm()).map { it.name } + "kotlinMultiplatform".toLowerCase()
    publishing {
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getExtraString("ossrhUsername")
                    password = getExtraString("ossrhPassword")
                }
            }
        }
        publications {
            matching { it.name in publicationsFromMainHost }.all {
                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>().matching { it.publication == targetPublication }
            }
            all {
                if (this !is MavenPublication) {
                    return@all
                }
                this.groupId = project.getPublishGroup()
                this.artifactId = "realtime-cassandra-$name".replace("kotlinMultiplatform", "").removeSuffix("-")
                this.version = project.getNewPublishVersion()
                artifact(javadocJar.get())

                // Provide artifacts information requited by Maven Central
                pom {
                    name.set("Realtime Cassandra")
                    description.set("Kotlin multiplatform realtime cassandra library")
                    url.set("https://github.com/elkhoudiry/kotlin-realtime-cassandra")

                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("elkhoudiry")
                            name.set("Ahmed Elkhoudiry")
                            email.set("ahmedelkhodery2@gmail.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/elkhoudiry/kotlin-realtime-cassandra")
                    }
                }
            }
        }
    }
}

jvmDependencies {
    api(libs.cassandra.driver.core)
    api(libs.cassandra.driver.query.builder)
}

signing {
    sign(publishing.publications)
}

object Meta {
    const val desc = "Cassandra realtime"
    const val license = "Apache-2.0"
    const val githubRepo = "elkhoudiry/kotlin-realtime-cassandra"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}



tasks.configureEach {
    if (this.name == "publishJvmPublicationToSonatypeRepository") {
        dependsOn("signKotlinMultiplatformPublication")
    }

    if (this.name == "publishKotlinMultiplatformPublicationToSonatypeRepository") {
        dependsOn("signJvmPublication")
    }
}