rootProject.name = "zombies"
include(":nms:nms-common")
include(":nms:nms-1_16_R3")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven {
            url = uri("https://maven.pkg.github.com/zapzombies/gradle-convention-plugins")
            credentials {
                val patFile = file("${rootProject.projectDir}/.pat")
                val credential =  if(patFile.exists()) {
                    val parts = patFile.readText().split("\n")
                    logger.lifecycle("zgpr: Credential found! Username: ${parts[0].trim()}, PAT: ${parts[1].trim().substring(0..10)}*** ")
                    parts[0].trim() to parts[1].trim()
                } else {
                    // Regular old project props & system env combo
                    val username = System.getenv("USERNAME")
                    val pat = System.getenv("TOKEN")
                    if(username == null || pat == null) {
                        logger.error("zgpr: Credential not found!")
                        "" to ""
                    } else {
                        logger.lifecycle("zgpr: Credential found! Username: $username, PAT: ${pat.substring(0..10)}*** ")
                        username to pat
                    }
                }

                username = credential.first
                password = credential.second
            }
        }
    }
}

