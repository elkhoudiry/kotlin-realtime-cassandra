plugins {
    id("kotlin.module")
    id("publish.maven.central")
}

dependencies {
    api(libs.cassandra.driver.core)
    api(libs.cassandra.driver.query.builder)
}