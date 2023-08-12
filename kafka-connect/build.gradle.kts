plugins {
    id("kotlin.module")
    id("publish.maven.central")
}

dependencies {
    api(libs.kafka.clients)
    api(libs.kafka.streams)
}