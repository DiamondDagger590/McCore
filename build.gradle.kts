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
version = "1.0.0.13-SNAPSHOT"
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

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") //Papi
    maven("https://repo.nexomc.com/releases") // Nexo
    maven("https://maven.devs.beer/") // ItemsAdder
    maven("https://mvn.lumine.io/repository/maven-public/") //MythicMobs + Model Engine

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

    val paperVersion = "1.21.5-R0.1-SNAPSHOT"
    compileOnlyApi("io.papermc.paper:paper-api:$paperVersion")

    val placeholderAPIVersion = "2.11.6"
    compileOnlyApi("me.clip:placeholderapi:$placeholderAPIVersion")
    testImplementation("me.clip:placeholderapi:$placeholderAPIVersion")

    val headDatabaseVersion = "1.3.1"
    compileOnly("com.arcaniax:HeadDatabase-API:$headDatabaseVersion")

    // Custom items
    val itemsAdderVersion = "4.0.2-beta-release-11"
    compileOnly("dev.lone:api-itemsadder:$itemsAdderVersion")
    val nexoVersion = "1.2.0"
    compileOnly("com.nexomc:nexo:$nexoVersion") {
        exclude(group = "net.byteflux")
    }

    val mythicMobsVersion = "5.6.1"
    compileOnly("io.lumine:Mythic-Dist:$mythicMobsVersion")

    val modelEngineVersion = "R4.0.4"
    compileOnly("com.ticxo.modelengine:ModelEngine:$modelEngineVersion")

    // Command annotations
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

    val hikariVersion = "6.1.0"
    api("com.zaxxer:HikariCP:$hikariVersion")
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