import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.7"
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
version = "1.0.0.10-SNAPSHOT"
group = "com.diamonddagger590"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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
    compileOnlyApi("com.intellij:annotations:$intellijAnnotationVersion")

    val paperVersion = "1.21-R0.1-SNAPSHOT"
    compileOnlyApi("io.papermc.paper:paper-api:$paperVersion")

    val cloudMinecraftVersion = "2.0.0-beta.9"
    api("org.incendo:cloud-paper:$cloudMinecraftVersion")
    val cloudVersion = "2.0.0-beta.9"
    api("org.incendo:cloud-minecraft-extras:$cloudVersion")
    val cloudAnnotationsVersion = "2.0.0-rc.2"
    api("org.incendo:cloud-annotations:$cloudAnnotationsVersion")

    val adventureBukkitVersion = "4.3.3"
    api("net.kyori:adventure-platform-bukkit:$adventureBukkitVersion")

    val boostedYamlVersion = "1.3.5"
    api("dev.dejvokep:boosted-yaml:$boostedYamlVersion")
    val boostedYamlSpigotVersion = "1.5"
    api("dev.dejvokep:boosted-yaml-spigot:$boostedYamlSpigotVersion")
}

tasks {

    shadowJar {
        relocate("org.incendo", "com.diamonddagger590.mccore.cloud")
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