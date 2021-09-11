import io.github.zap.build.gradle.convention.*

plugins {
    id("io.github.zap.build.gradle.convention.lib")
}

// Change wording? I am not too fancy about English
description = "Provide support for feature that would require the use of minecraft code"

repositories {
    maven(zgpr("zap-commons"))
}

dependencies {
    @Suppress("UNCHECKED_CAST")
    val selector = rootProject.ext.get("versionSelector") as Action<ExternalModuleDependency>

    compileOnlyApi("io.github.zap:zap-commons", selector)
    paperApi ("1.16.5-R0.1-SNAPSHOT")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}