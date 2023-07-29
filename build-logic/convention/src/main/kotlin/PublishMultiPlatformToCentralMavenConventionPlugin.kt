import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.all
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.util.Properties

class PublishMultiPlatformToCentralMavenConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("kotlin-multiplatform")
        pluginManager.apply("maven-publish")
        pluginManager.apply("signing")

        val kmp = extensions.getByType<KotlinMultiplatformExtension>()
        val javadocJar by tasks.registering(Jar::class) { archiveClassifier.set("javadoc") }
        val publicationsFromMainHost = listOf(kmp.jvm()).map { it.name } + "kotlinMultiplatform".toLowerCase()

        // Stub secrets to let the project sync and build without the publication values set up
        extra["signing.password"] = null
        extra["signing.secretKey"] = null
        extra["ossrhUsername"] = null
        extra["ossrhPassword"] = null

        val secretPropsFile = project.rootProject.file("local.properties")
        if (secretPropsFile.exists()) {
            secretPropsFile
                .reader()
                .use { Properties().apply { load(it) } }
                .onEach { (name, value) -> extra[name.toString()] = value }
        } else {
            extra["signing.password"] = System.getenv("SIGNING_PASSWORD")
            extra["signing.secretKey"] = System.getenv("SIGNING_KEY")
            extra["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
            extra["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
        }

        extensions.configure<PublishingExtension> {
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
                    artifact(javadocJar)

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

        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(extra["signing.secretKey"]?.toString(), extra["signing.password"]?.toString())
            sign(extensions.getByType<PublishingExtension>().publications)
        }

        tasks.configureEach {
            if (this.name == "publishJvmPublicationToSonatypeRepository") {
                dependsOn("signKotlinMultiplatformPublication")
            }

            if (this.name == "publishKotlinMultiplatformPublicationToSonatypeRepository") {
                dependsOn("signJvmPublication")
            }
        }
    }
}