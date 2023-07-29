@file:Suppress("unused")

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Locale
import java.util.Properties

class BuildConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}

@Suppress("NOTHING_TO_INLINE")
inline fun VersionCatalog.library(name: String): Provider<MinimalExternalModuleDependency> {
    val library = findLibrary(name)

    if (!library.isPresent) {
        throw IllegalAccessException(
            "Couldn't find library: $name, at VersionCatalog file"
        )
    }
    return library.get()
}

@Suppress("NOTHING_TO_INLINE")
inline fun VersionCatalog.version(name: String): VersionConstraint {
    val version = findVersion(name)

    if (!version.isPresent) {
        throw IllegalAccessException(
            "Couldn't find version: $name, at VersionCatalog file"
        )
    }
    return version.get()
}

@Suppress("NOTHING_TO_INLINE")
inline fun VersionCatalog.plugin(name: String): String {
    val library = findPlugin(name)

    if (!library.isPresent) {
        throw IllegalAccessException(
            "Couldn't find plugin: $name, at VersionCatalog file"
        )
    }
    return library.get()
        .get().pluginId
}

internal val Project.catalog
    get() = this.extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.getLocalProperty(
    key: String,
    file: String = "local.properties",
    recursive: Boolean = true
): Any? {
    val properties = Properties()
    val localProperties = File("$projectDir/$file")

    return when {
        localProperties.isFile -> {
            properties.load(localProperties.reader())
            properties.getProperty(key)
        }

        parent != null && recursive -> {
            parent?.getLocalProperty(key, file)
        }

        else -> null
    }
}

fun Project.getLocalPropertiesFromFile(name: String = "local"): Properties {
    val publishProperties = Properties().apply {
        val propertiesFile = File("$projectDir/$name.properties")
        if (propertiesFile.exists()) {
            load(propertiesFile.reader())
        }
    }

    return publishProperties
}

fun Project.setLocalProperty(
    values: Map<String, String>,
    file: String = ""
) {
    val properties = Properties()
    val fileName = if (file.isBlank()) "local.properties" else "$file.properties"
    val propertiesFile = File("$projectDir/$fileName")

    if (!propertiesFile.exists()) {
        propertiesFile.createNewFile()
    }

    properties.load(propertiesFile.reader())
    values.forEach {
        properties[it.key] = it.value
    }

    properties.store(propertiesFile.outputStream(), null)
}

fun Project.getAllChildren(): List<Project> {
    val list = arrayListOf<Project>()

    list.addAll(childProjects.map { it.value })
    list.addAll(childProjects.map { it.value.getAllChildren() }.flatten())

    return list
}

fun Project.namespace(): String {
    return "${rootProject.group}.$group.$name"
        .replace("/", ".")
        .replace("-", ".")
        .replace("_", ".")
        .removePrefix(".")
        .toLowerCase(Locale.getDefault())
}

fun Project.getPublishGroup(): String {
    return rootProject.group as String
}

fun Project.getPublishArtifactId(): String {
    return path.replace(":", "-")
        .removeSuffix("-")
        .removePrefix("-")
}

fun Project.getNewPublishVersion(): String {
    val envVersion = System.getenv("PUBLISH_REF")
    println("[LOG] ref version: $envVersion")
    val localVersion by lazy { rootProject.version as String }

    if (!envVersion.isNullOrBlank()) {
        return getVersionFromRef(envVersion, localVersion)
    }

    return getVersionFromRef(localVersion, localVersion)
}

internal fun getVersionFromRef(
    ref: String,
    current: String
): String {
    val (major, minor) = ref.split(".")
        .map { it.toInt() }
    val (currentMajor, currentMinor, currentPatch) = current.split(".")
        .map { it.toInt() }

    if (major > currentMajor) {
        return "$major.0.0"
    }

    if (minor > currentMinor){
        return "$major.$minor.0"
    }

    return "$currentMajor.$currentMinor.${currentPatch + 1}"
}

val Project.nameOneWord
    get() = name.split("-", "_", " ", ".").joinToString("") { it.capitalize() }