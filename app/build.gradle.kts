plugins {
    id("kotlin.module")
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    implementation(libs.ktor.client.core.jvm)
    implementation(libs.ktor.client.java.jvm)

    implementation(libs.ktor.client.content.negotiation.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
}