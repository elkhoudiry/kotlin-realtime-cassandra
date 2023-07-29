import ConfigValuesExtension.Companion.configValues
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

class ConfigValuesConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) = target.run {
        val configValues = configValues()
    }
}

open class ConfigValuesExtension {

    private lateinit var project: Project
    private lateinit var startSeparator: String
    private lateinit var endSeparator: String
    private lateinit var variablesPath: String
    private lateinit var generatedClass: File

    internal companion object {
        fun Project.configValues(): ConfigValuesExtension {
            val extension = extensions.create("configValues", ConfigValuesExtension::class.java)

            extension.project = this
            extension.startSeparator = "// values start"
            extension.endSeparator = "// values end"
            extension.variablesPath =
                "${project.buildDir}/generated/src/main/kotlin/${project.namespace().replace(".", "/")}"
            extension.generatedClass = File("${extension.variablesPath}/ConfigValues.kt")

            extension.createValuesFiles(extension.generatedClass, project)
            extension.setValues()

            extension.includeKMPSrcDirs()

            return extension
        }
    }

    fun setValues(vararg values: Pair<String, Any>) {
        val list = ArrayList<String>()
        list.add(startSeparator)
        for (value in values) {
            list.add("val ${value.first}: ${getTypeStr(value.second)} = ${getValue(value.second)}")
        }
        list.add(endSeparator)
        val file = generatedClass
        val writer = file.writer()

        writer.write(generatedClassContent(project, list))
        writer.flush()
        writer.close()
    }

    private fun getValue(value: Any): Any {
        return if (value is String) "\"$value\"" else value
    }

    private fun getTypeStr(value: Any): String {
        return when (value) {
            is String -> "String"
            is Boolean -> "Boolean"
            is Int -> "Int"
            is Long -> "Long"
            is Double -> "Double"
            else -> "Any"
        }
    }

    private fun generatedClassContent(project: Project, lines: List<String>): String {
        return """package ${project.namespace()}

object ConfigValues {
    ${lines.joinToString("\n    ")}
}
""".trimIndent()
    }

    private fun createValuesFiles(file: File, project: Project) {
        val lines = listOf(startSeparator, endSeparator)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(
                generatedClassContent(project, lines)
            )
        }
    }

    private fun includeKMPSrcDirs() {
        try {
            val kmp by lazy { project.extensions.getByType<KotlinMultiplatformExtension>() }
            val commonMain by lazy { kmp.sourceSets.getByName("commonMain") }

            commonMain.kotlin.srcDir(variablesPath)
        } catch (ex: Exception) {
            includeJavaSrcDirs()
        }
    }


    private fun includeJavaSrcDirs() {
        try {
            val sourceSets = project.extensions.getByType<SourceSetContainer>()

            sourceSets.named("main") {
                kotlin.srcDir(variablesPath)
            }
        } catch (ex: Exception) {
            throw Exception("Couldn't find kotlin multiplatform or java plugin").initCause(ex)
        }
    }
}


private val org.gradle.api.tasks.SourceSet.`kotlin`: SourceDirectorySet
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin") as SourceDirectorySet

private fun org.gradle.api.tasks.SourceSet.`kotlin`(configure: Action<SourceDirectorySet>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlin", configure)