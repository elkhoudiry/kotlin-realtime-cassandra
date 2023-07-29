import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

class JvmTargetLibraryConventionPlugin : Plugin<Project>{

    override fun apply(target: Project): Unit = with(target) {
        val extension by lazy {
            extensions.getByType<KotlinMultiplatformExtension>()
        }
        @Suppress("UNUSED_VARIABLE")
        val jvmMain by lazy { extension.sourceSets.getByName("jvmMain") }
        val jvmTest by lazy { extension.sourceSets.getByName("jvmTest") }

        pluginManager.apply("common.module")

        extension.jvm()

        jvmTest.dependencies {
            implementation(catalog.library("kotlin.test"))
        }
    }
}

@Suppress("unused")
fun Project.jvmDependencies(scope: KotlinDependencyHandler.() -> Unit) {
    val extension by lazy {
        extensions.getByType<KotlinMultiplatformExtension>()
    }
    val jvmMain by lazy { extension.sourceSets.getByName("jvmMain") }

    jvmMain.dependencies { scope() }
}

@Suppress("unused")
fun Project.jvmTestDependencies(scope: KotlinDependencyHandler.() -> Unit) {
    val extension by lazy {
        extensions.getByType<KotlinMultiplatformExtension>()
    }
    val jvmTest by lazy { extension.sourceSets.getByName("jvmTest") }

    jvmTest.dependencies { scope() }
}