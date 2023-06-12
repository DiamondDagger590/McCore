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
version = "1.0.0.5-SNAPSHOT"
group = "com.diamonddagger590"

java {
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

}

dependencies {

    val intellijAnnotationVersion = "12.0"
    compileOnly("com.intellij:annotations:$intellijAnnotationVersion")

    val spigotVersion = "1.19.3-R0.1-SNAPSHOT"
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")

    val cloudVersion = "1.7.0"
    implementation("cloud.commandframework:cloud-bukkit:$cloudVersion")
    implementation("cloud.commandframework:cloud-annotations:$cloudVersion")

    val adventureVersion = "4.14.0"
    implementation("net.kyori:adventure-api:$adventureVersion")

    val adventureBukkitVersion = "4.3.0"
    implementation("net.kyori:adventure-platform-bukkit:$adventureBukkitVersion")

    val adventureMiniMessageVersion = "4.14.0"
    implementation("net.kyori:adventure-text-minimessage:$adventureMiniMessageVersion")

    val configMeVersion = "1.3.0"
    implementation("ch.jalu:configme:$configMeVersion")
}

tasks {

    named<ShadowJar>("shadowJar") {

        //My gheto solution to get the commit hash on the shadow'd jar

        // Open git
        val git = org.ajoberstar.grgit.Grgit.open(file("."))
        // Use abbreviated id from git head
        archiveAppendix.set(git.head().abbreviatedId)

        // check if classifier is present before adding an unnecessary '-'.
        val classifier = archiveClassifier.get()

        // Set our desired formatting for the file name
        archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-${archiveAppendix.get()}${if (classifier.isEmpty()) "" else "-$classifier"}.${archiveExtension.get()}")

        mergeServiceFiles()
        relocate("cloud.commandframework", "com.diamonddagger590.mccore.cloud")
        relocate("net.kyori", "com.diamonddagger590.mccore.adventure")
        relocate("ch.jalu.configme", "com.diamonddagger590.mccore.configme")
    }
//    shadowJar {
//        minimize()
//        relocate("cloud.commandframework", "com.diamonddagger590.mccore.cloud")
//        relocate("net.kyori", "com.diamonddagger590.mccore.adventure")
//        relocate("ch.jalu.configme", "com.diamonddagger590.mccore.configme")
//        archiveClassifier.set("")
//    }

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
            artifact(tasks["shadowJar"])
        }

    }
}