plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

apply {
    plugin("java")
    plugin("maven-publish")

}

//RECODE.RELEASE.PATCH.DEVELOPMENT
version = "1.0.0.2-SNAPSHOT"
group = "com.diamonddagger590"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")

    //Spigot
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

}

dependencies {

    val intellijAnnotationVersion = "12.0"
    compileOnly("com.intellij:annotations:$intellijAnnotationVersion")

    val spigotVersion = "1.18.2-R0.1-SNAPSHOT"
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")

    val cloudVersion = "1.7.0"
    implementation("cloud.commandframework:cloud-bukkit:$cloudVersion")
    implementation("cloud.commandframework:cloud-annotations:$cloudVersion")
}

tasks {

    shadowJar {
        minimize()
        relocate("cloud.commandframework", "com.diamonddagger590.mccore.cloud")
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar )
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    processResources{
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }

    publish {
        dependsOn(compileJava)
    }
}

// publishing configuration (Thx Kitsune)
publishing {

    publications {

        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }

    }
}