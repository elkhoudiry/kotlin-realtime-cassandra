plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "io.github.elkhoudiry.build.logic"

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaCompatibility.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaCompatibility.get())

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaCompatibility.get()))
    }
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
        register("kotlinModule") {
            id = "kotlin.module"
            implementationClass = "KotlinModuleConventionPlugin"
        }
        register("publishToMavenCentral") {
            id = "publish.maven.central"
            implementationClass = "PublishToCentralMavenConventionPlugin"
        }
    }
}