plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "io.github.elkhoudiry.build.logic"

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaCompatibility.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaCompatibility.get())
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.nexus.publish.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("buildModule") {
            id = "build.module"
            implementationClass = "BuildConventionPlugin"
        }
        register("configModule") {
            id = "config.values.module"
            implementationClass = "ConfigValuesConventionPlugin"
        }
        register("commonModule") {
            id = "common.module"
            implementationClass = "CommonModuleConventionPlugin"
        }
        register("commonLibrary") {
            id = "common.library"
            implementationClass = "CommonLibraryConventionPlugin"
        }
        register("jvmTargetModule") {
            id = "jvm.target.library"
            implementationClass = "JvmTargetLibraryConventionPlugin"
        }
        register("publishMultiplatform") {
            id = "publish.multiplatform"
            implementationClass = "PublishMultiPlatformToCentralMavenConventionPlugin"
        }
    }
}