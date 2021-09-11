import io.github.zap.build.gradle.convention.*

ext["versionSelector"] = object : Action<ExternalModuleDependency> {
    override fun execute(emd: ExternalModuleDependency) {
        emd.version {
            if(project.property("${emd.name}Local") == "true") {
                require("0.0.0-SNAPSHOT")
            }
            else {
                require(project.property("${emd.name}Version") as String)
            }
        }
    }
}

// Uncomment to use local maven version - help local testing faster
plugins {
    id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "1.0.0-SNAPSHOT-1631207694"
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
    @Suppress("UNCHECKED_CAST")
    val selector = project.ext["versionSelector"] as Action<ExternalModuleDependency>

    paperApi ("1.16.5-R0.1-SNAPSHOT")
    compileOnlyApi("io.github.zap:zap-commons", selector)

    implementation("com.grinderwolf:slimeworldmanager-api:2.6.2-SNAPSHOT")
    shade(project(":nms:nms-common"))
    shade(project("nms:nms-1_16_R3"))

    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")
    shade("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
    }

    bukkitPlugin("io.github.zap:arena-api", selector)
    bukkitPlugin("io.github.zap:zap-party", selector)
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

tasks.test {
    dependsOn(":nms:nms-common:build", ":nms:nms-1_16_R3:build")

    testLogging {
        showStandardStreams = true
    }
}

publishToZGpr()

configurations.api