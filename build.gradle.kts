import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "1.9.23"
}

val modName = "ExaMod"
version = "0.0.1"
group = "dev.topscrech"

repositories {
    // Add repositories to retrieve artifacts from
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories
}

dependencies {
    val minecraft_version = "1.20.4"
    val yarn_mappings = "1.20.4+build.3"
    val loader_version = "0.15.9"
    val fabric_kotlin_version = "1.10.19+kotlin.1.9.23"
    val fabric_version = "0.96.11+1.20.4"

    //to change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    // Fabric API. Technically optional, but you probably want it anyway
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${fabric_kotlin_version}")

    // PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs
    // You may need to force-disable transitiveness on them
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Minecraft 1.18.1 upwards uses Java 17
    options.release.set(17)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present
    // If you remove this line, sources will not be generated
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename {
            "${modName}_${version}.jar"
        }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // Assuming `remapJar` and `remapSourcesJar` are task names, not variables
            artifact(tasks.named("remapJar").get().outputs.files.singleFile) {
                builtBy(tasks.named("remapJar"))
            }

            artifact(tasks.named("remapSourcesJar").get().outputs.files.singleFile) {
                builtBy(tasks.named("remapSourcesJar"))
            }
        }
    }

    repositories {
        // Uncomment to publish to the local Maven
        // mavenLocal()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
