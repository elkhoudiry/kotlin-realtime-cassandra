import java.util.Properties

plugins {
    id("common.module")
    id("jvm.target.library")
    id("maven-publish")
    id("signing")
    id("publish.multiplatform")
}

jvmDependencies {
    api(libs.cassandra.driver.core)
    api(libs.cassandra.driver.query.builder)
}