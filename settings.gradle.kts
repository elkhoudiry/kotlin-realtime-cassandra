pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
    }
}

rootProject.name = "kotlin-realtime-cassandra"

include(":cassandra")
include(":kafka-connect")
include(":app")