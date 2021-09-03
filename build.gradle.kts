import io.github.zap.build.gradle.convention.*

// Uncomment to use local maven version - help local testing faster
plugins {
    // id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "0.0.0-SNAPSHOT"
    id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "1.0.0"
}

repositories {
    maven("https://jitpack.io")
    maven("https://repo.rapture.pw/repository/maven-snapshots")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven(zgpr("zap-commons"))
    maven(zgpr("zap-party"))
    maven(zgpr("arena-api"))
}


dependencies {
    paperApi ("1.16.5-R0.1-SNAPSHOT")
    compileOnlyApi("io.github.zap:zap-commons:1.0.0")
    // compileOnlyApi("io.github.zap:zap-commons:0.0.0-SNAPSHOT")
    implementation("com.grinderwolf:slimeworldmanager-api:2.6.2-SNAPSHOT")
    shade(project(":nms:nms-common"))
    shade(project("nms:nms-1_16_R3"))

    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")
    shade("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
    }

    bukkitPlugin("io.github.zap:arena-api:1.0.0")
    // bukkitPlugin("io.github.zap:arena-api:0.0.0-SNAPSHOT")
    bukkitPlugin("io.github.zap:zap-party:1.0.0")
    // bukkitPlugin("io.github.zap:zap-party:0.0.0-SNAPSHOT")
    bukkitPlugin("io.lumine.xikage:MythicMobs:4.12.0")
    bukkitPlugin("com.grinderwolf:slimeworldmanager-plugin:2.6.2-SNAPSHOT")
    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.7.0")
    serverArtifactVerless("com.grinderwolf:slimeworldmanager-classmodifier:2.6.2-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.relocate {
    dependsOn(":nms:nms-common:build", ":nms:nms-1_16_R3:build")
}

publishToZGpr()

configurations.api