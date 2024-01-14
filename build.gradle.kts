import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    dependencies {
        classpath("org.ajoberstar:gradle-git:1.2.0")
    }
}

apply {
    plugin("java")
    plugin("maven-publish")

}

//RECODE.RELEASE.PATCH.DEVELOPMENT
version = "1.0.0.6-SNAPSHOT"
group = "com.diamonddagger590"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")

    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }

    //Spigot
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")

}

dependencies {

    val intellijAnnotationVersion = "12.0"
    api("com.intellij:annotations:$intellijAnnotationVersion")

    val paperVersion = "1.20.4-R0.1-SNAPSHOT"
    api("io.papermc.paper:paper-api:$paperVersion")


    val cloudVersion = "1.7.0"
    api("cloud.commandframework:cloud-bukkit:$cloudVersion")
    api("cloud.commandframework:cloud-annotations:$cloudVersion")

    val adventureBukkitVersion = "4.3.0"
    api("net.kyori:adventure-platform-bukkit:$adventureBukkitVersion")

    val configMeVersion = "1.3.0"
    api("ch.jalu:configme:$configMeVersion")
}

tasks {

    shadowJar {
        relocate("cloud.commandframework", "com.diamonddagger590.mccore.cloud")
        archiveClassifier.set("")
    }

    build {
        dependsOn(compileJava)
        dependsOn(shadowJar)
    }
    jar {
        dependsOn(shadowJar)
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
        dependsOn(shadowJar)
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
            //artifact(tasks["shadowJar"])
        }

    }
}