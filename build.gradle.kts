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
version = "1.0.0.1-SNAPSHOT"
group = "com.diamonddagger590"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()

    //Spigot
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {

    val intellijAnnotationVersion = "12.0"
    implementation("com.intellij:annotations:$intellijAnnotationVersion")

    val spigotVersion = "1.19.3-R0.1-SNAPSHOT"
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filesMatching("**/*.yml") {
        expand(project.properties)
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