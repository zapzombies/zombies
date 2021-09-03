import io.github.zap.build.gradle.convention.configureShadow
import io.github.zap.build.gradle.convention.paperNms
import io.github.zap.build.gradle.convention.zgpr

plugins {
    id("io.github.zap.build.gradle.convention.shadow-lib")
}

// Change wording? I am not too fancy about English
description = "Provide support for 1.16.R3 NMS specifically for Zombies Plugin"

repositories {
    maven("https://libraries.minecraft.net")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven(zgpr("zap-commons"))
}

dependencies {
    compileOnlyApi(project(":nms:nms-common"))
    paperNms("1.16.5-R0.1-SNAPSHOT")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}