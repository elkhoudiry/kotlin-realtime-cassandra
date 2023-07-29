plugins {
    id("common.module")
    id("jvm.target.library")
}

jvmDependencies {
    implementation(libs.cassandra.driver.core)
    implementation(libs.cassandra.driver.query.builder)
}