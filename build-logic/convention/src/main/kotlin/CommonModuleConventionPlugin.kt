import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@Suppress("unused")
class CommonModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension by lazy {
            extensions.getByType<KotlinMultiplatformExtension>()
        }
        val commonMain by lazy { extension.sourceSets.getByName("commonMain") }
        val commonTest by lazy { extension.sourceSets.getByName("commonTest") }

        pluginManager.apply(catalog.plugin("kotlin-multiplatform"))
        pluginManager.apply(catalog.plugin("kotlin-serialization"))

        commonMain.dependencies {
            implementation(catalog.library("kotlinx.coroutines.core"))
            implementation(catalog.library("kotlinx.serialization.json"))
        }

        commonTest.dependencies {
            implementation(catalog.library("kotlin.test"))
            implementation(catalog.library("kotlinx.coroutines.test"))
        }

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}

@Suppress("unused")
fun Project.commonDependencies(scope: KotlinDependencyHandler.() -> Unit) {
    val extension by lazy {
        extensions.getByType<KotlinMultiplatformExtension>()
    }
    val commonMain by lazy { extension.sourceSets.getByName("commonMain") }

    commonMain.dependencies { scope() }
}

@Suppress("unused")
fun Project.commonTestDependencies(scope: KotlinDependencyHandler.() -> Unit) {
    val extension by lazy {
        extensions.getByType<KotlinMultiplatformExtension>()
    }
    val commonTest by lazy { extension.sourceSets.getByName("commonTest") }

    commonTest.dependencies { scope() }
}